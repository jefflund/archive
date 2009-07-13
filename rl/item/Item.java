package rl.item;

import jade.core.Actor;
import java.awt.Color;
import java.io.Serializable;

public class Item extends Actor implements Serializable
{
	public enum Slot
	{
		Weapon, Armor
	};

	private Slot slot;

	public Item(char face, Color color, Slot slot)
	{
		super(face, color);
		this.slot = slot;
	}

	@Override
	public void act()
	{
	}

	public Slot slot()
	{
		return slot;
	}
}
