package rl.magic;

import jade.core.Actor;
import java.awt.Color;
import rl.creature.Monster;

public class Weave extends Actor
{
	private int duration;

	public Weave(int duration)
	{
		super('*', Color.red);
		this.duration = duration;
	}

	public void act()
	{
		duration--;
		Actor monster = world().getActorAt(x(), y(), Monster.class);
		if(monster != null)
		{
			monster.expire();
			appendMessage(monster + " disappears in a flash");
		}
		if(duration < 0)
			expire();
	}
}
