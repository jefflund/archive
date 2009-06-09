package rl.magic;

import jade.core.Actor;
import java.awt.Color;
import rl.creature.Creature;

public class Weave extends Actor
{
	private Instant instant;
	private int duration;
	private boolean activated;

	public Weave(Instant instant, int duration)
	{
		super('*', Color.red);
		this.instant = instant;
		this.duration = duration;
		activated = false;
	}

	public void act()
	{
		Creature target = world().getActorAt2(x(), y(), Creature.class); 
		activated = activated ? true : instant.doIt(target);
		if(activated)
			duration--;
		if(duration < 0)
			expire();
	}
}
