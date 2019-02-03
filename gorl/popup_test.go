package gorl

func Example_popup() {
	// This example demonstrates various popups and menus.
	TermMustInit()
	defer TermDone()

	// PopupTextdump is used to display things like help files.
	PopupTextdump{
		Title: "==== This is a title ==============",
		Lines: []string{
			"0. This is a line",
			"1. This is a line",
			"2. This is a line",
			"3. This is a line",
			"4. This is a line",
			"5. This is a line",
			"6. This is a line",
			"7. This is a line",
			"8. This is a line",
			"9. This is a line",
		},
		Height: 5,

		DoneKeys:       KeySet(KeyEsc),
		ScrollDownKeys: KeySet('j'),
		ScrollUpKeys:   KeySet('k'),

		TitleColor: ColorYellow,
	}.Display()
}
