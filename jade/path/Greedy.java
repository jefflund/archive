package jade.path;

import jade.core.World;
import jade.util.type.Coord;

/**
 * Uses the greedy best first algorithm to find paths. The algorithm is not
 * optimal, but is complete. The algorithm is completely guided by its
 * heuristic, and as such will always follow the heuristic if it can, rather
 * than following the optimal path.
 */
public class Greedy extends GraphSearch
{
    @Override
    protected final double gFunction(World world, Coord start, Coord goal)
    {
        return 0;
    }

    @Override
    protected double hEstimate(World world, Coord start, Coord goal)
    {
        return start.distCart(goal);
    }
}
