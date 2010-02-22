package jade.util.type;

/**
 * Represents a rectangle boundry.
 */
public class Rect
{
	private Coord upleft;
	private Coord lowright;
	private int width;
	private int height;

	/**
	 * Creates a new Rectangle with one corner at the origin and the other at the
	 * given location
	 */
	public Rect(int x, int y)
	{
		this(0, 0, x, y);
	}

	/**
	 * Creates a new Rectangle with one corner at the origin and the other at the
	 * given location
	 */
	public Rect(Coord coord)
	{
		this(coord.x(), coord.y());
	}

	/**
	 * Creates a new Rectangle with corners at the two given locations
	 */
	public Rect(int x1, int y1, int x2, int y2)
	{
		upleft = new Coord(Math.min(x1, x2), Math.max(y1, y2));
		lowright = new Coord(Math.max(x1, x2), Math.min(y1, y2));
		calcWidth();
		calcHeight();
	}

	/**
	 * Creates a new Rectangle with corners at the two given locations
	 */
	public Rect(Coord coord1, Coord coord2)
	{
		this(coord1.x(), coord1.y(), coord2.x(), coord2.y());
	}

	/**
	 * Returns the minimum x value in the Rectangle
	 */
	public int xMin()
	{
		return upleft.x();
	}

	/**
	 * Returns the maximum x value in the Rectangle
	 */
	public int xMax()
	{
		return lowright.x();
	}

	/**
	 * Returns the minimum y value in the Rectangle
	 */
	public int yMax()
	{
		return upleft.y();
	}

	/**
	 * Returns the maximum y value in the Rectangle
	 */
	public int yMin()
	{
		return lowright.y();
	}

	private void calcWidth()
	{
		width = xMax() - xMin();
	}

	/**
	 * Returns the width of the Rectangle
	 */
	public int width()
	{
		return width;
	}

	private void calcHeight()
	{
		height = yMax() - yMin();
	}

	/**
	 * Returns the height of the Rectangle
	 */
	public int height()
	{
		return height;
	}

	/**
	 * Translates the entire Rectangle by the given amount
	 */
	public void translate(int dx, int dy)
	{
		upleft.translate(dx, dy);
		lowright.translate(dx, dy);
	}

	/**
	 * Returns a copy of this Rectangle, translated by the given amount
	 */
	public Rect getTranslated(int dx, int dy)
	{
		Rect result = new Rect(upleft, lowright);
		result.translate(dx, dy);
		return result;
	}

	public String toString()
	{
		return upleft.toString() + " " + lowright.toString();
	}
}
