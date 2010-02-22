package rl.world;

import jade.core.Actor;
import jade.core.World;
import jade.gen.Gen;
import jade.util.ColoredChar;
import jade.util.Dice;
import java.util.List;
import rl.creature.Creature;
import rl.creature.Monster;
import rl.creature.Player;
import rl.item.Item;
import rl.item.Item.Slot;
import rl.magic.Weave;
import rl.prototype.Prototype;

public class Level extends World
{
	private Dungeon dungeon;

	public Level(long seed, Dungeon dungeon, Gen gen)
	{
		super(80, 23);
		gen.generate(this, seed);
		this.dungeon = dungeon;
		for(int i = 0; i < 4; i++)
			addActor(new Monster(Prototype.getAny()), Dice.global);

		addActor(new Item(Slot.Weapon, '|', null), Dice.global);
		addActor(new Item(Slot.Armor, ']', null), Dice.global);
		addActor(new Item(Slot.Scroll, '!', null), Dice.global);
		addActor(new Item(Slot.Scroll, '!', null), Dice.global);
		addActor(new Item(Slot.Scroll, '!', null), Dice.global);
		addActor(new Item(Slot.Scroll, '!', null), Dice.global);
		addActor(new Item(Slot.Scroll, '!', null), Dice.global);
		addActor(new Item(Slot.Bow, '}', null), Dice.global);
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
		for(Script script : getActors(Script.class))
			script.act();
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
	public List<ColoredChar> lookAll(int x, int y)
	{
		List<ColoredChar> look = super.lookAll(x, y);
		Actor actor = getActorAt(x, y, Weave.class);
		if(actor != null)
			look.add(actor.look());
		actor = getActorAt(x, y, Item.class);
		if(actor != null)
			look.add(actor.look());
		actor = getActorAt(x, y, Creature.class);
		if(actor != null)
			look.add(actor.look());
		return look;
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
