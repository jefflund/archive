package rl.world;

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
		for(int i = 0; i < depth; i++)
			levels[i] = new Level(dice.nextLong(), this);
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
		getLevel().addActor(player, Dice.dice);
		getLevel().appendMessage(player + " descends");
		return true;
	}

	public boolean ascend()
	{
		getLevel().removeActor(player);
		depth--;
		getLevel().addActor(player, Dice.dice);
		getLevel().appendMessage(player + " ascends");
		return true;
	}
}
