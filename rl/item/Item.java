package rl.item;

import jade.core.Actor;
import java.awt.Color;
import java.io.Serializable;

public abstract class Item extends Actor implements Serializable
{
	public enum Type
	{
		EQUIPMENT, SCROLL, POTION;
	};

	private final Type type;

	public Item(char face, Color color, Type type)
	{
		super(face, color);
		this.type = type;
	}

	public Type type()
	{
		return type;
	}
}
