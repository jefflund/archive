package jade.fov;

import jade.core.World;
import jade.path.Bresenham;
import jade.util.type.Coord;
import java.util.HashSet;
import java.util.Set;

/**
 * Uses simple raycasting to produce a field of vision. This algorithm sends out
 * a ray to every border cell. As a result, cells close to the origin will be
 * checked multiple times. That said, the algorithm can be quite fast due to its
 * simplicity. Also note that the algorithm is asymetric.
 */
public class Raycast extends Bresenham implements FoV
{
    @Override
    public Set<Coord> getFov(World world, Coord orig, int range)
    {
        Set<Coord> fov = new HashSet<Coord>();
        fov.add(orig);
        int x = orig.x();
        int y = orig.y();
        for(int dx = x - range; dx <= x + range; dx++)
        {
            fov.addAll(castray(world, x, y, dx, y - range));
            fov.addAll(castray(world, x, y, dx, y + range));
        }
        for(int dy = y - range; dy <= y + range; dy++)
        {
            fov.addAll(castray(world, x, y, x + range, dy));
            fov.addAll(castray(world, x, y, x - range, dy));
        }
        return fov;
    }
}
