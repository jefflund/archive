package jade.util;

import java.io.Serializable;

/**
 * Represents an integer cartesian coordinate. The Coord is not immutable, so
 * care should be taken when assigning Coord reference to another when the copy
 * constructor should have been used.
 */
public class Coord implements Comparable<Coord>, Serializable
{
	private int x;
	private int y;

	/**
	 * Creates a default coord at (0,0)
	 */
	public Coord()
	{
		move(0, 0);
	}

	/**
	 * Creates a coord with the specifed coordinates
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 */
	public Coord(int x, int y)
	{
		move(x, y);
	}

	/**
	 * A deep copy constructor
	 * @param coord to be copied
	 */
	public Coord(Coord coord)
	{
		move(coord);
	}

	/**
	 * Moves this coord to the specified coordinate.
	 * @param x the new x-coordinate
	 * @param y the new y-coordinate
	 * @return this coord after being moved
	 */
	public Coord move(int x, int y)
	{
		this.x = x;
		this.y = y;
		return this;
	}

	/**
	 * Moves the coordinate to the location of the given coordinate
	 * @param coord the coordinate to move this coord to
	 * @return this coord after being moved
	 */
	public final Coord move(Coord coord)
	{
		return move(coord.x, coord.y);
	}

	/**
	 * Translates the coord by the specified amount.
	 * @param dx the change in x
	 * @param dy the change in y
	 * @return this Coord after being translated
	 */
	public Coord translate(int dx, int dy)
	{
		x += dx;
		y += dy;
		return this;
	}

	/**
	 * Translates the coord by the specified amount.
	 * @param coord the change in this coord
	 * @return this Coord after being translated
	 */
	public final Coord translate(Coord coord)
	{
		return translate(coord.x, coord.y);
	}

	public final Coord getTranslated(Coord coord)
	{
		return getTranslated(coord.x(), coord.y());
	}

	public Coord getTranslated(int x, int y)
	{
		return new Coord(this).translate(x, y);
	}

	/**
	 * Returns the x-coordinate of the coord
	 * @return the x-coordinate of the coord
	 */
	public int x()
	{
		return x;
	}

	/**
	 * Returns the y-coordinate of the coord
	 * @return the y-coordinate of the coord
	 */
	public int y()
	{
		return y;
	}

	public int compareTo(Coord other)
	{
		if (x == other.x)
			return y - other.y;
		return x - other.x;
	}

	public boolean equals(Coord other)
	{
		return x() == other.x() && y() == other.y();
	}

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
}
