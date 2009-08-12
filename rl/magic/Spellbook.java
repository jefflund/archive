package rl.magic;

import java.util.LinkedList;
import java.util.List;

public class Spellbook
{
	private List<Spell> spells;
	
	public Spellbook()
	{
		spells = new LinkedList<Spell>();
	}
	
	public void add(Spell spell)
	{
		spells.add(spell);
	}
	
	public List<Spell> spells()
	{
		return spells;
	}
}
