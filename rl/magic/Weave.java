package rl.magic;

import jade.core.Actor;
import java.awt.Color;
import java.io.Serializable;
import rl.creature.Creature;
import rl.magic.Instant.Effect;

public class Weave extends Actor implements Serializable
{
	private Instant instant;
	private int duration;
	
	public Weave(Effect effect, int magnitude, int duration)
	{
		super('*', Color.red);
		instant = new Instant(effect, magnitude);
		this.duration = duration;
	}

	public void act()
	{
		Creature target = world().getActorAt(x(), y(), Creature.class);
		if(target != null)
			instant.doIt(target);
		if(duration == 0)
			expire();
		duration--;
	}
}
