package jade.util;

/**
 * A mutable two-dimensional integer cartesian coordinate
 */
public class Coord
{
    private int x;
    private int y;

    /**
     * Constructs a Coord at (x, y).
     * @param x the x value of the Coord
     * @param y the y value of the Coord
     */
    public Coord(int x, int y)
    {
        move(x, y);
    }

    /**
     * Constructs a new Coord at the location of the given Coord.
     * @param coord the Coord to be copied
     */
    public Coord(Coord coord)
    {
        this(coord.x, coord.y);
    }

    /**
     * Constructs a new Coord at (0, 0).
     */
    public Coord()
    {
        this(0, 0);
    }

    /**
     * The x value of the Coord.
     * @return x value of the Coord
     */
    public int x()
    {
        return x;
    }

    /**
     * The y value of the Coord.
     * @return y value of the Coord
     */
    public int y()
    {
        return y;
    }

    /**
     * Moves the Coord to (x, y).
     * @param x the new x value of the Coord
     * @param y the new y value of the Coord
     * @return this for convience
     */
    public Coord move(int x, int y)
    {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Moves this Coord to that of the given Coord
     * @param coord the new location of this Coord
     * @return this for convience
     */
    public Coord move(Coord coord)
    {
        return move(coord.x, coord.y);
    }

    /**
     * Translates the Coord by (dx, dy) so the new location is (x + dx, y + dy).
     * @param dx the change in x
     * @param dy the change in y
     * @return this for convience
     */
    public Coord translate(int dx, int dy)
    {
        return move(x + dx, y + dy);
    }

    /**
     * Translates the Coord by (dx, dy) as given by the delta, so that the new
     * location is (x + dx, y + dy).
     * @param delta the change in x and y
     * @return this for convience
     */
    public Coord translate(Coord delta)
    {
        return translate(delta.x, delta.y);
    }

    /**
     * Translates the Coord by one in specified Direction. The new position of
     * the Coord will be (x + dx, y + dy) where (dx, dy) is given by the value
     * of the Direction.
     * @param direction the direction to translate the Coord
     * @return this for convience
     */
    public Coord translate(Direction direction)
    {
        return translate(direction.dx(), direction.dy());
    }

    /**
     * Returns the cartesian distance from this Coord to the other Coord.
     * @param other the other Coord over which distance is calculated
     * @return the cartesian distance from this Coord to the other Coord
     */
    public double distCart(Coord other)
    {
        double a = x - other.x;
        double b = y - other.y;
        return Math.sqrt(a * a + b * b);
    }

    /**
     * Returns the manhatan distance from this Coord to the other Coord.
     * @param other the other Coord over which distance is calculated
     * @return the manhatan distance from this Coord to the other Coord
     */
    public double distTaxi(Coord other)
    {
        int a = Math.abs(x - other.x);
        int b = Math.abs(y - other.y);
        return a + b;
    }

    /**
     * Returns the roguelike distance from this Coord to the other Coord.
     * @param other the other Coord over which distance is calculated
     * @return the roguelike distance from this Coord to the other Coord
     */
    public double distRl(Coord other)
    {
        int a = Math.abs(x - other.x);
        int b = Math.abs(y - other.y);
        return Math.min(a, b);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof Coord))
            return false;
        Coord coord = (Coord)obj;
        return coord.x == x && coord.y == y;
    }

    @Override
    public int hashCode()
    {
        return (x << 16) | (y & 0xffff);
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
}
