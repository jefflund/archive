package jade.path;

import jade.core.World;
import jade.util.Coord;

/**
 * Uniform cost search is an uninformed graph search. In other words, there is
 * no heuristic. The algorithm is complete and optimal, but may take longer than
 * some informed searches. Still, it is useful when it is difficult to generate
 * an admissible heuristic and optimality is still required.
 */
public class Uniform extends GraphSearch
{
    @Override
    protected double gCost(World world, Coord start, Coord end)
    {
        return start.distCart(end);
    }

    @Override
    protected final double hEstimate(World world, Coord start, Coord end)
    {
        return 0;
    }
}
