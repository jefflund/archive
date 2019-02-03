package gorl

import (
	"fmt"
	"regexp"
	"strings"
	"unicode"
)

// Log applies the gorl log formating language to create a LogEvent.
//
// The format specifiers include the following:
// 	%s - subject
// 	%o - object
// 	%v - verb
// 	%x - literal
// Additionally, verb literals may be included using the form <verb>.
//
// Each format specifier can be mapped to any arbitrary value, and is converted
// to a string by the fmt package. Consequently, format values should probably
// implement the fmt.Stringer interface to ensure that the values are correctly
// represented in the formatted string.
//
// Example usage:
// 	Log("%s <hit> %o", hero, bear) yields "You hit the bear."
// 	Log("%s %v %o", tiger, verb, hero) yields "The saber-tooth slashes you."
// 	Log("%s <hit> %o!", tiger, rabbit) yields "The saber-tooth hits the rabbit!"
// 	Log("%s %v %o?", bear, verb, bear) yields "The bear hits itself?"
// 	Log("%s <laugh>", unique) yields "Gorp laughs."
//
// Note that if the String conversion for a value is "you" so that the formatter
// knows which grammatical-person to use. Named monsters should have string
// representations which are capitalized so the formatter knows not to add
// certain articles to the names.
//
// Also note that if no ending punctuation is given, then a period is added
// automatically. The sentence is also capitalized if was not already.
func Log(s string, args ...interface{}) *LogEvent {
	var subject interface{}
	var subjectName string

	replace := func(match string) string {
		// If match is format specifier (as opposed to verb literal).
		if match[0] == '%' {
			// If no more args to consume, return missing format value string.
			if len(args) == 0 {
				return fmt.Sprintf("%%!%s(MISSING)", match[1:])
			}

			// Consume the next argument from args.
			arg := args[0]
			args = args[1:]
			// Handle the arg based on the format specifier.
			switch match {
			case "%s": // Subject.
				subject = arg
				subjectName = getName(subject)
				return subjectName
			case "%o": // Object.
				if arg == subject {
					return getReflexive(arg)
				}
				return getName(arg)
			case "%v": // Verb.
				return getVerb(arg, subjectName)
			case "%x": // Literal.
				return fmt.Sprintf("%v", arg)
			}
		}

		// Match was not a format specifier, so must be verb literal.
		return getVerb(match[1:len(match)-1], subjectName)
	}

	// Replace format specifiers with formatted arguments.
	s = strings.Replace(s, "%%", "%", -1)
	s = makeSentence(formatRE.ReplaceAllStringFunc(s, replace))

	// If we have remaining arguments, append an extra argument format value to
	// the result string.
	if len(args) > 0 {
		argstrs := make([]string, len(args))
		for i, arg := range args {
			argstrs[i] = fmt.Sprintf("%v", arg)
		}
		extra := strings.Join(argstrs, " ")
		s = fmt.Sprintf("%s%%!(EXTRA %s)", s, extra)
	}

	return &LogEvent{s}
}

// Name is a Component which reponds to NameEvent with a string name.
type Name string

// Process implements Component for Name.
func (c Name) Process(v Event) {
	switch v := v.(type) {
	case *NameEvent:
		v.Name = string(c)
	}
}

// Data needed by Fmt helper functions. These should be regarded as constants.
var (
	formatRE            = regexp.MustCompile("%s|%o|%v|%x|<.+?>")
	articles            = []string{"the", "a"}
	irregularVerbsFirst = map[string]string{
		"be": "am",
	}
	irregularVerbsSecond = map[string]string{
		"be": "are",
	}
	irregularVerbsThird = map[string]string{
		"do":     "does",
		"be":     "is",
		"have":   "has",
		"can":    "can",
		"cannot": "cannot",
		"could":  "could",
		"may":    "may",
		"must":   "must",
		"shall":  "shall",
		"should": "should",
		"will":   "will",
		"would":  "would",
	}
	esEndings      = []string{"ch", "sh", "ss", "x", "o"}
	endPunctuation = []string{".", "!", "?"}
)

// includesArticle returns true if the given name starts with an article.
func includesArticle(name string) bool {
	for _, article := range articles {
		if strings.HasPrefix(name, article+" ") {
			return true
		}
	}
	return false
}

// isUniqueName returns true if the name starts with an uppercase rune.
func isUniqueName(name string) bool {
	return unicode.IsUpper([]rune(name)[0])
}

// getArticle prepends the article 'the' when the name is both non-unique and
// does not already contain an article.
func getArticle(name string) string {
	if name == "I" || name == "you" || includesArticle(name) || isUniqueName(name) {
		return name
	}
	return "the " + name
}

// getName returns the string name for a particular noun. If the noun is an
// Entity, then a NameEvent is attempted. If that fails, then fmt.Sprintf
// is used instead. If needed, the article 'the' is prepended to the name.
func getName(noun interface{}) string {
	// Attempt NameEvent query on an Entity.
	if entity, ok := noun.(Entity); ok {
		v := NameEvent{}
		entity.Handle(&v)
		if v.Name != "" {
			return getArticle(v.Name)
		}
	}

	// Default to Sprintf if NameEvent fails.
	return getArticle(fmt.Sprintf("%v", noun))
}

// getReflexive turns a noun into a reflexive pronoun.
func getReflexive(noun interface{}) string {
	name := getName(noun)
	switch name {
	case "I":
		return "myself"
	case "you":
		return "yourself"
	default:
		return "itself"
	}
}

// conjuageSecond conjugates a verb in the first person tense.
func conjugateFirst(verb string) string {
	if conjugated, irregular := irregularVerbsFirst[verb]; irregular {
		return conjugated
	}
	return verb
}

// conjuageSecond conjugates a verb in the second person tense.
func conjugateSecond(verb string) string {
	if conjugated, irregular := irregularVerbsSecond[verb]; irregular {
		return conjugated
	}
	return verb
}

// conjugateThird conjugates a verb in the third person tense.
func conjugateThird(verb string) string {
	// Check if verb is irregular (e.g. be -> is).
	if congugated, irregular := irregularVerbsThird[verb]; irregular {
		return congugated
	}
	// Handle es endings (e.g. miss -> misses).
	for _, ending := range esEndings {
		if strings.HasSuffix(verb, ending) {
			return verb + "es"
		}
	}
	// Handle ies endings (e.g. fly -> flies).
	if strings.HasSuffix(verb, "y") {
		return verb[:len(verb)-1] + "ies"
	}
	// Default is s ending (e.g. hit -> hits).
	return verb + "s"
}

// getVerb conjugates a verb given a particular subject.
func getVerb(verb interface{}, subjectName string) string {
	phrase := strings.Fields(fmt.Sprintf("%v", verb))
	switch subjectName {
	case "I":
		phrase[0] = conjugateFirst(phrase[0])
	case "you":
		phrase[0] = conjugateSecond(phrase[0])
	default:
		phrase[0] = conjugateThird(phrase[0])
	}
	return strings.Join(phrase, " ")
}

// makeSentence ensures proper capitalization and punctuation.
func makeSentence(s string) string {
	// Capitalize the sentence.
	s = strings.ToUpper(s[:1]) + s[1:]
	// Check if we already have ending punctuation.
	for _, punctuation := range endPunctuation {
		if strings.HasSuffix(s, punctuation) {
			return s
		}
	}
	// If no ending punctuation, default to a period.
	return s + "."
}
