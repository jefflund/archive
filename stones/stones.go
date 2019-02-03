package main

import (
	"fmt"

	"github.com/jefflund/gorl"
	"github.com/jefflund/stones/habilis"
)

const (
	COLS     = 80
	ROWS     = 24
	FOVDIST  = (ROWS - 1) / 2
	VIEWSIZE = 2*FOVDIST + 1

	HERO_OFFSET = 0
	NPC_OFFSET  = .5
)

func main() {
	gorl.TermMustInit()
	defer gorl.TermDone()

	clock := gorl.NewDeltaClock()
	world := habilis.NewWorld()

	viewport := gorl.NewCameraWidget(0, 0, VIEWSIZE, VIEWSIZE)
	journal := gorl.NewLogWidget(VIEWSIZE, 0, COLS-VIEWSIZE, ROWS-1)
	status := gorl.NewTextWidget(0, VIEWSIZE, COLS, 1)
	screen := gorl.Screen{viewport, journal, status}

	hero := habilis.HeroProto.Create()
	hero.AddComponent(clock.NewScheduler(hero, HERO_OFFSET))
	hero.AddComponent(viewport.NewCamera(hero, FOVDIST))
	hero.AddComponent(journal.NewLogger())
	hero.AddComponent(status.NewBinding(func() string {
		count := habilis.CountEvent{Type: habilis.AnyStone}
		hero.Handle(&count)
		return fmt.Sprintf("Hero at (%d, %d)\t%d Stones", hero.Pos.Offset.X, hero.Pos.Offset.Y, count.Result)
	}))
	hero.AddComponent(screen)
	gorl.RandOpenTile(world).PlaceOccupant(hero)

	protos := []habilis.SkinProto{
		habilis.MammothProto,
		habilis.SaberToothProto,
		habilis.HareProto,
		habilis.DeerProto,
	}
	for i := 0; i < 20; i++ {
		creature := protos[gorl.RandIntn(len(protos))].Create()
		creature.AddComponent(clock.NewScheduler(creature, NPC_OFFSET))
		creature.AddComponent(journal.NewConditionalLogger(func() bool {
			_, ok := gorl.LoS(hero.Pos, creature.Pos, FOVDIST)
			return ok
		}))
		gorl.RandOpenTile(world).PlaceOccupant(creature)
	}

	for hero.Pos != nil {
		clock.Tick()
	}

	gorl.TermGetKey()
}
