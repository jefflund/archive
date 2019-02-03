package gorl

import (
	"reflect"
	"testing"
)

// stubs for ExampleBehavior
func IsSafe() bool           { return true }
func IsHungry() bool         { return true }
func FoodNear() bool         { return true }
func GetFood() BehaviorState { return SuccessState }
func Wander() BehaviorState  { return SuccessState }
func RunAway() BehaviorState { return SuccessState }

func ExampleBehavior() {
	// ComponentSliece used for illustrative purpose
	// normally you'd need to add other Component, data, etc to your Entity
	entity := &ComponentSlice{}

	// create an example BehaviorTree for our entity
	controller := BehaviorTree(SelectBehavior(
		// idle behavior
		ConcurrentBehavior(
			// only idle while safe
			BehaviorCondition(IsSafe),
			// eat or wander
			SelectBehavior(
				// eat
				SequenceBehavior(
					BehaviorCondition(IsHungry),
					BehaviorCondition(FoodNear),
					ActBehavior(GetFood),
				),
				// wander
				ActBehavior(Wander),
			),
		),
		// flea from danger
		ConcurrentBehavior(
			InvertBehavior(BehaviorCondition(IsSafe)),
			ActBehavior(RunAway),
		),
	))

	// add BehaviorTree Component to our Entity
	entity.AddComponent(controller)

	// note that normally ActEvent is sent by DeltaClock.Tick
	entity.Handle(&ActEvent{})
}

type BehaviorStub struct {
	RunStub []BehaviorState
	IsReset bool
}

func (b *BehaviorStub) Reset() {
	b.IsReset = true
}

func (b *BehaviorStub) Run() BehaviorState {
	if len(b.RunStub) == 0 {
		return DoneState
	}

	b.IsReset = false
	s := b.RunStub[0]
	b.RunStub = b.RunStub[1:]
	return s
}

func TestSequenceBehavior(t *testing.T) {
	b1 := &BehaviorStub{[]BehaviorState{SuccessState, SuccessState, SuccessState}, false}
	b2 := &BehaviorStub{[]BehaviorState{SuccessState, FailureState, RunningState, SuccessState}, false}
	b3 := &BehaviorStub{[]BehaviorState{SuccessState, SuccessState}, false}
	s := SequenceBehavior(b1, b2, b3)

	if s.Run() != SuccessState {
		t.Errorf("Sequence failed success run")
	}
	if s.Run() != DoneState {
		t.Errorf("Sequence failed done run after success")
	}

	s.Reset()
	if !b1.IsReset || !b2.IsReset || !b3.IsReset {
		t.Errorf("Sequence failed to reset children after success")
	}

	if s.Run() != FailureState {
		t.Errorf("Sequence failed failure run")
	}
	if s.Run() != DoneState {
		t.Errorf("Sequence failed done run after failure")
	}

	s.Reset()
	if !b1.IsReset || !b2.IsReset || !b3.IsReset {
		t.Errorf("Sequence failed to reset children after failure")
	}

	if s.Run() != RunningState {
		t.Errorf("Sequence failed running run")
	}
	if s.Run() != SuccessState {
		t.Errorf("Sequence failed continue from running run")
	}
	if s.Run() != DoneState {
		t.Errorf("Sequence failed done run after running run")
	}
}

func TestSelectBehavior(t *testing.T) {
	b1 := &BehaviorStub{[]BehaviorState{FailureState, FailureState, FailureState}, false}
	b2 := &BehaviorStub{[]BehaviorState{SuccessState, FailureState, RunningState, FailureState}, false}
	b3 := &BehaviorStub{[]BehaviorState{FailureState, SuccessState}, false}
	s := SelectBehavior(b1, b2, b3)

	if s.Run() != SuccessState {
		t.Errorf("Selector failed success run")
	}
	if s.Run() != DoneState {
		t.Errorf("Selector failed done run after success")
	}

	s.Reset()
	if !b1.IsReset || !b2.IsReset || !b3.IsReset {
		t.Errorf("Selector failed to reset children after success")
	}

	if s.Run() != FailureState {
		t.Errorf("Selector failed success run")
	}
	if s.Run() != DoneState {
		t.Errorf("Selector failed done run after failure")
	}

	s.Reset()
	if !b1.IsReset || !b2.IsReset || !b3.IsReset {
		t.Errorf("Selector failed to reset children after faiure")
	}

	if s.Run() != RunningState {
		t.Errorf("Selector failed on running run")
	}
	if s.Run() != SuccessState {
		t.Errorf("Selector failed continue after running run")
	}
	if s.Run() != DoneState {
		t.Errorf("Seelctor failed done after running run")
	}
}

func TestConcurrentBehavior(t *testing.T) {
	b1 := &BehaviorStub{[]BehaviorState{SuccessState, SuccessState, DoneState}, false}
	b2 := &BehaviorStub{[]BehaviorState{FailureState, RunningState, SuccessState}, false}
	b3 := &BehaviorStub{[]BehaviorState{SuccessState, DoneState}, false}
	s := ConcurrentBehavior(b1, b2, b3)

	if s.Run() != FailureState {
		t.Errorf("Concurrent failed failure run")
	}
	if s.Run() != DoneState {
		t.Errorf("Concurrent done failed after failure run")
	}

	s.Reset()
	if !b1.IsReset || !b2.IsReset || !b3.IsReset {
		t.Errorf("Concurrent failed to reset after failure run")
	}

	if s.Run() != RunningState {
		t.Errorf("Concurrent failed running run")
	}

	if s.Run() != SuccessState {
		t.Errorf("Concurrent failed success run")
	}
	if s.Run() != DoneState {
		t.Errorf("Concurrent failed done after success run")
	}
}

type BehaviorUnique int

func (b BehaviorUnique) Reset() {}

func (b BehaviorUnique) Run() BehaviorState { return 0 }

func TestRandomBehavior(t *testing.T) {
	r := RandomBehavior().(*random)
	for i := 0; i < 10; i++ {
		r.Children = append(r.Children, BehaviorUnique(i))
	}
	RandSeed(0xFEE15BAD) // ensure test is deterministic
	for i := 0; i < 10; i++ {
		oldperm := r.Children
		r.Reset()
		newperm := r.Children
		if reflect.DeepEqual(oldperm, newperm) {
			t.Errorf("Random reset failed to shuffle")
		}
	}
}

// TestStonesBehavior covers a use case in Stones which was not working as
// intended.  The bug turned out to be a missing scheduler call instead of
// anything with behaviors, but the test was left.
func TestStonesBehavior(t *testing.T) {
	var calls [3]int
	tree := BehaviorTree(SelectBehavior(
		ConcurrentBehavior(
			BehaviorCondition(func() bool {
				calls[0]++
				return calls[0] < 4
			}),
			ActBehavior(func() BehaviorState {
				calls[1]++
				return RunningState
			}),
		),
		ActBehavior(func() BehaviorState {
			calls[2]++
			return SuccessState
		}),
	))
	entity := ComponentSlice{tree}

	cases := [][3]int{
		{1, 1, 0},
		{2, 2, 0},
		{3, 3, 0},
		{4, 3, 1},
		{5, 3, 2},
	}
	for _, expected := range cases {
		entity.Handle(&ActEvent{})
		if calls != expected {
			t.Errorf("Stones incorrect calls (%v != %v)", calls, expected)
		}
	}
}
