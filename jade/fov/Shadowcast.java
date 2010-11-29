package jade.fov;

import jade.core.World;
import jade.util.type.Coord;
import java.util.HashSet;
import java.util.Set;

/**
 * Uses recursive shadowcasting to compute field of vision. For large open
 * spaces, recursive shadowcasting can perform better than the much simpler
 * raycasting algorithm. This is because shadowcasting does not visit any cell
 * more than once. Instead, the algorithm outward until it finds an obstacle, at
 * which point it starts two recursive scans and the begining and end of the
 * obstacle.
 */
public class Shadowcast implements FoV
{
    @Override
    public Set<Coord> getFov(World world, Coord orig, int range)
    {
        Set<Coord> fov = new HashSet<Coord>();
        fov.add(orig);
        for(int octant = 0; octant < 8; octant++)
            scan(1, 1f, 0, orig, fov, world, range, octant);
        return fov;
    }

    private void scan(int depth, float startslope, float endslope, Coord orig,
            Set<Coord> fov, World world, int range, int octant)
    {
        if(depth > range)
            return;
        int y = Math.round(startslope * depth);
        while(slope(depth, y) >= endslope)
        {
            Coord curr = getCurr(orig, depth, y, octant);
            Coord prev = getPrev(orig, depth, y, octant, world);
            if(world.passable(curr) && !world.passable(prev))
                startslope = newStartslope(depth, endslope, y);
            if(!world.passable(curr) && world.passable(prev))
                scan(depth + 1,
                     startslope,
                     newEndslope(depth, y),
                     orig,
                     fov,
                     world,
                     range,
                     octant);
            fov.add(curr);
            y--;
        }
        y++;
        if(world.passable(getCurr(orig, depth, y, octant)))
            scan(depth + 1,
                 startslope,
                 endslope,
                 orig,
                 fov,
                 world,
                 range,
                 octant);
    }

    private float newEndslope(int depth, int y)
    {
        return slope(depth - .5f, y + .5f);
    }

    private float newStartslope(int depth, float endslope, int y)
    {
        return Math.max(slope(depth + .5f, y - .5f), endslope);
    }

    private float slope(float x, float y)
    {
        if(x == 0)
            return Float.MAX_VALUE;
        return y / x;
    }

    private Coord getCurr(Coord orig, int x, int y, int octant)
    {
        switch(octant)
        {
        case 0:
            return new Coord(orig.x() - y, orig.y() - x);
        case 1:
            return new Coord(orig.x() + x, orig.y() + y);
        case 2:
            return new Coord(orig.x() + x, orig.y() - y);
        case 3:
            return new Coord(orig.x() + y, orig.y() + x);
        case 4:
            return new Coord(orig.x() - y, orig.y() + x);
        case 5:
            return new Coord(orig.x() - x, orig.y() + y);
        case 6:
            return new Coord(orig.x() - x, orig.y() - y);
        case 7:
            return new Coord(orig.x() + y, orig.y() - x);
        default:
            throw new IllegalArgumentException("octant must be between 0 and 7");
        }
    }

    private Coord getPrev(Coord orig, int x, int y, int octant, World world)
    {
        Coord curr = getCurr(orig, x, y, octant);
        if(curr.x() == 0 || curr.y() == 0 || curr.x() == world.width() - 1
                || curr.y() == world.height() - 1)
            return curr;
        switch(octant)
        {
        case 0:
            return curr.getTranslated(-1, 0);
        case 1:
            return curr.getTranslated(0, 1);
        case 2:
            return curr.getTranslated(0, -1);
        case 3:
            return curr.getTranslated(1, 0);
        case 4:
            return curr.getTranslated(-1, 0);
        case 5:
            return curr.getTranslated(0, 1);
        case 6:
            return curr.getTranslated(0, -1);
        case 7:
            return curr.getTranslated(1, 0);
        default:
            throw new IllegalArgumentException("octant must be between 0 and 7");
        }
    }

}
