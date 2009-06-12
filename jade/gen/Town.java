package jade.gen;

import jade.core.World;

import java.awt.Color;

/**
 * This implementation of Gen takes a simple wilderness and adds a few
 * buildings.
 */
public class Town extends Wilderness
{
	private static final int MAX_SIZE = 9;
	private static final int MIN_SIZE = 3;

	protected Town()
	{
	}

	public void generate(World world, long seed)
	{
		super.generate(world, seed);
		for(int x = 0; x + MAX_SIZE < world.width - 2; x += MAX_SIZE)
			for(int y = 0; y + MAX_SIZE < world.height - 2; y += MAX_SIZE)
			{
				int x1 = dice.nextInt(x + 1, x + MAX_SIZE - MIN_SIZE - 1);
				int x2 = dice.nextInt(x1 + MIN_SIZE - 1, x + MAX_SIZE - 1);
				int y1 = dice.nextInt(y + 1, y + MAX_SIZE - MIN_SIZE - 1);
				int y2 = dice.nextInt(y1 + MIN_SIZE - 1, y + MAX_SIZE - 1);				
				for(int i = x1; i <= x2; i++)
					for(int j = y1; j <= y2; j++)
					world.tile(i, j).setTile('#', Color.white, false);
			}
	}
}
