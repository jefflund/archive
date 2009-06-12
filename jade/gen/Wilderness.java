package jade.gen;

import jade.core.World;
import jade.util.Dice;
import java.awt.Color;

/**
 * This implemenatation of Gen generates a simple wilderness. It will be
 * surrounded in walls, with trees in about 1 in 20 tiles.
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

	protected Wilderness()
	{
		dice = new Dice();
	}

	public void generate(World world, long seed)
	{
		dice.setSeed(seed);
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				if(dice.nextFloat() < TREE_CHANCE)
				{
					Color color = dice.nextBoolean() ? TREE_COLOR1 : TREE_COLOR2;
					world.tile(x, y).setTile(TREE_CHAR, color, false);
				}
				else
				{
					Color color = dice.nextBoolean() ? OPEN_COLOR1 : OPEN_COLOR2;
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
	}
}
