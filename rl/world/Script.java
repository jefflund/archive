package rl.world;

import jade.core.Actor;
import jade.core.Console;
import java.awt.Color;
import rl.creature.Player;

public abstract class Script extends Actor
{
	public Script()
	{
		super(' ', Color.black);
	}
	
	protected Console console()
	{
		return player().console();
	}
	
	protected Player player()
	{
		return ((Level)world()).player();
	}
}
