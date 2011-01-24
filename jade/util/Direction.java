package jade.util;

/**
 * Represents a cardinal direction.
 */
public enum Direction
{
    /**
     * Up on screen.
     */
    NORTH(0, -1),
    /**
     * Up-right on screen.
     */
    NORTHEAST(1, -1),
    /**
     * Right on screen.
     */
    EAST(1, 0),
    /**
     * Down-right on screen.
     */
    SOUTHEAST(1, 1),
    /**
     * Down on screen.
     */
    SOUTH(0, 1),
    /**
     * Down-left on screen.
     */
    SOUTHWEST(-1, 1),
    /**
     * Left on screen.
     */
    WEST(-1, 0),
    /**
     * Up-left on screen.
     */
    NORTHWEST(-1, -1),
    /**
     * No change on screen.
     */
    ORIGIN(0, 0);

    private int x;
    private int y;

    /**
     * Returns the change in x for this Direction.
     * @return the change in x for this Direction
     */
    public int dx()
    {
        return x;
    }

    /**
     * Returns the change in y for this Direction.
     * @return the change in y for this Direction
     */
    public int dy()
    {
        return y;
    }

    private Direction(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the direction that corresponds with a given vi key. If no
     * direction corresponds, null is returned instead.
     * @param key the key being queried
     * @return the direction that corresponds with a given vi key
     */
    public static Direction viKeyDir(char key)
    {
        switch(key)
        {
        case 'l':
            return Direction.EAST;
        case 'h':
            return Direction.WEST;
        case 'k':
            return Direction.NORTH;
        case 'j':
            return Direction.SOUTH;
        case 'b':
            return Direction.SOUTHWEST;
        case 'n':
            return Direction.SOUTHEAST;
        case 'u':
            return Direction.NORTHEAST;
        case 'y':
            return Direction.NORTHWEST;
        case '.':
        default:
            return null;
        }
    }

    /**
     * Returns the direction that corresponds with a given numpad key. If no
     * direction corresponds, null is returned instead.
     * @param key the key being queried
     * @return the direction that corresponds with a given numpad key
     */
    public static Direction numKeyDir(char key)
    {
        switch(key)
        {
        case '6':
            return Direction.EAST;
        case '4':
            return Direction.WEST;
        case '8':
            return Direction.NORTH;
        case '2':
            return Direction.SOUTH;
        case '1':
            return Direction.SOUTHWEST;
        case '3':
            return Direction.SOUTHEAST;
        case '9':
            return Direction.NORTHEAST;
        case '7':
            return Direction.NORTHWEST;
        case '5':
            return Direction.ORIGIN;
        default:
            return null;
        }
    }

    /**
     * Returns the direction that corresponds with a given vi or numpad key. If
     * no direction corresponds, null is returned instead.
     * @param key the key being queried
     * @return the direction that corresponds with a given vi or numpad key key
     */
    public static Direction keyDir(char key)
    {
        Direction dir = viKeyDir(key);
        if(dir == null)
            dir = numKeyDir(key);
        return dir;
    }

    /**
     * Returns the direction that corresponds to the opposite cardinal direction
     * as this one.
     * @return the direction that corresponds to the opposite cardinal direction
     */
    public Direction opposite()
    {
        switch(this)
        {
        case EAST:
            return WEST;
        case WEST:
            return EAST;
        case NORTH:
            return SOUTH;
        case SOUTH:
            return NORTH;
        case NORTHEAST:
            return SOUTHWEST;
        case SOUTHWEST:
            return NORTHEAST;
        case NORTHWEST:
            return SOUTHEAST;
        case SOUTHEAST:
            return NORTHWEST;
        case ORIGIN:
            return ORIGIN;
        default:
            return null;
        }
    }
}
