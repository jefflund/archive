package rl.item;

import jade.core.Actor;
import java.awt.Color;

public class Item extends Actor
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

	public void act()
	{
	}
	
	public Slot slot()
	{
		return slot;
	}
}
