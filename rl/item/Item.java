package rl.item;

import jade.core.Actor;
import java.awt.Color;
import java.io.Serializable;
import rl.creature.Creature;

public class Item extends Actor implements Serializable
{
	public enum Type
	{
		WEAPON(true), ARMOR(true), SCROLL(false), POTION(false);
		
		private boolean equipable;
		
		private Type(boolean equipable)
		{
			this.equipable = equipable;
		}
	};

	private Type type;
	private int modifier;

	public Item(char face, Color color, Type slot, int modifier)
	{
		super(face, color);
		this.type = slot;
		this.modifier = modifier;
	}

	@Override
	public void act()
	{
	}
	
	public void onEquip(Creature owner)
	{
		switch(type)
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

	public Type type()
	{
		return type;
	}
	
	public boolean equipable()
	{
		return type.equipable;
	}
}
