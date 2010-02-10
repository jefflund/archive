package rl.world;

import jade.core.Actor;
import jade.core.Console;
import java.awt.Color;

public abstract class Script extends Actor
{
	public Script()
	{
		super(' ', Color.black);
	}
	
	protected Console console()
	{
		return ((Level)world()).player().console();
	}
}
