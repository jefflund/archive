package rl.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import rl.creature.Player;

public class Dungeon
{
	private int depth;
	private Map<Integer, Level> levels;
	
	public Dungeon()
	{
		depth = 0;
		levels = new HashMap<Integer, Level>();
	}
	
	public Level getLevel()
	{
		Level level = levels.get(depth);
		if(level == null)
		{
			level = new Level(depth);
			levels.put(depth, level);
		}
		return level;
	}
	
	public void descend()
	{
		changeLevel(depth + 1);
	}
	
	public void ascend()
  {
		if(depth != 0)
			changeLevel(depth - 1);
  }
	
	private void changeLevel(int newDepth)
	{
		Player player = getLevel().player();
		getLevel().removeActor(player);
		depth = newDepth;
		getLevel().addActor(player, new Random(0));
	}
}
