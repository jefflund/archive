package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a field of vision algorithm. All FoV should be generated assuming
 * a square line of sight, and then given the option to filter on a circle.
 */
public abstract class FoV
{
    private boolean circular;

    /**
     * Initializes the FoV.
     * @param circular true if circular filter post-processing is desired
     */
    public FoV(boolean circular)
    {
        this.circular = circular;
    }

    /**
     * Returns the location of all visible tiles from (x, y) on the world,
     * within range r.
     * @param world the world on which (x, y) refers to
     * @param x the x value of the center of the field of vision
     * @param y the y value of the center of the field of vision
     * @param r the range of the field of vision
     * @return the location of all the visible tiles from (x, y)
     */
    public final Set<Coord> getFoV(World world, int x, int y, int r)
    {
        Set<Coord> fov = calcFoV(world, x, y, r);
        if(circular)
            filterCircle(fov, x, y, r);
        return fov;
    }

    /**
     * Returns the location of all visible tiles from (x, y) on the world, where
     * (x, y) is given by the Coord, within range r.
     * @param world the world on which (x, y) refers to
     * @param coord the value of the center of the field of vision
     * @param r the range of the field of vision
     * @return the location of all the visible tiles from (x, y)
     */
    public final Set<Coord> getFoV(World world, Coord pos, int r)
    {
        return getFoV(world, pos.x(), pos.y(), r);
    }

    /**
     * Performs the actual calculation of field of vision. This method should
     * consider the field to be a square with length 2 * r, centered at (x, y).
     * @param world the world on which (x, y) refers to
     * @param x the x value of the center of the field of vision
     * @param y the y value of the center of the field of vision
     * @param r the range of the field of vision
     * @return the location of all the visible tiles from (x, y)
     */
    protected abstract Set<Coord> calcFoV(World world, int x, int y, int r);

    /**
     * Removes an Coord from the field that are not with in the circle with the
     * given radius and center.
     * @param field the field of vision
     * @param x the x value of the center of the field of vision
     * @param y the y value of the center of the field of vision
     * @param radius the radius of the circular filter
     */
    public static void filterCircle(Collection<Coord> field, int x, int y,
            int radius)
    {
        Collection<Coord> out = new HashSet<Coord>();
        radius++;
        for(Coord coord : field)
            if(!inCircle(x, y, coord.x(), coord.y(), radius))
                out.add(coord);
        field.removeAll(out);
    }

    private static boolean inCircle(int cx, int cy, int x, int y, int r)
    {
        final int a = x - cx;
        final int b = y - cy;
        return a * a + b * b < r * r;
    }
}
