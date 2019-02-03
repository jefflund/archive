package gorl

import (
	"testing"
)

// needed for ExampleLog
var (
	mammoth    = "mammoth"
	attackVerb = "hit"
	ughNPC     = "Ugh"
	statusCold = "cold"
	dmg        = 3
)

func ExampleLog() {
	// Create a LogWidget to display on screen.
	journal := NewLogWidget(0, 0, 10, 10)

	// Obviously most Entity will be more complicated than this, but
	// nevertheless, our Entity now responds to LogEvent after we add the
	// LogWidget as a Component. See also LogWidget.NewLogger for a way to
	// create a logging Component which only logs if a condition function
	// returns true.
	entity := &ComponentSlice{}
	entity.AddComponent(journal.NewLogger())

	// Adding a Name to an Entity makes it format nicely in Log. Alternatively,
	// Entity can be made to implement fmt.String. Either way, making it so the
	// name of an Entity is "you" indicates to Log that the sentence should be
	// congugated in second person instead of third person.
	entity.AddComponent(Name("you"))

	// Handle LogEvent created with Log. These messages get added to the
	// LogWidget cache. Crucially, by handling these LogEvent, if any other
	// components on the Entity need to modify the LogEvent they can (as
	// opposed to directly calling LogWidget.Log).
	entity.Handle(Log("%s %v %o", entity, attackVerb, mammoth)) // yields something like "You hit the mammoth."
	entity.Handle(Log("%s <hit> %o", entity, ughNPC))           // yields something like "You hit Ugh."
	entity.Handle(Log("%s <be> %o", entity, statusCold))        // yields something like "You are cold."
	entity.Handle(Log("%s <deal> %x damage", entity, dmg))      // yields something like "You deal 3 damage."

	// Displays the above log messages. Normally done through a UIEvent passed
	// to a Screen containing the LogWidget, but here we trigger the update
	// manually.
	journal.Update()
}

type vals []interface{}

func NamedEntity(n string) Entity {
	return &ComponentSlice{Name(n)}
}

func TestLog(t *testing.T) {
	me := NamedEntity("I")
	you := NamedEntity("you")
	dog := NamedEntity("dog")
	Ugh := NamedEntity("Ugh")

	cases := []struct {
		s        string
		args     vals
		expected string
	}{
		// Basic SVO
		{"%s %v %o", vals{you, "hit", dog}, "You hit the dog."},
		{"%s %v %o", vals{"mammoth", "hit", dog}, "The mammoth hits the dog."},
		{"%s %v %o", vals{you, "hit", Ugh}, "You hit Ugh."},
		{"%s %v %o", vals{Ugh, "hit", you}, "Ugh hits you."},

		// Embedded verb
		{"%s <hit> %o", vals{you, dog}, "You hit the dog."},
		{"%s <hit> %o", vals{"mammoth", dog}, "The mammoth hits the dog."},
		{"%s <hit> %o", vals{you, Ugh}, "You hit Ugh."},
		{"%s <hit> %o", vals{Ugh, you}, "Ugh hits you."},

		// Verb phrases
		{"%s <scream loudly>", vals{you}, "You scream loudly."},
		{"%s <scream loudly>", vals{dog}, "The dog screams loudly."},

		// Irregular verbs
		{"%s <be cold>", vals{me}, "I am cold."},
		{"%s <be cold>", vals{you}, "You are cold."},
		{"%s <be cold>", vals{dog}, "The dog is cold."},
		{"%s <can eat>", vals{me}, "I can eat."},
		{"%s <can eat>", vals{you}, "You can eat."},
		{"%s <can cold>", vals{dog}, "The dog can cold."},
		{"%s <have> %o", vals{me, "stick"}, "I have the stick."},
		{"%s <have> %o", vals{you, "stick"}, "You have the stick."},
		{"%s <have> %o", vals{dog, "stick"}, "The dog has the stick."},

		// Reflexive
		{"%s <hit> %o", vals{me, me}, "I hit myself."},
		{"%s <hit> %o", vals{you, you}, "You hit yourself."},
		{"%s <hit> %o", vals{dog, dog}, "The dog hits itself."},
		{"%s <hit> %o", vals{Ugh, Ugh}, "Ugh hits itself."}, // gender?

		// End punctuation
		{"%s <hit> %o!", vals{you, dog}, "You hit the dog!"},
		{"%s <hit> %o!", vals{"mammoth", dog}, "The mammoth hits the dog!"},
		{"%s <hit> %o?", vals{you, Ugh}, "You hit Ugh?"},
		{"%s <hit> %o?", vals{Ugh, you}, "Ugh hits you?"},

		// Literals
		{"%s <hit> %o for %x", vals{you, dog, 3}, "You hit the dog for 3."},
		{"%s <hit> %o for %x", vals{"cat", dog, 3}, "The cat hits the dog for 3."},
		{"%s <hit> %o for %x", vals{you, Ugh, 3}, "You hit Ugh for 3."},
		{"%s <hit> %o for %x", vals{Ugh, you, 3}, "Ugh hits you for 3."},

		// Unique name with lower case words in it
		{"%s <hit> %o", vals{you, NamedEntity("Tigris of Gaul")}, "You hit Tigris of Gaul."},

		// Invalid argument number
		{"%s <hit> %o", vals{you}, "You hit %!o(MISSING)."},
		{"%s <hit> %o for %x", vals{you}, "You hit %!o(MISSING) for %!x(MISSING)."},
		{"%s <hit> %o", vals{you, "orc", "foobar"}, "You hit the orc.%!(EXTRA foobar)"},
		{"%s <hit> %o", vals{you, "orc", "foobar", 123}, "You hit the orc.%!(EXTRA foobar 123)"},

		// Percent specifier
		{"%s <hit> %o for %%%x%%", vals{you, "orc", 20}, "You hit the orc for %20%."},

		// Passive sentences
		{"%o <be hit> by %s", vals{"orc", "goblin"}, "The orc is hit by the goblin."},
	}
	for _, c := range cases {
		if actual := Log(c.s, c.args...).Msg; actual != c.expected {
			t.Errorf("Fmt(%s, %v) = %s != %s", c.s, c.args, actual, c.expected)
		}
	}
}
