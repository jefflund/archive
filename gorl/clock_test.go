package gorl

import (
	"testing"
)

// Stubb for ExampleDeltaClock
func DoStuff() BehaviorState { return SuccessState }

// Consts for ExampleDeltaClock
const (
	ActOrderOffset = .5
	MoveSpeed      = 1
)

func ExampleDeltaClock() {
	// Create a new DeltaClock
	clock := NewDeltaClock()

	// Here ComponentSlice is a stand in for a more fleshed out Entity...
	entity := &ComponentSlice{}

	// Add scheduler to Entity to it responds to ScheduleEvent properly
	entity.AddComponent(clock.NewScheduler(entity, ActOrderOffset))

	// Add a BehaviorTree which responds to ActEvent from DeltaClock.Tick
	entity.AddComponent(BehaviorTree(
		ActBehavior(func() BehaviorState {
			// Normally there would be more interesting Behavior here...
			DoStuff()

			// As part of the Behavior, make sure to reschedule the Entity!
			// If you forget this step, the next call to DeltaClock.Tick will not
			// send an ActEvent to this Entity.
			entity.Handle(&ScheduleEvent{MoveSpeed})

			return RunningState
		}),
	))
}

func checkAdvance(expected []Entity, actual map[Entity]struct{}) bool {
	if len(expected) != len(actual) {
		return false
	}
	for _, e := range expected {
		if _, ok := actual[e]; !ok {
			return false
		}
	}
	return true
}

func checkSchedule(t *testing.T, c *DeltaClock, schedule [][]Entity, speeds map[Entity]float64) {
	for i, expected := range schedule {
		actual := c.Advance()
		if !checkAdvance(expected, actual) {
			t.Errorf("Unexpected delta (%v!=%v) at delta=%d", expected, actual, i)
		}
		for e := range actual {
			c.Schedule(e, speeds[e])
		}
	}
}

func initClock(speeds map[Entity]float64) *DeltaClock {
	c := NewDeltaClock()
	for e, s := range speeds {
		c.Schedule(e, s)
	}
	return c
}

func TestDeltaClock_Schedule(t *testing.T) {
	e1, e2, e3 := &ComponentSlice{}, &ComponentSlice{}, &ComponentSlice{}
	speeds := map[Entity]float64{e1: 1, e2: 1.5, e3: 2}
	c := initClock(speeds)

	schedule := [][]Entity{
		{e1}, {e2},
		{e1, e3}, {e2},
		{e1}, {e2},
		{e1, e3}, {e2},
		{e1}, {e2},
	}
	checkSchedule(t, c, schedule, speeds)
}

func TestDeltaClock_Unschedule(t *testing.T) {
	e1, e2, e3 := &ComponentSlice{}, &ComponentSlice{}, &ComponentSlice{}
	speeds := map[Entity]float64{e1: 2, e2: 2, e3: 3}
	c := initClock(speeds)
	c.Unschedule(e2)
	c.Unschedule(e3)
	schedule := [][]Entity{{e1}, {}, {e1}}
	checkSchedule(t, c, schedule, speeds)
}

func TestDeltaClock_Delta(t *testing.T) {
	e1, e2, e3 := &ComponentSlice{}, &ComponentSlice{}, &ComponentSlice{}
	speeds := map[Entity]float64{e1: 1, e2: 1.5, e3: 2}
	c := initClock(speeds)

	if delta, ok := c.Delta(e1); !ok || delta != 1 {
		t.Errorf("Delta(e1) got wrong delta")
	}
	if delta, ok := c.Delta(e2); !ok || delta != 1 {
		t.Errorf("Delta(e2) got wrong delta")
	}
	if delta, ok := c.Delta(e3); !ok || delta != 2 {
		t.Errorf("Delta(e3) got wrong delta")
	}
	if _, ok := c.Delta(&ComponentSlice{}); ok {
		t.Errorf("Delta(unknown) incorrectly gave a delta")
	}
}

func TestDeltaClock_UnscheduleAdvanced(t *testing.T) {
	e1, e2, e3 := &ComponentSlice{}, &ComponentSlice{}, &ComponentSlice{}
	c := NewDeltaClock()
	c.Schedule(e1, 1)
	c.Schedule(e2, 1)
	c.Schedule(e3, 1)

	a := c.Advance()
	c.Unschedule(e2)

	if !checkAdvance([]Entity{e1, e3}, a) {
		t.Errorf("Unschedule left Advanced Entity")
	}
}

func TestDeltaClock_Reschedule(t *testing.T) {
	e1, e2 := &ComponentSlice{}, &ComponentSlice{}
	c := NewDeltaClock()
	c.Schedule(e1, 1)
	c.Schedule(e2, 2)
	c.Schedule(e1, 3)

	if a := c.Advance(); !checkAdvance([]Entity{}, a) {
		t.Errorf("Reschedule failed to remove from old delta")
	}
	if a := c.Advance(); !checkAdvance([]Entity{e2}, a) {
		t.Errorf("Reschedule changed the delta order")
	}
	if a := c.Advance(); !checkAdvance([]Entity{e1}, a) {
		t.Errorf("Reschedule failed to add to new delta")
	}
}

func TestDeltaClock_Scheduler(t *testing.T) {
	e1, e2, e3 := &ComponentSlice{}, &ComponentSlice{}, &ComponentSlice{}
	entities := map[*ComponentSlice]struct{}{e1: {}, e2: {}, e3: {}}
	speeds := map[Entity]float64{e1: 1, e2: 1, e3: 2}
	offsets := map[Entity]float64{e1: 0, e2: .5, e3: 0}

	c := NewDeltaClock()
	var actual map[Entity]struct{}
	for e := range entities {
		func(e *ComponentSlice) {
			e.AddComponent(ComponentFunc(func(v Event) {
				switch v.(type) {
				case *ActEvent:
					actual[e] = struct{}{}
				}
			}))
		}(e)
		e.AddComponent(c.NewScheduler(e, offsets[e]))
		e.Handle(&ScheduleEvent{speeds[e]})
	}

	schedule := [][]Entity{
		{e1}, {e2},
		{e1, e3}, {e2},
		{e1}, {e2},
		{e1, e3}, {e2},
		{e1}, {e2},
	}

	for i, expected := range schedule {
		actual = make(map[Entity]struct{})
		c.Tick()
		if !checkAdvance(expected, actual) {
			t.Errorf("Unexpected delta (%v!=%v) at delta=%d", expected, actual, i)
		}
		for e := range actual {
			e.Handle(&ScheduleEvent{speeds[e]})
		}
	}
}
