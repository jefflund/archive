package rl.world;

import jade.util.Coord;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import rl.creature.Player;

public class Dungeon implements Serializable
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
		Player player = getLevel().player();
		if(getLevel().tile(player.x(), player.y()).look().ch() != '>')
			player.appendMessage("No stairs here");
		else
			changeLevel(depth + 1);
	}

	public void ascend()
	{
		Player player = getLevel().player();
		if(getLevel().tile(player.x(), player.y()).look().ch() != '<')
			player.appendMessage("No stairs here");
		else
			changeLevel(depth - 1);
	}

	private void changeLevel(int newDepth)
	{
		boolean goingDown = newDepth > depth;
		Player player = getLevel().player();
		getLevel().removeActor(player);
		depth = newDepth;
		Coord pos = goingDown ? getLevel().upStairs() : getLevel().downStairs();
		getLevel().addActor(player, pos);
	}
}
