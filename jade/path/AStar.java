package jade.path;

import jade.core.World;
import jade.util.type.Coord;

/**
 * Uses the A* algorithm to generate optimal paths. The algorithm is informed by
 * a heuristic, which improves speed compared to uniform cost search. A* is
 * complete so long as the heuristic is admissible, meaning that it never
 * overestimates the cost from a node to the goal. The default heuristic is
 * cartesian distance, which is admissible. Do note that inadmissible heuristics
 * can generate interesting results, just without the guarantee of completeness.
 */
public class AStar extends GraphSearch
{
    @Override
    protected double gFunction(World world, Coord start, Coord goal)
    {
        return start.distCart(goal);
    }

    @Override
    protected double hEstimate(World world, Coord start, Coord goal)
    {
        return start.distCart(goal);
    }
}
