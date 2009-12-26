package rl.magic;

import jade.core.Console;
import jade.util.Tools;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import rl.magic.Spell.Target;
import rl.magic.Weave.Effect;

public class Spellbook
{
	private List<Spell> spells;
	private HashSet<Effect> knownEffects;

	public Spellbook()
	{
		spells = new ArrayList<Spell>();
		knownEffects = new HashSet<Effect>();
		knownEffects.add(Effect.Fire);
	}

	public void learnEffect(Effect effect)
	{
		knownEffects.add(effect);
	}

	public List<Spell> getSpells()
	{
		return spells;
	}

	public void removeSpell(Spell spell)
	{
		spells.remove(spell);
	}

	public void makeSpell(Console console)
	{
		console.saveBuffer();
		console.clearBuffer();
		console.buffLine(0, "Prepare your spell:", Color.white);
		Spell spell = chooseEffect(console);
		console.recallBuffer();
		console.refreshScreen();
		if(spell != null)
			spells.add(spell);
	}

	private Spell chooseEffect(Console console)
	{
		Effect[] effects = knownEffects.toArray(new Effect[knownEffects.size()]);
		int i = 0;
		char key;
		Spell result;
		do
		{
			console.buffLine(1, "Choose effect: " + effects[i], Color.white);
			console.refreshScreen();
			key = console.getKey();
			i += key == 'l' ? 1 : key == 'h' ? -1 : 0;
			i = Tools.clampToRange(i, 0, effects.length - 1);
			if(key == '\n')
			{
				result = chooseMagnitude(console, effects[i]);
				if(result != null)
					return result;
			}
		}
		while(key != 27);
		return null;
	}

	private Spell chooseMagnitude(Console console, Effect effect)
	{
		int mag = 1;
		char key;
		Spell result;
		do
		{
			console.buffLine(2, "Choose magnitude: " + mag, Color.white);
			console.refreshScreen();
			key = console.getKey();
			mag += key == 'l' ? 1 : key == 'h' ? -1 : 0;
			mag = mag < 1 ? 1 : mag;
			if(key == '\n')
			{
				result = chooseDuration(console, effect, mag);
				if(result != null)
					return result;
			}
		}
		while(key != 27);
		return null;
	}

	private Spell chooseDuration(Console console, Effect effect, int mag)
	{
		int dur = 1;
		char key;
		Spell result;
		do
		{
			console.buffLine(3, "Choose duration: " + dur, Color.white);
			console.refreshScreen();
			key = console.getKey();
			dur += key == 'l' ? 1 : key == 'h' ? -1 : 0;
			dur = dur < 1 ? 1 : dur;
			if(key == '\n')
			{
				result = chooseTarget(console, effect, mag, dur);
				if(result != null)
					return result;
			}
		}
		while(key != 27);
		return null;
	}

	private Spell chooseTarget(Console console, Effect effect, int mag, int dur)
	{
		Target[] targets = Target.values();
		int i = 0;
		char key;
		do
		{
			console.buffLine(4, "Choose target: " + targets[i], Color.white);
			console.refreshScreen();
			key = console.getKey();
			i += key == 'l' ? 1 : key == 'h' ? -1 : 0;
			i = Tools.clampToRange(i, 0, targets.length - 1);
			if(key == '\n')
				return new Spell(effect, mag, dur, targets[i]);
		}
		while(key != 27);
		return null;
	}
}
