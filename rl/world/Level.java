package rl.world;

import jade.core.Actor;
import jade.core.World;
import jade.gen.Gen.GenFactory;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Dice;
import java.awt.Color;
import java.io.Serializable;
import rl.creature.Creature;
import rl.creature.Monster;
import rl.creature.Player;
import rl.item.Item;
import rl.item.Item.Type;
import rl.magic.Weave;
import rl.magic.Instant.Effect;

public class Level extends World implements Serializable
{
	private Player player;
	private Coord upStairs;
	private Coord downStairs;

	public Level(int depth)
	{
		super(20, 20);
		Dice random = new Dice(depth);
		int algorithm = depth == 0 ? GenFactory.Town : GenFactory.Traditional;
		GenFactory.get(algorithm).generate(this, depth);
		upStairs = depth > 0 ? getOpenTile(random) : null;
		if(upStairs != null)
			tile(upStairs).setTile('<', Color.white, true);
		downStairs = getOpenTile(random);
		tile(downStairs).setTile('>', Color.white, true);
		addActor(new Monster('D', Color.red), random);
		addActor(new Item('|', Color.white, Type.WEAPON, 1000, null, 0), random);
		addActor(new Item(']', Color.white, Type.ARMOR, 10, null, 0), random);
		addActor(new Item('|', Color.red, Type.WEAPON, 1000, Effect.CHANNEL, 50), random);
		addActor(new Item(']', Color.red, Type.ARMOR, 10, Effect.CHANNEL, 150), random);		
		addActor(new Item('?', Color.white, Type.SCROLL, 0, null, 0), random);
		addActor(new Item('?', Color.red, Type.SCROLL, 7, Effect.FIRE, 6), random);
		addActor(new Item('!', Color.red, Type.POTION, 5, Effect.FIRE, 7), random);
		addActor(new Item('!', Color.white, Type.POTION, 0, null, 0), random);
		addActor(new Feature('^', Color.red, Effect.FIRE, 15), random);
		addActor(new Feature('^', Color.blue, Effect.CHANNEL, 90), random);
	}

	@Override
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

	public Coord upStairs()
	{
		return upStairs;
	}

	public Coord downStairs()
	{
		return downStairs;
	}

	@Override
	public void tick()
	{
		player.act();
		for(Monster monster : getActors(Monster.class))
			monster.act();
		for(Weave weave : getActors(Weave.class))
			weave.act();
		for(Feature feature : getActors(Feature.class))
			feature.act();
		for(Actor actor : getActors(Actor.class))
			retrieveMessages(actor);
		removeExpired();
	}

	@Override
	public ColoredChar look(int x, int y)
	{
		Creature creature = getActorAt(x, y, Creature.class);
		if(creature != null)
			return creature.look();
		Weave weave = getActorAt(x, y, Weave.class);
		if(weave != null)
			return weave.look();
		Item item = getActorAt(x, y, Item.class);
		if(item != null)
			return item.look();
		Feature feature = getActorAt(x, y, Feature.class);
		if(feature != null)
			return feature.look();
		return super.look(x, y);
	}
}
