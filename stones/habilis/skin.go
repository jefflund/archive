package habilis

import (
	"github.com/jefflund/gorl"
)

type Stone uint

const (
	CoreStone Stone = 1 << iota
	ToHitStone
	ToEvsStone
	ToDmgStone
	ToArmStone
	ToRunStone
	ToIntStone
	AlertStone

	AnyStone Stone = 0

	CombatStone = ToHitStone | ToEvsStone | ToDmgStone | ToArmStone
)

type Circle struct {
	Type  Stone
	Count int
}

type Skin struct {
	Name    string
	Face    gorl.Glyph
	Pos     *gorl.Tile
	Circles []*Circle

	Mark gorl.Entity

	gorl.ComponentSlice
}

func (e *Skin) Handle(v gorl.Event) {
	switch v := v.(type) {
	case *gorl.RenderEvent:
		v.Render, v.Priority = e.Face, true
	case *gorl.BumpEvent:
		e.Melee(v.Bumped)
	case *gorl.CollideEvent:
		e.Handle(gorl.Log("%s <cannot pass> %o", e, v.Obstacle))
	case *gorl.PosEvent:
		e.Pos = v.Pos
	case *gorl.NameEvent:
		v.Name = e.Name

	case *CountEvent:
		v.Result = e.StoneCount(v.Type)
	case *RollEvent:
		v.Result = e.CoreRole()
	case *DamageEvent:
		e.TakeDamage(v.Amount)
	case *MarkEvent:
		if v.Mark == nil {
			v.Mark = e.Mark
		} else if e.Mark == nil {
			e.Mark = v.Mark
		} else {
			q := MarkEvent{}
			e.Mark.Handle(&q)
			if q.Mark != e {
				e.Mark = v.Mark
			}
		}
	case *DieEvent:
		e.Die()
	}

	e.ComponentSlice.Handle(v)
}

func (e *Skin) Melee(t gorl.Entity) {
	e.Mark = t
	t.Handle(&MarkEvent{e})

	atk, def := RollEvent{}, RollEvent{}
	e.Handle(&atk)
	t.Handle(&def)
	roll := atk.Result - def.Result

	tohit, toevs := CountEvent{Type: ToHitStone}, CountEvent{Type: ToEvsStone}
	e.Handle(&tohit)
	t.Handle(&toevs)
	hit := roll + tohit.Result - toevs.Result

	if hit > 0 {
		todmg, toarm := CountEvent{Type: ToDmgStone}, CountEvent{Type: ToArmStone}
		e.Handle(&todmg)
		t.Handle(&toarm)
		dmg := roll + todmg.Result - toarm.Result

		if dmg > 0 {
			e.Handle(gorl.Log("%s <hit> %o for %x", e, t, dmg))
			t.Handle(&DamageEvent{dmg})
		} else {
			e.Handle(gorl.Log("%s <graze> %o", e, t))
		}
	} else {
		e.Handle(gorl.Log("%s <miss> %o", e, t))
	}
}

func (e *Skin) StoneCount(s Stone) int {
	total := 0
	for _, c := range e.Circles {
		if c.Type&s == s {
			total += c.Count
		}
	}
	return total
}

func (e *Skin) CoreRole() int {
	q := CountEvent{Type: CoreStone}
	e.Handle(&q)
	return gorl.RandRange(0, q.Result)
}

func (e *Skin) TakeDamage(amount int) {
	buckets := make([]int, len(e.Circles))
	for i, c := range e.Circles {
		buckets[i] = c.Count
	}
	dmg, dead := gorl.RandBuckets(buckets, amount)
	for _, i := range dmg {
		e.Circles[i].Count--
	}
	if dead {
		e.Handle(&DieEvent{})
	}
}

func (e *Skin) Die() {
	e.Handle(gorl.Log("%s <die>", e))
	e.Handle(&gorl.UIEvent{})
	e.Pos.RemoveOccupant()

}

type CountEvent struct {
	Type   Stone
	Result int
}

type RollEvent struct {
	Result int
}

type DamageEvent struct {
	Amount int
}

type MarkEvent struct {
	Mark gorl.Entity
}

type DieEvent struct{}
