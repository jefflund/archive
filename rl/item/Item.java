package rl.item;

import jade.core.Actor;
import java.awt.Color;
import java.io.Serializable;
import rl.creature.Creature;

public class Item extends Actor implements Serializable
{
	public enum Slot
	{
		WEAPON, ARMOR
	};

	private Slot slot;
	private int modifier;

	public Item(char face, Color color, Slot slot, int modifier)
	{
		super(face, color);
		this.slot = slot;
		this.modifier = modifier;
	}

	@Override
	public void act()
	{
	}
	
	public void onEquip(Creature owner)
	{
		switch(slot)
		{
		case ARMOR:
			owner.def().buff(modifier);
			break;
		case WEAPON:
			owner.dmg().buff(modifier);
			break;
		}
	}
	
	public void onUnequip(Creature owner)
	{
		modifier *= -1;
		onEquip(owner);
		modifier *= -1;
	}

	public Slot slot()
	{
		return slot;
	}
}
