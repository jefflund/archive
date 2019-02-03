package gorl

// BehaviorState indicates the result of running a Behavior.
type BehaviorState uint8

// BehaviorState constants used to indicate the result of running a Behavior.
const (
	SuccessState BehaviorState = iota
	FailureState
	RunningState
	DoneState
)

// Behavior is a node in a BehaviorTree. It could be a composite Behavior, a
// Behavior decorator, or a leaf node in the tree.
type Behavior interface {
	Reset()
	Run() BehaviorState
}

// BehaviorTree is a Component which runs a Behavior on an Act Event.
func BehaviorTree(b Behavior) Component {
	return ComponentFunc(func(v Event) {
		switch v.(type) {
		case *ActEvent:
			state := b.Run()
			if state != RunningState {
				b.Reset()
			}
		}
	})
}

// sequence is the unexported backing struct for a SequenceBehavior.
type sequence struct {
	Children []Behavior
	Curr     int
}

// SequenceBehavior is a composite Behavior which runs the children in order.
// SequenceBehavior succeeds if all its children run successfully, but
// immediately fails if any of its children fail. It is akin to the 'and'
// operator for Behavior.
func SequenceBehavior(children ...Behavior) Behavior {
	return &sequence{children, 0}
}

// Reset restarts the SequenceBehavior iteration, and resets its children.
func (b *sequence) Reset() {
	b.Curr = 0
	for _, child := range b.Children {
		child.Reset()
	}
}

// Run performs the SequenceBehavior.
func (b *sequence) Run() BehaviorState {
	// reached end of sequence, so we are done executing
	if b.Curr == len(b.Children) {
		return DoneState
	}

	for b.Curr < len(b.Children) {
		switch state := b.Children[b.Curr].Run(); state {
		case SuccessState:
			// Continue sequence execution with next child on success.
			b.Curr++
		case FailureState:
			// Short circuit sequence execution on child failure.
			b.Curr = len(b.Children)
			return FailureState
		case RunningState:
			// Pause execution if child has not finished.
			return RunningState
		default:
			// Done or unknown states terminate the sequence.
			b.Curr = len(b.Children)
			return state
		}
	}

	// All children succeeded, so sequence succeeds.
	return SuccessState
}

// selector is the unexported backing struct for the SelecteBehavioror.
type selector struct {
	Children []Behavior
	Curr     int
}

// SelectBehavior is a composite Behavior which runs the children in order.
// SelectBehavior immediately succeeds if any of of its children run
// successfully, and fails if all of its children fail. It is akin to the 'or'
// operator for Behavior.
func SelectBehavior(children ...Behavior) Behavior {
	return &selector{children, 0}
}

// Reset restarts the SelecteBehavioror iteration, and resets the children.
func (b *selector) Reset() {
	b.Curr = 0
	for _, child := range b.Children {
		child.Reset()
	}
}

// Run performs the SelectBehavioror.
func (b *selector) Run() BehaviorState {
	// After reaching end of select, so executing is complete.
	if b.Curr == len(b.Children) {
		return DoneState
	}

	for b.Curr < len(b.Children) {
		switch state := b.Children[b.Curr].Run(); state {
		case SuccessState:
			// Short circuit select execution on child success.
			b.Curr = len(b.Children)
			return SuccessState
		case FailureState:
			// Continue select execution with next child on failure.
			b.Curr++
		case RunningState:
			// Pause execution if child has not finished.
			return RunningState
		default:
			// Done or unknown states terminate the select.
			b.Curr = len(b.Children)
			return state
		}
	}

	// All children failed, so select fails.
	return FailureState
}

// concurrent is the unexported backing struct for the ConcurrentBehavior.
type concurrent struct {
	Children []Behavior
	Done     bool
}

// ConcurrentBehavior is a composite Behavior which runs each child in order
// but concurrently (not in parallel though). In other words, each call to Run
// will result in each child being run. ConcurrentBehavior immediately fails if
// any of its children fail, but succeeds if all of its children succeed. If
// any of the child Behavior are still running, then concurrent will continue
// to rerun all child Behaviors, which often does nothing for children in the
// done state.
func ConcurrentBehavior(children ...Behavior) Behavior {
	return &concurrent{children, false}
}

// Reset restarts the ConcurrentBehavior iteration, and resets the children.
func (b *concurrent) Reset() {
	b.Done = false
	for _, child := range b.Children {
		child.Reset()
	}
}

// Run performs the ConcurrentBehavior.
func (b *concurrent) Run() BehaviorState {
	// All children finished, so we are done executing.
	if b.Done {
		return DoneState
	}

	// Assume success unless a child indicates otherwise.
	state := SuccessState
	// Execute each child each time we run.
	for _, child := range b.Children {
		switch child.Run() {
		case FailureState:
			// A child failed, so short circuit execution.
			b.Done = true
			return FailureState
		case RunningState:
			// Update return state, but do not pause execution.
			state = RunningState
		}
	}

	// Continue running until all children succeed.
	if state == SuccessState {
		b.Done = true
	}
	return state
}

// random is the unexported backing struct for the RandomBehavior.
type random struct {
	selector
}

// RandomBehavior is a composite Behavior which runs each child in random
// order. It succeeds if any of its children succeed, and fails if all of its
// children fail. It is akin to the 'or' operator for Behavior, but with
// randomized order.
func RandomBehavior(children ...Behavior) Behavior {
	return &random{selector{children, 0}}
}

// Reset restarts the RandomBehavior iteration, and resets the children.
func (b *random) Reset() {
	// random is backed by a selector, so all we have to do is shuffle the
	// selector children, and let selector handle the rest.
	shuffled := make([]Behavior, len(b.Children))
	for i, j := range RandPerm(len(b.Children)) {
		shuffled[i] = b.Children[j]
	}
	b.Children = shuffled

	b.selector.Reset()
}

// inverted is the unexported backing struct for the InverterBehavior.
type inverter struct {
	Behavior
}

// InvertBehavior is a Behavior decorator that inverts the success and fail
// states. Any other state, including running and done are simply returned.
func InvertBehavior(child Behavior) Behavior {
	return &inverter{child}
}

// Run runs the InverterBehavior.
func (b *inverter) Run() BehaviorState {
	switch state := b.Behavior.Run(); state {
	case SuccessState:
		return FailureState
	case FailureState:
		return SuccessState
	default:
		return state
	}
}

// repeater is the unexported backing struct for the RepeatingBehavior.
type repeater struct {
	Behavior
}

// RepeatingBehavior is a Behavior decorator that repeatedly runs the child
// Behavior until it fails. Upon completion (i.e. the failure of the child),
// the RepeatingBehavior succeeds.
func RepeatingBehavior(child Behavior) Behavior {
	return &repeater{child}
}

// Run performs the RepeatingBehavior.
func (b *repeater) Run() BehaviorState {
	switch state := b.Behavior.Run(); state {
	case SuccessState:
		// Pause execution on success.
		b.Behavior.Reset()
		return RunningState
	case FailureState:
		// Short circuit on failure.
		return SuccessState
	default:
		// Done or unknown states pause execution.
		return state
	}
}

// BehaviorCondition is a leaf Behavior which can only succeed or fail. It
// succeeds if the underlying condition is true, and fails otherwise.
type BehaviorCondition func() bool

// Reset is a noop for the BehaviorCondition.
func (b BehaviorCondition) Reset() {}

// Run performs the BehaviorCondition.
func (b BehaviorCondition) Run() BehaviorState {
	if b() {
		return SuccessState
	}
	return FailureState
}

// ActBehavior is a leaf Behavior which should perform an action on the
// target Entity. The return value of the underlying function is used as the
// state of the Behavior.
type ActBehavior func() BehaviorState

// Reset is a noop for the ActBehavior.
func (b ActBehavior) Reset() {}

// Run performs the ActBehavior.
func (b ActBehavior) Run() BehaviorState {
	return b()
}
