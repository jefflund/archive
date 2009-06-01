package rl.world;

import jade.core.Actor;
import jade.core.World;
import rl.creature.Creature;

public class Level extends World
{
	public Level()
	{
		super(80, 24);
	}

	public void tick()
	{
		for(Actor creature : getActors(Creature.class))
			creature.act();
		for(Actor creature : getActors(Creature.class))
			retrieveMessages(creature);
		removeExpired();
	}
}
