package rl.item;

import jade.core.Actor;
import java.awt.Color;
import java.io.Serializable;
import rl.magic.Instant;

public class Item extends Actor implements Serializable
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

	@Override
	public void act()
	{
		if(enchant != null && held())
			enchant.doIt(x(), y(), world());
	}

	public Slot slot()
	{
		return slot;
	}
}
