package jade.gen;

import jade.core.World;
import jade.util.Dice;
import java.awt.Color;

public class SimpleWildGen implements Gen
{
	private Dice random;
	private static final float treeChance = .05f;

	public SimpleWildGen()
	{
		random = new Dice();
	}

	public void generate(World world, long seed)
	{
		random.setSeed(seed);
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				if(random.nextFloat() < treeChance)
				{
					Color color = random.nextBoolean() ? Color.yellow : Color.green;
					world.tile(x, y).setTile('%', color, false);
				}
				else
				{
					Color color = random.nextBoolean() ? Color.white : Color.green;
					world.tile(x, y).setTile('.', color, true);
				}
		for(int x = 0; x < world.width; x++)
		{
			world.tile(x, 0).setTile('#', Color.white, false);
			world.tile(x, world.height - 1).setTile('#', Color.white, false);
		}
		for(int y = 0; y < world.height; y++)
		{
			world.tile(0, y).setTile('#', Color.white, false);
			world.tile(world.width - 1, y).setTile('#', Color.white, false);
		}
	}
}
