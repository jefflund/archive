package jade.path;

import jade.core.World;
import jade.util.type.Coord;
import java.util.LinkedList;
import java.util.List;

/**
 * Uses Bresenham's line drawing algorithm to find straight line paths. The
 * non-existence of such a path does not imply non-connectivity, only that the
 * digital line draw between the two points crossed some obstacle. Not only is
 * this algorithm non-complete, but it is also asymetric. In other words, the
 * path from A to B might not be the same as the path from B to A.
 */
public class Bresenham implements Path
{
    @Override
    public List<Coord> getPath(World world, Coord start, Coord goal)
    {
        List<Coord> path = castray(world, start, goal);
        if(!path.isEmpty() && path.get(path.size() - 1).equals(goal))
            return path;
        else
            return null;
    }

    public final List<Coord> castray(World world, Coord start, Coord goal)
    {
        return castray(world, start.x(), start.y(), goal.x(), goal.y());
    }

    public List<Coord> castray(World world, int x1, int y1, int x2, int y2)
    {
        List<Coord> path = new LinkedList<Coord>();
        int dx = Math.abs(x2 - x1) << 1;
        int dy = Math.abs(y2 - y1) << 1;
        int ix = x2 > x1 ? 1 : -1;
        int iy = y2 > y1 ? 1 : -1;
        if(dx >= dy)
        {
            int error = dy - (dx >> 1);
            while(x1 != x2)
            {
                if(error >= 0 && (error != 0 || ix > 0))
                {
                    y1 += iy;
                    error -= dx;
                }
                x1 += ix;
                error += dy;
                path.add(new Coord(x1, y1));
                if(!world.passable(x1, y1))
                    break;
            }
        }
        else
        // dx < dy
        {
            int error = dx - (dy >> 1);
            while(y1 != y2)
            {
                if(error >= 0 && (error != 0 || iy > 0))
                {
                    x1 += ix;
                    error -= dy;
                }
                y1 += iy;
                error += dx;
                path.add(new Coord(x1, y1));
                if(!world.passable(x1, y1))
                    break;
            }
        }
        return path;
    }// end castray
}
