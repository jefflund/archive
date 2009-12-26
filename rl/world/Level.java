package rl.world;

import jade.core.Actor;
import jade.core.World;
import jade.gen.Gen.GenFactory;
import jade.util.ColoredChar;
import jade.util.Dice;
import rl.creature.Creature;
import rl.creature.Monster;
import rl.creature.Player;
import rl.creature.Monster.Prototype;
import rl.item.Item;
import rl.item.Item.Slot;
import rl.magic.Weave;

public class Level extends World
{
	private Dungeon dungeon;

	public Level(long seed, Dungeon dungeon)
	{
		super(80, 23);
		GenFactory.cellular().generate(this, seed);
		this.dungeon = dungeon;
		addActor(new Monster(Prototype.Dragon), Dice.dice);
		addActor(new Monster(Prototype.Ogre), Dice.dice);
		addActor(new Monster(Prototype.Orc), Dice.dice);

		addActor(new Item(Slot.Weapon, '|', null), Dice.dice);
		addActor(new Item(Slot.Armor, ']', null), Dice.dice);
		addActor(new Item(Slot.Scroll, '!', null), Dice.dice);
		addActor(new Item(Slot.Bow, '}', null), Dice.dice);
	}

	@Override
	public void addActor(Actor actor, int x, int y)
	{
		super.addActor(actor, x, y);
		if(actor instanceof Player)
			((Player)actor).calcFoV();
	}

	@Override
	public void tick()
	{
		player().act();
		for(Monster monster : getActors(Monster.class))
			monster.act();
		for(Weave weave : getActors(Weave.class))
			weave.act();
		for(Actor actor : getActors(Actor.class))
			retrieveMessages(actor);
		removeExpired();
	}

	@Override
	public ColoredChar look(int x, int y)
	{
		Actor actor = getActorAt(x, y, Creature.class);
		if(actor != null)
			return actor.look();
		actor = getActorAt(x, y, Item.class);
		if(actor != null)
			return actor.look();
		actor = getActorAt(x, y, Weave.class);
		if(actor != null)
			return actor.look();
		return super.look(x, y);
	}

	public Player player()
	{
		return dungeon.player();
	}

	public boolean descend()
	{
		return dungeon.descend();
	}

	public boolean ascend()
	{
		return dungeon.ascend();
	}
}
