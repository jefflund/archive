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
import rl.item.Item.Slot;
import rl.magic.Weave;

public class Level extends World implements Serializable
{
	private Player player;
	private Coord upStairs;
	private Coord downStairs;

	public Level(int depth)
	{
		super(80, 22);
		Dice random = new Dice(depth);
		int algorithm = depth == 0 ? GenFactory.Town : GenFactory.Traditional;
		GenFactory.get(algorithm).generate(this, depth);
		upStairs = depth > 0 ? getOpenTile(random) : null;
		if(upStairs != null)
			tile(upStairs).setTile('<', Color.white, true);
		downStairs = getOpenTile(random);
		tile(downStairs).setTile('>', Color.white, true);
		addActor(new Monster('D', Color.red), random);
		addActor(new Item('|', Color.white, Slot.Weapon), random);
		addActor(new Item(']', Color.white, Slot.Armor), random);
		addActor(new Item('|', Color.gray, Slot.Weapon), random);
		addActor(new Item(']', Color.gray, Slot.Armor), random);
		addActor(new Feature('^', Color.blue), random);
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
