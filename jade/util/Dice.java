package jade.util;

import java.awt.Color;
import java.io.Serializable;
import java.util.Random;

/**
 * For now Dice mearly extends Random and adds a few methods, but will
 * eventually be reimplementated as a Mersenne twister.
 */
public class Dice extends Random implements Serializable
{
	/**
	 * A default instance of Dice when you don't need a unique instance.
	 */
	public static final Dice global = new Dice();

	public Dice()
	{
		super();
	}

	public Dice(long seed)
	{
		super(seed);
	}

	/**
	 * Returns an integer between min (inclusive) and max (inclusive)
	 */
	public int nextInt(int min, int max)
	{
		assert (min <= max);
		int range = max - min;
		return nextInt(range + 1) + min;
	}

	/**
	 * Performs a dice roll xdy, or an y sided dice x times. For example, a
	 * monopoly roll would be 2d6.
	 */
	public int diceXdY(int x, int y)
	{
		int sum = 0;
		for(int i = 0; i < x; i++)
			sum += nextInt(1, y);
		return sum;
	}

	/**
	 * Returns a random color
	 */
	public Color nextColor()
	{
		final int r = nextInt(256);
		final int g = nextInt(256);
		final int b = nextInt(256);
		return new Color(r, g, b);
	}

	/**
	 * Returns a random char between min (inclusive) and max (inclusive) on the
	 * ascii table.
	 */
	public char nextChar(char min, char max)
	{
		return (char)nextInt(min, max);
	}

	/**
	 * Returns a random Direction
	 */
	public Direction nextDir()
	{
		Direction[] values = Direction.values();
		return values[nextInt(values.length)];
	}
}