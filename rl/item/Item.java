package rl.item;

import jade.core.Actor;
import java.awt.Color;
import rl.creature.Creature;
import rl.magic.Instant;

public class Item extends Actor
{
	public enum Slot 
	{
		Weapon, Armor
	};
	
	private Slot slot;
	private Instant enchant;
	
	public Item(char face, Color color, Slot slot, Instant enchant)
	{
		super(face, color);
		this.slot = slot;
		this.enchant = enchant;
	}

	public void act()
	{
		if(enchant != null && held())
			enchant.doIt(world().getActorAt(x(), y(), Creature.class));
	}
	
	public Slot slot()
	{
		return slot;
	}
}
