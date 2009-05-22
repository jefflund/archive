package jade.gen;

import jade.core.World;
import jade.util.Dice;
import java.awt.Color;

public class SimpleDunGen implements Gen
{
	private Dice random;

	public SimpleDunGen()
	{
		random = new Dice();
	}

	public void generate(World world, long seed)
	{
		random.setSeed(seed);
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				world.tile(x, y).setTile('#', Color.white, false);
		int x1 = world.width / 2;
		int y1 = world.height / 2;
		int x2, y2;
		for(int i = 0; i < Math.min(world.height, world.width); i++)
		{
			x2 = random.nextInt(1, world.width - 2);
			y2 = random.nextInt(1, world.height - 2);
			while(x2 != x1 || y2 != y1)
			{
				world.tile(x1, y1).setTile('.', Color.white, true);
				if(random.nextBoolean())
					x1 += x2 - x1 < 0 ? -1 : (x2 - x1 > 0 ? 1 : 0);
				else
					y1 += y2 - y1 < 0 ? -1 : (y2 - y1 > 0 ? 1 : 0);
			}
		}
	}
}
