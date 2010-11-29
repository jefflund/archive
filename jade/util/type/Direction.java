package jade.util.type;

import jade.util.Tools;

/**
 * Represents a compass direction. In addition, Direction has an associated (dx,
 * dy) which will translate a Coord one cell in the given direction.
 */
public enum Direction
{
    N(0, -1), NE(1, -1), E(1, 0), SE(1, 1), S(0, 1), SW(-1, 1), W(-1, 0), NW(
            -1, -1), O(0, 0);

    private int x;
    private int y;

    Direction(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int x()
    {
        return x;
    }

    public int y()
    {
        return y;
    }

    /**
     * Returns the direction with the associated (dx, dy). Both dx and dy will
     * be clamped to [-1, 1].
     * @param dx the change in x
     * @param dy the change in y
     * @return the direction with the associated (dx, dy)
     */
    public static Direction fromCoord(int dx, int dy)
    {
        dx = Tools.clamp(dx, -1, 1);
        dy = Tools.clamp(dy, -1, 1);
        for(Direction dir : Direction.values())
            if(dir.x == dx && dir.y == dy)
                return dir;
        return null;
    }

    public static Direction fromCoord(Coord delta)
    {
        return fromCoord(delta.x(), delta.y());
    }
}
