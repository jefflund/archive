package rl.world;

import jade.core.Actor;
import jade.core.World;
import jade.gen.Gen;
import jade.gen.Gen.GenFactory;
import jade.util.ColoredChar;
import java.awt.Color;
import java.util.Random;
import rl.creature.Creature;
import rl.creature.Monster;
import rl.creature.Player;
import rl.item.Item;
import rl.item.Item.Slot;
import rl.magic.Weave;

public class Level extends World
{
	private Player player;

	public Level(int depth)
	{
		super(80, 23);
		int algorithm = depth == 0 ? Gen.Town : Gen.Cellular;
		GenFactory.get(algorithm).generate(this, depth);
		Random random = new Random(0);
		addActor(new Monster('D', Color.red), random);
		addActor(new Item('|', Color.white, Slot.Weapon), random);
		addActor(new Item(']', Color.white, Slot.Weapon), random);
		addActor(new Item('|', Color.gray, Slot.Weapon), random);
		addActor(new Item(']', Color.gray, Slot.Weapon), random);
		addActor(new Feature('^', Color.blue), random);		
	}

	public void addActor(Actor actor, int x, int y)
	{
		super.addActor(actor, x, y);
		if(actor instanceof Player)
		{
			player = (Player)actor;
			player.calcFoV();
		}
	}

	public Player player()
	{
		return player;
	}

	public void tick()
	{
		player.act();
		for(Actor monster : getActors(Monster.class))
			monster.act();
		for(Actor weave : getActors(Weave.class))
			weave.act();
		for(Actor feature : getActors(Feature.class))
			feature.act();
		for(Actor actor : getActors(Actor.class))
			retrieveMessages(actor);
		removeExpired();
	}

	public ColoredChar look(int x, int y)
	{
		Actor actor = null;
		actor = actor == null ? getActorAt(x, y, Creature.class) : actor;
		actor = actor == null ? getActorAt(x, y, Weave.class) : actor;
		actor = actor == null ? getActorAt(x, y, Item.class) : actor;
		actor = actor == null ? getActorAt(x, y, Feature.class) : actor;
		if(actor == null)
			return super.look(x, y);
		return actor.look();
	}
}
