package gorl

import (
	"math"
)

// deltanode stores Entity events for a particular delta in a DeltaClock.
type deltanode struct {
	delta  float64
	link   *deltanode
	events map[Entity]struct{}
}

// DeltaClock implements a data structure which allows for fast scheduling.
// The DeltaClock is essentially a linked list in which each node stores a
// collection of Entity events, and a delta (in time) until the next node.
// Since each node stores the time until the next node, the total time until
// any one node is the result of the sum of the deltas of all the previous
// nodes. Consequently, advancing the clock can be done in O(1) time by simply
// decrementing or removing the head node. Adding new events can be done in
// O(n) time, where n is the number of nodes (not the number of events).
type DeltaClock struct {
	head  *deltanode
	nodes map[Entity]*deltanode
}

// NewDeltaClock creates an empty DeltaClock.
func NewDeltaClock() *DeltaClock {
	return &DeltaClock{nil, make(map[Entity]*deltanode)}
}

// Schedule adds an Entity to the queue at the given delta. Note that the delta
// is split into its integer and fractional parts. The integer part is used to
// determine the amount of delay, while the fractional part is only used to
// ensure a unique scheduling delta. If the Entity was previous scheduled, it
// will be removed from its delta.
//
// As an example, suppose we repeatedly schedule event A with deltas of 1,
// event B with deltas of 1.5, and event C with deltas of 2. It is not the
// case that A will fire 3 times for every 2 times B fires. Instead, both A and
// B will fire at the same rate, but A will always go first as it has a lower
// fractional part in its delta. It is the case that A and B will fire twice
// as often as C.
func (c *DeltaClock) Schedule(e Entity, delta float64) {
	c.Unschedule(e)

	var prev, curr *deltanode = nil, c.head

	// Iterate over nodes, ensuring we haven't gone passed the end,
	// or passed the desired node.
	for curr != nil && delta > curr.delta {
		delta -= math.Trunc(curr.delta)
		prev, curr = curr, curr.link
	}

	var node *deltanode
	if curr != nil && delta == curr.delta {
		// If the desired node already exists, just reuse it.
		node = curr
	} else {
		// Desired node didn't exist, so create it, with a link to curr node.
		node = &deltanode{delta, curr, make(map[Entity]struct{})}

		if prev == nil {
			// prev == nil iff we're at the beginning of the list.
			c.head = node
		} else {
			// Otherwise, insert the node in the middle of this list.
			prev.link = node
		}

		// The next node needs to take the new node's delta into account note
		// that the next node delta will always be less than the curr due to
		// the way we iterated over nodes.
		if curr != nil {
			curr.delta -= math.Trunc(delta) // Only subtract schedule time.
		}
	}

	// Add the event to the node.
	node.events[e] = struct{}{}
	c.nodes[e] = node
}

// Unschedule removes an Entity from the queue. If an Entity is in a delta
// which has already been advanced, it will be removed from that delta. If the
// Entity is not in a delta, no action is taken.
func (c *DeltaClock) Unschedule(e Entity) {
	if node, ok := c.nodes[e]; ok {
		delete(node.events, e)
		delete(c.nodes, e)
	}
}

// Advance returns the next set of Entity scheduled. If all Events have been
// removed from the next delta, then the set will be empty. If no deltas are
// remaining, then the result is nil. The Entity to delta mapping is left
// unchanged, so each Entity in the returned Entity set may still be
// unscheduled or rescheduled.
func (c *DeltaClock) Advance() map[Entity]struct{} {
	// No events were scheduled.
	if c.head == nil {
		return nil
	}

	// Get the events in the head to return, and then advance the head node.
	events := c.head.events
	c.head = c.head.link
	return events
}

// Tick advances the clock, and sends an ActEvent to each of the Entity in the
// next delta. No reschedules are made, so the Entity actions should include a
// call to Schedule if needed.
func (c *DeltaClock) Tick() {
	for e := range c.Advance() {
		e.Handle(&ActEvent{})
	}
}

// Delta returns the schedule delta for an Entity. The ok bool indicates
// whether the Entity was actually scheduled or not.
func (c *DeltaClock) Delta(e Entity) (delta float64, ok bool) {
	node, ok := c.nodes[e]
	if !ok {
		return 0, false
	}

	// Iterate through the node list to check if node delta already advanced.
	curr := c.head
	delta = curr.delta
	for curr != node {
		curr = curr.link

		// We've gone off end of the deltanode list so entity is not scheduled
		// this happens when an entity is a deltanode which has already been
		// returned by a call to Advance.
		if curr == nil {
			return 0, false
		}

		delta += math.Trunc(curr.delta)
	}

	// Found the delta, so the Entity is scheduled.
	return delta, true
}

// NewScheduler creates a Component which responds to ScheduleEvent and
// UnscheduleEvent in order to schedule the given Entity with the DeltaClock.
// It is intended to be added to the given Entity so that when that Entity
// receives scheduling Event it will respond properly in relation to the
// DeltaClock. The offset is added to the delta of the ScheduleEvent to
// schedule the given Entity and is meant to be used in order to automatically
// give an Entity a fractional part to their delta in order to enforce move
// order.
func (c *DeltaClock) NewScheduler(e Entity, offset float64) Component {
	return ComponentFunc(func(v Event) {
		switch v := v.(type) {
		case *ScheduleEvent:
			c.Schedule(e, v.Delta+offset)
		case *UnscheduleEvent:
			c.Unschedule(e)
		}
	})
}
