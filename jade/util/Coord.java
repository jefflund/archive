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
     * Returns a new Coord, which is a copy of this one translated by (dx, dy).
     * @param dx the change in x
     * @param dy the change in y
     * @return a new Coord, which is a copy of this one translated by (dx, dy)
     */
    public Coord translated(int dx, int dy)
    {
        Coord copy = new Coord(this);
        copy.translate(dx, dy);
        return copy;
    }

    /**
     * Returns a new Coord, which is a copy of this one translated by (dx, dy),
     * where (dx, dy) is given by the Coord delta.
     * @param delta the change in (x, y)
     * @return a new Coord, which is a copy of this one translated by (dx, dy)
     */
    public final Coord translated(Coord delta)
    {
        return translated(delta.x, delta.y);
    }

    /**
     * Returns a new Coord, which is a copy of this one tranlated by (dx, dy),
     * where (dx, dy) is given by the Direction.
     * @param direction the direction of change
     * @return a new Coord, which is a copy of this one translated by (dx, dy)
     */
    public final Coord translated(Direction direction)
    {
        return translated(direction.dx(), direction.dy());
    }

    /**
     * Returns the cartesian distance from this Coord to the other Coord.
     * @param other the other Coord over which distance is calculated
     * @return the cartesian distance from this Coord to the other Coord
     */
    public final double distCart(Coord other)
    {
        return distCart(other.x, other.y);
    }

    /**
     * Return the cartesian distance from this Coord to the Coord (x, y)
     * @param x the x value of the other Coord over which distance is calculated
     * @param y the y value of the other Coord over which distance is calculated
     * @return the cartesian distance from this Coord to (x, y)
     */
    public double distCart(int x, int y)
    {
        double a = this.x - x;
        double b = this.y - y;
        return Math.sqrt(a * a + b * b);
    }

    /**
     * Returns the manhatan distance from this Coord to the other Coord.
     * @param other the other Coord over which distance is calculated
     * @return the manhatan distance from this Coord to the other Coord
     */
    public final double distTaxi(Coord other)
    {
        return distTaxi(other.x, other.y);
    }

    /**
     * Return the manhatan distance from this Coord to the Coord (x, y)
     * @param x the x value of the other Coord over which distance is calculated
     * @param y the y value of the other Coord over which distance is calculated
     * @return the manhatan distance from this Coord to (x, y)
     */
    public double distTaxi(int x, int y)
    {
        int a = Math.abs(this.x - x);
        int b = Math.abs(this.y - y);
        return a + b;
    }

    /**
     * Returns the roguelike distance from this Coord to the other Coord.
     * @param other the other Coord over which distance is calculated
     * @return the roguelike distance from this Coord to the other Coord
     */
    public final double distRl(Coord other)
    {
        return distRl(other.x, other.y);
    }

    /**
     * Return the roguelike distance from this Coord to the Coord (x, y)
     * @param x the x value of the other Coord over which distance is calculated
     * @param y the y value of the other Coord over which distance is calculated
     * @return the roguelike distance from this Coord to (x, y)
     */
    public double distRl(int x, int y)
    {
        int a = Math.abs(this.x - x);
        int b = Math.abs(this.y - y);
        return Math.min(a, b);
    }

    /**
     * Returns the direction which is one step toward the given Coord.
     * @param goal the Coord towards which the result points
     * @return the direction which is one step toward the given Coord
     */
    public final Direction directionTo(Coord goal)
    {
        return directionTo(goal.x, goal.y);
    }

    /**
     * Returns the direction which is one step toward the given (x, y)
     * @param x the x value of the Coord towards which the result points
     * @param y the y value of the Coord towards which the result points
     * @return the direction which is one step toward the given Coord
     */
    public Direction directionTo(int x, int y)
    {
        int dx = x - this.x;
        int dy = y - this.y;
        if(dx < 0)
        {
            if(dy < 0)
                return Direction.NORTHWEST;
            else if(dy > 0)
                return Direction.SOUTHWEST;
            else
                return Direction.WEST;
        }
        else if(dx > 0)
        {
            if(dy < 0)
                return Direction.NORTHEAST;
            else if(dy > 0)
                return Direction.SOUTHEAST;
            else
                return Direction.EAST;
        }
        else
        {
            if(dy < 0)
                return Direction.NORTH;
            else if(dy > 0)
                return Direction.SOUTH;
            else
                return Direction.ORIGIN;
        }
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
