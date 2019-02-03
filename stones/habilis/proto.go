package habilis

import (
	"github.com/jefflund/gorl"
)

type SkinProto struct {
	Name     string
	Face     gorl.Glyph
	Circles  []Circle
	Behavior func(*Skin) gorl.Component
}

func (p SkinProto) Create() *Skin {
	circles := make([]*Circle, len(p.Circles))
	for i, c := range p.Circles {
		circles[i] = &Circle{c.Type, c.Count}
	}
	e := &Skin{
		Name:    p.Name,
		Face:    p.Face,
		Circles: circles,
	}
	e.AddComponent(p.Behavior(e))
	return e
}

var (
	HeroProto = SkinProto{
		Name: "you",
		Face: gorl.Char('@'),
		Circles: []Circle{
			{CoreStone, 6},
		},
		Behavior: HeroController,
	}

	SaberToothProto = SkinProto{
		Name: "saber-tooth",
		Face: gorl.ColoredChar('t', gorl.ColorYellow),
		Circles: []Circle{
			{CoreStone, 6},
			{CombatStone, 2},
			{ToRunStone, 1},
		},
		Behavior: WanderComponent,
	}
	MammothProto = SkinProto{
		Name: "mammoth",
		Face: gorl.ColoredChar('M', gorl.ColorLightBlack),
		Circles: []Circle{
			{CoreStone, 9},
			{CombatStone, 1},
			{ToArmStone, 1},
			{ToRunStone, 1},
			{ToIntStone, 1},
		},
		Behavior: WanderComponent,
	}
	HareProto = SkinProto{
		Name: "hare",
		Face: gorl.ColoredChar('r', gorl.ColorWhite),
		Circles: []Circle{
			{CoreStone, 3},
			{ToRunStone, 1},
			{AlertStone, 1},
		},
		Behavior: func(e *Skin) gorl.Component {
			return gorl.BehaviorTree(gorl.SelectBehavior(
				WanderBehavior(e),
			))
		},
	}
	DeerProto = SkinProto{
		Name: "deer",
		Face: gorl.ColoredChar('d', gorl.ColorLightYellow),
		Circles: []Circle{
			{CoreStone, 6},
			{ToRunStone, 1},
			{CombatStone, 1},
		},
		Behavior: WanderComponent,
	}
)

func HeroController(e *Skin) gorl.Component {
	return gorl.BehaviorTree(gorl.ActBehavior(func() gorl.BehaviorState {
		e.Handle(&gorl.UIEvent{})
		switch key := gorl.TermGetKey(); key {
		case gorl.KeyEsc:
			e.Handle(&DieEvent{})
		default:
			if delta, ok := gorl.KeyMap[key]; ok {
				if delta.X != 0 || delta.Y != 0 {
					e.Pos.MoveOccupant(delta)
				} else {
					e.Handle(gorl.Log("%s <rest>", e))
				}
			}
			e.Handle(&gorl.ScheduleEvent{1})
		}
		return gorl.SuccessState
	}))
}

func WanderComponent(e *Skin) gorl.Component {
	return gorl.BehaviorTree(gorl.ActBehavior(func() gorl.BehaviorState {
		if delta := gorl.RandDelta(); delta.X != 0 || delta.Y != 0 {
			e.Pos.MoveOccupant(delta)
		}
		e.Handle(&gorl.ScheduleEvent{1})
		return gorl.SuccessState
	}))
}

func WanderBehavior(e *Skin) gorl.Behavior {
	return gorl.ActBehavior(func() gorl.BehaviorState {
		for _, tile := range gorl.FoV(e.Pos, 5) {
			if tile.Occupant != nil && tile.Occupant != e {
				e.Mark = tile.Occupant
			}
		}

		//if e.Mark != nil {
		//return gorl.FailureState
		//}

		delta := gorl.RandDelta()
		if adj, ok := e.Pos.Adjacent[delta]; ok && adj.Pass {
			e.Pos.MoveOccupant(delta)
		}
		e.Handle(&gorl.ScheduleEvent{1})
		return gorl.RunningState
	})
}
