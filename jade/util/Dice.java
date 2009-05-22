package jade.util;

import java.util.Random;

public class Dice extends Random
{
	public Dice()
	{
		super();
	}
	
	public Dice(long seed)
	{
		super(seed);
	}
	
	public int nextInt(int min, int max)
	{
		int range = Math.abs(max - min);
		return nextInt(range + 1) + Math.min(min, max);
	}
	
	public int diceXdY(int x, int y)
	{
		int sum = 0;
		for(int i = 0; i < x; i++)
			sum += nextInt(1, y);
		return sum;
	}
}