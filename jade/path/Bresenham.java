package jade.path;

import jade.core.World;
import jade.util.Coord;
import java.util.LinkedList;
import java.util.List;

/**
 * Uses Bresenham's Line Drawing Algorthim to find straight line paths between
 * two points. This can be useful for line of sight detection, or even in simple
 * raycasting field of vision. Note that the algorithm is not totally symetric.
 */
public class Bresenham extends Path
{
    @Override
    public List<Coord> calcPath(World world, Coord start, Coord end)
    {
        return castray(world, start.x(), start.y(), end.x(), end.y());
    }

    private List<Coord> castray(World world, int x1, int y1, int x2, int y2)
    {
        // Rogue Basin article on Bresenham's
        List<Coord> path = new LinkedList<Coord>();
        path.add(new Coord(x1, y1));
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