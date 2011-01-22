package jade.path;

import jade.core.World;
import jade.util.Coord;

/**
 * Greedily searches, without reguard to path cost. In other words, the gCost
 * function for the greedy best first search always returns 0. This actually
 * leads to very quick search times, and the paths are usually fairly good, but
 * are not guaranteed to be optimal, and in some cases can be wildly suboptimal.
 */
public class Greedy extends GraphSearch
{
    @Override
    protected final double gCost(World world, Coord start, Coord end)
    {
        return 0;
    }

    @Override
    protected double hEstimate(World world, Coord start, Coord end)
    {
        return start.distCart(end);
    }
}
