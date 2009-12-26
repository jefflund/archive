package jade.util;

import java.util.Collection;
import java.util.TreeSet;

/**
 * A collecection of utility methods
 */
public final class Tools
{
	private Tools()
	{
		throw new IllegalAccessError("Tools cannot be instantiated");
	}

	/**
	 * Takes a keypress char and turns it into a Direction, or null if the char
	 * doesn't represent a Direction
	 */
	public static Direction keyToDir(char key, boolean vi, boolean numeric)
	{
		assert (vi || numeric);
		if(vi && (Character.isLetter(key) || key == '.'))
			return viToDir(key);
		else if(numeric && Character.isDigit(key))
			return numToDir(key);
		else
			return null;
	}

	private static Direction numToDir(char key)
	{
		switch(key)
		{
		case '6':
			return Direction.E;
		case '4':
			return Direction.W;
		case '8':
			return Direction.N;
		case '2':
			return Direction.S;
		case '1':
			return Direction.SW;
		case '3':
			return Direction.SE;
		case '9':
			return Direction.NE;
		case '7':
			return Direction.NW;
		case '5':
			return Direction.O;
		default:
			return null;
		}
	}

	private static Direction viToDir(char key)
	{
		switch(key)
		{
		case 'l':
			return Direction.E;
		case 'h':
			return Direction.W;
		case 'k':
			return Direction.N;
		case 'j':
			return Direction.S;
		case 'b':
			return Direction.SW;
		case 'n':
			return Direction.SE;
		case 'u':
			return Direction.NE;
		case 'y':
			return Direction.NW;
		case '.':
			return Direction.O;
		default:
			return null;
		}
	}

	/**
	 * Returns true if str ends with suffix. (case sensitive)
	 */
	public static boolean strHasSuffix(String str, String suffix)
	{
		assert (str.length() >= suffix.length());
		return str.substring(str.length() - suffix.length()).equals(suffix);
	}

	/**
	 * Returns the string if it has the suffix, or returns the suffix concatenated
	 * with the string if it does not.
	 */
	public static String strEnsureSuffix(String str, String suffix)
	{
		if(strHasSuffix(str, suffix))
			return str;
		return str + suffix;
	}

	/**
	 * Returns the string concatinated with enough spaces to ensure the length.
	 */
	public static String strEnsureLength(String str, int length)
	{
		while(str.length() < length)
			str += " ";
		return str;
	}

	/**
	 * Maps an integer to a char, with 1 mapping to 'a', 2 to 'b', 3 to 'c' and so
	 * on.
	 */
	public static char intToAlpha(int i)
	{
		return (char)(i + 'a');
	}

	/**
	 * Maps a char to an integer, with 'a' mapping to 1, 'b' to 2, 'c' to 3 and so
	 * on.
	 */
	public static int alphaToInt(char ch)
	{
		return(ch - 'a');
	}

	/**
	 * Removes an Coord from the field that are not with in the circle with the
	 * given radius and center.
	 */
	public static void filterCircle(Collection<Coord> field, int x, int y,
			int radius)
	{
		final Collection<Coord> out = new TreeSet<Coord>();
		radius++;
		for(final Coord coord : field)
			if(!inCircle(x, y, coord.x(), coord.y(), radius))
				out.add(coord);
		field.removeAll(out);
	}

	/**
	 * Returns true if the point (x, y) is in the circle centered at (cx, cy) with
	 * radius r.
	 */
	public static boolean inCircle(int cx, int cy, int x, int y, int r)
	{
		final int a = x - cx;
		final int b = y - cy;
		return a * a + b * b < r * r;
	}

	/**
	 * Returns the value if it within the given range, or the nearest extreama if
	 * the value is outside the range.
	 */
	public static int clampToRange(int value, int min, int max)
	{
		if(value < min)
			value = min;
		if(value > max)
			value = max;
		return value;
	}

	/**
	 * Returns the direction needed to get from start to goal.
	 */
	public static Direction directionTo(Coord start, Coord goal)
	{
		int dx = goal.x() - start.x();
		int dy = goal.y() - start.y();
		if(dx < 0)
		{
			if(dy < 0)
				return Direction.NW;
			else if(dy > 0)
				return Direction.SW;
			else
				return Direction.W;
		}
		else if(dx > 0)
		{
			if(dy < 0)
				return Direction.NE;
			else if(dy > 0)
				return Direction.SE;
			else
				return Direction.E;
		}
		else
		{
			if(dy < 0)
				return Direction.N;
			else if(dy > 0)
				return Direction.S;
			else
				return Direction.O;
		}
	}
}
