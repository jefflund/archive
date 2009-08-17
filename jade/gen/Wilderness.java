package jade.gen;

import jade.core.World;
import jade.util.Dice;
import java.awt.Color;

/**
 * This implemenatation of Gen generates a simple wilderness. It will be
 * surrounded in walls, with trees in about 1 in 20 tiles. Optionally, rooms can
 * be added to the the wilderness.
 */
public class Wilderness implements Gen
{
	protected Dice dice;
	private static final char TREE_CHAR = '%';
	private static final Color TREE_COLOR2 = Color.green;
	private static final Color TREE_COLOR1 = Color.yellow;
	private static final char OPEN = '.';
	private static final Color OPEN_COLOR1 = Color.white;
	private static final Color OPEN_COLOR2 = Color.green;
	private static final float TREE_CHANCE = .05f;
	private static final int MAX_ROOM_SIZE = 9;
	private static final int MIN_ROOM_SIZE = 3;
	private final boolean rooms;

	protected Wilderness(boolean rooms)
	{
		dice = new Dice();
		this.rooms = rooms;
	}

	public void generate(World world, long seed)
	{
		dice.setSeed(seed);
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				if(dice.nextFloat() < TREE_CHANCE)
				{
					final Color color = dice.nextBoolean() ? TREE_COLOR1 : TREE_COLOR2;
					world.tile(x, y).setTile(TREE_CHAR, color, false);
				}
				else
				{
					final Color color = dice.nextBoolean() ? OPEN_COLOR1 : OPEN_COLOR2;
					world.tile(x, y).setTile(OPEN, color, true);
				}
		for(int x = 0; x < world.width; x++)
		{
			world.tile(x, 0).setTile('#', OPEN_COLOR1, false);
			world.tile(x, world.height - 1).setTile('#', OPEN_COLOR1, false);
		}
		for(int y = 0; y < world.height; y++)
		{
			world.tile(0, y).setTile('#', OPEN_COLOR1, false);
			world.tile(world.width - 1, y).setTile('#', OPEN_COLOR1, false);
		}
		if(rooms)
			addRooms(world);
	}

	private void addRooms(World world)
	{
		for(int x = 0; x + MAX_ROOM_SIZE < world.width - 2; x += MAX_ROOM_SIZE)
			for(int y = 0; y + MAX_ROOM_SIZE < world.height - 2; y += MAX_ROOM_SIZE)
			{
				final int x1 = dice.nextInt(x + 1, x + MAX_ROOM_SIZE - MIN_ROOM_SIZE
						- 1);
				final int x2 = dice.nextInt(x1 + MIN_ROOM_SIZE - 1, x + MAX_ROOM_SIZE
						- 1);
				final int y1 = dice.nextInt(y + 1, y + MAX_ROOM_SIZE - MIN_ROOM_SIZE
						- 1);
				final int y2 = dice.nextInt(y1 + MIN_ROOM_SIZE - 1, y + MAX_ROOM_SIZE
						- 1);
				for(int i = x1; i <= x2; i++)
					for(int j = y1; j <= y2; j++)
						world.tile(i, j).setTile('#', Color.white, false);
			}
	}
}
