package rl.world;

import jade.core.Actor;
import java.awt.Color;
import java.io.Serializable;
import rl.creature.Creature;
import rl.magic.Instant;
import rl.magic.Instant.Effect;

public class Feature extends Actor implements Serializable
{
	private final Instant instant;

	public Feature(char face, Color color, Effect effect, int magnitude)
	{
		super(face, color);
		assert (!effect.undoNeeded());
		instant = new Instant(effect, magnitude);
	}

	@Override
	public void act()
	{
		if(world().getActorAt(x(), y(), Creature.class) != null)
		{
			if(instant != null)
				instant.doIt(x(), y(), world());
			expire();
		}
	}
}
