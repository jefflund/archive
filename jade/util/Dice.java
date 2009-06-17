package jade.util;

import java.io.Serializable;
import java.util.Random;

/**
 * Dice basically extends Random and adds two methods. Eventually, I may
 * reimplement Random as a Mersenne Twister.
 */
public class Dice extends Random implements Serializable
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
	 * 
	 * @param seed the pseudorandom generator seed
	 */
	public Dice(long seed)
	{
		super(seed);
	}

	/**
	 * Returns a random integer between the min and max inclusive. In order for
	 * this method to work, min must be less than max.
	 * 
	 * @param min the minimum result
	 * @param max the maximum result
	 * @return a random integer between the min and max inclusive.
	 */
	public int nextInt(int min, int max)
	{
		assert(min <= max);
		int range = max - min;
		return nextInt(range + 1) + min;
	}

	/**
	 * Performs an xdy dice roll where x number of y sided dice are rolled. Thus
	 * the minimum value returned from this function is x (ie all the dice were
	 * 1s). The maximum value this function could take is x * y (ie all dice
	 * rolled their highest value). However, as x increases, the probability curve
	 * becomes more normally distributed.
	 * 
	 * @param x the number of dice to roll
	 * @param y the number of sides the dice have
	 * @return the total value of all the dice rolled.
	 */
	public int diceXdY(int x, int y)
	{
		int sum = 0;
		for(int i = 0; i < x; i++)
			sum += nextInt(1, y);
		return sum;
	}
}