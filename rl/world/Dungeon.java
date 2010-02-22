package rl.world;

import jade.gen.Gen;
import jade.gen.Gen.GenFactory;
import jade.util.Dice;
import rl.creature.Player;

public class Dungeon
{
	private Player player;
	private Level[] levels;
	private int depth;

	public Dungeon(int depth, int seed, Player player)
	{
		levels = new Level[depth];
		Dice dice = new Dice(seed);
		levels[0] = new Level(dice.nextLong(), this, GenFactory.wilderness(), 0);
		for(int i = 1; i < depth; i++)
		{
			Gen gen = dice.nextBoolean() ? GenFactory.traditional() : GenFactory.cellular();
			levels[i] = new Level(dice.nextLong(), this, gen, i);
		}
		this.player = player;
		levels[0].addActor(player, dice);
		this.depth = 0;
	}

	public Level getLevel()
	{
		return levels[depth];
	}

	public Player player()
	{
		return player;
	}

	public boolean descend()
	{
		getLevel().removeActor(player);
		depth++;
		getLevel().addActor(player, Dice.global);
		getLevel().appendMessage(player + " descends");
		return true;
	}

	public boolean ascend()
	{
		getLevel().removeActor(player);
		depth--;
		getLevel().addActor(player, Dice.global);
		getLevel().appendMessage(player + " ascends");
		return true;
	}
}
