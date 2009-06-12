package jade.gen;

import jade.core.World;
import jade.util.Dice;
import java.awt.Color;

public class Traditional implements Gen
{
	private static final int MAX_SIZE = 10;
	private static final int MIN_SIZE = 4;
	private Dice dice;
	
	protected Traditional()
	{
		dice = new Dice();
	}
	
	public void generate(World world, long seed)
	{
		dice.setSeed(seed);
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				world.setTile(x, y, '#', Color.white, false);
		for(int x = 0; x + MAX_SIZE < world.width - 2; x += MAX_SIZE)
			for(int y = 0; y + MAX_SIZE < world.height - 2; y += MAX_SIZE)
			{
				int x1 = dice.nextInt(x + 1, x + MAX_SIZE - MIN_SIZE - 1);
				int x2 = dice.nextInt(x1 + MIN_SIZE - 1, x + MAX_SIZE - 1);
				int y1 = dice.nextInt(y + 1, y + MAX_SIZE - MIN_SIZE - 1);
				int y2 = dice.nextInt(y1 + MIN_SIZE - 1, y + MAX_SIZE - 1);				
				for(int i = x1; i <= x2; i++)
					for(int j = y1; j <= y2; j++)
					world.setTile(i, j, '.', Color.white, true);
			}
	}
}
