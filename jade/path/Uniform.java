package jade.path;

import jade.core.World;
import jade.util.type.Coord;

/**
 * Uses the uniform cost algorithm to generate optimal paths. The algorithm is
 * uninformed, completely ignoring any heuristics. This makes it slower than A*,
 * but in certain cases might generate more interesting results if you have an
 * interesting cost function, or have a difficult heuristic.
 */
public class Uniform extends GraphSearch
{
    @Override
    protected double gFunction(World world, Coord start, Coord goal)
    {
        return start.distCart(goal);
    }

    @Override
    protected final double hEstimate(World world, Coord start, Coord goal)
    {
        return 0;
    }
}
