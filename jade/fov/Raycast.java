package jade.fov;

import jade.core.World;
import jade.path.Bresenham;
import jade.util.Coord;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Uses the simple and fast raycasting algorithm to calculate line of sight.
 */
public class Raycast extends FoV
{
    private Bresenham raycaster;

    /**
     * Initializes the FoV.
     * @param circular true if circular filter post-processing is desired
     */
    public Raycast(boolean circular)
    {
        super(circular);
        raycaster = new Bresenham();
    }

    @Override
    protected Set<Coord> calcFoV(World world, int x, int y, int r)
    {
        Set<Coord> result = new HashSet<Coord>();
        result.add(new Coord(x, y));
        for(int dx = x - r; dx <= x + r; dx++)
        {
            result.addAll(castray(world, x, y, dx, y - r));
            result.addAll(castray(world, x, y, dx, y + r));
        }
        for(int dy = y - r; dy <= y + r; dy++)
        {
            result.addAll(castray(world, x, y, x + r, dy));
            result.addAll(castray(world, x, y, x - r, dy));
        }
        return result;
    }

    private List<Coord> castray(World world, int x1, int y1, int x2, int y2)
    {
        return raycaster.calcPath(world, new Coord(x1, y1), new Coord(x2, y2));
    }
}
