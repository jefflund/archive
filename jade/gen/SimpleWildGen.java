package jade.gen;

import jade.core.World;
import jade.util.Dice;
import java.awt.Color;

public class SimpleWildGen implements Gen
{
	private Dice random;
	private static final char TREE_CHAR = '%';
	private static final Color TREE_COLOR2 = Color.green;
	private static final Color TREE_COLOR1 = Color.yellow;
	private static final char OPEN = '.';
	private static final Color OPEN_COLOR1 = Color.white;
	private static final Color OPEN_COLOR2 = Color.green;
	private static final float TREE_CHANCE = .05f;
	
	public SimpleWildGen()
	{
		random = new Dice();
	}

	public void generate(World world, long seed)
	{
		random.setSeed(seed);
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				if(random.nextFloat() < TREE_CHANCE)
				{
					Color color = random.nextBoolean() ? TREE_COLOR1 : TREE_COLOR2;
					world.tile(x, y).setTile(TREE_CHAR, color, false);
				}
				else
				{
					Color color = random.nextBoolean() ? OPEN_COLOR1 : OPEN_COLOR2;
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
