package rl.world;

import jade.core.Actor;
import java.awt.Color;
import java.io.Serializable;
import rl.creature.Creature;

public class Feature extends Actor implements Serializable
{
	public Feature(char face, Color color)
	{
		super(face, color);
	}

	@Override
	public void act()
	{
		Creature target = world().getActorAt(x(), y(), Creature.class);
		if(target != null)
		{
			target.expire();
			appendMessage(target + " dies in a trap");
		}
	}
}
