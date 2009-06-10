package jade.util;

import java.util.Random;

/**
 * Dice basically extends Random and adds two methods. Eventually, I may
 * reimplement Random as a Mersenne Twister.
 */
public class Dice extends Random
{
	/**
	 * Constructs a new Dice with a seed based on the current time.
	 */
	public Dice()
	{
		super();
	}

	/**
	 * Constructs a new Dice with a user provided seed.
	 * @param seed the pseudorandom generator seed
	 */
	public Dice(long seed)
	{
		super(seed);
	}

	/**
	 * Returns a random integer between the min and max inclusive. Actually, it
	 * doesnt matter the order of the two parameters.
	 * @param min the minimum result
	 * @param max the maximum result
	 * @return a random integer between the min and max inclusive.
	 */
	public int nextInt(int min, int max)
	{
		int range = Math.abs(max - min);
		return nextInt(range + 1) + Math.min(min, max);
	}
	
	/**
	 * Performs a 
	 * @param x
	 * @param y
	 * @return
	 */
	public int diceXdY(int x, int y)
	{
		int sum = 0;
		for(int i = 0; i < x; i++)
			sum += nextInt(1, y);
		return sum;
	}
}