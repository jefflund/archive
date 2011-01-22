package jade.path;

import jade.core.World;
import jade.util.Coord;

/**
 * Uses the A* algorithm to quickly find optimal paths. So long as the heuristic
 * is admissible, meaning it never overestimates, the returned path is
 * guaranteed to be an optimal path. The closer to the actual cost the heuristic
 * is, the faster A* will run. A* is aloways complete.
 */
public class AStar extends GraphSearch
{
    @Override
    protected double gCost(World world, Coord start, Coord end)
    {
        return start.distCart(end);
    }

    @Override
    protected double hEstimate(World world, Coord start, Coord end)
    {
        return start.distCart(end);
    }
}
