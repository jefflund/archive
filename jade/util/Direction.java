package jade.util;

/**
 * Represents a cardinal direction.
 */
public enum Direction
{
    NORTH(0, -1), NORTHEAST(1, -1), EAST(1, 0), SOUTHEAST(1, 1), SOUTH(0, 1), SOUTHWEST(
            -1, 1), WEST(-1, 0), NORTHWEST(-1, -1), ORIGIN(0, 0);

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

    public static Direction keyDir(char key)
    {
        Direction dir = viKeyDir(key);
        if(dir == null)
            dir = numKeyDir(key);
        return dir;
    }
}
