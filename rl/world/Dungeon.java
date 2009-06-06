package rl.world;

import java.util.HashMap;
import java.util.Map;

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
		depth++;
	}
}
