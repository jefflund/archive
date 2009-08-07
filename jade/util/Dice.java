package jade.util;

import java.awt.Color;
import java.io.Serializable;
import java.util.Random;

/**
 * Dice basically extends Random and adds a few methods. Eventually, I may
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
		assert (min <= max);
		final int range = max - min;
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
		for (int i = 0; i < x; i++)
			sum += nextInt(1, y);
		return sum;
	}

	/**
	 * Returns a psuedorandom color with r,g,b values from 0 to 255.
	 * @return a psuedorandom color with r,g,b values from 0 to 255.
	 */
	public Color nextColor()
	{
		final int r = nextInt(256);
		final int g = nextInt(256);
		final int b = nextInt(256);
		return new Color(r, g, b);
	}

	/**
	 * Returns a random char between the min and max char on the ascii table
	 * (inclusive).
	 * @param min the minium ascii value that could be returned
	 * @param max the maximuc ascii value that could be returned
	 * @return a random char between the min and max char on the ascii table
	 */
	public char nextChar(char min, char max)
	{
		return (char) nextInt(min, max);
	}
}