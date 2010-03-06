package rl.world;

import jade.core.Actor;
import jade.core.World;
import jade.gen.Gen;
import jade.util.Dice;
import jade.util.type.ColoredChar;
import java.util.List;
import rl.creature.Creature;
import rl.creature.Monster;
import rl.creature.Player;
import rl.item.Item;
import rl.magic.Weave;

public class Level extends World
{
	private Dungeon dungeon;

	public Level(long seed, Dungeon dungeon, Gen gen, int depth)
	{
		super(80, 23);
		gen.generate(this, seed);
		this.dungeon = dungeon;
		Prototype prototype = dungeon.prototype();
		for(int i = 0; i < 4; i++)
			addActor(prototype.getMonster(depth), Dice.global);
		for(int i = 0; i < 4; i++)
			addActor(prototype.getItem(depth), Dice.global);
		Monster unique = prototype.getUnique(depth);
		if(unique != null)
			unique.attachBossScript();
		addActor(unique, Dice.global);
		Item artifact = prototype.getArtifact(depth);
		if(artifact != null)
			artifact.attachArtifactScript();
		addActor(artifact, Dice.global);
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
