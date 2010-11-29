package jade.path;

import jade.core.World;
import jade.util.Factory;
import jade.util.type.Coord;
import java.util.List;

/**
 * Defines the interface for a path generator. Various algorithms can be
 * complete, meaning that between two nodes, if there is a path it will be
 * found. Algorithms can also be optimal, meaning that if a path is found, it
 * will be the optimal according to some metric such as path traversal cost.
 * These factors should be considered when choosing a path finding algorithm.
 * Note that in order to use the factory, all implementing classes should have a
 * default constructor.
 */
public interface Path
{
    /**
     * Returns a list of coordinates representing the path that was generated.
     * This path will not include the starting coordinate, but will include the
     * goal. If no path was found, null is returned. This does not imply
     * non-connectivity, only that the path generator did not find a path.
     * @param world the world on which to generate the path
     * @param start the starting position of the path
     * @param goal the ending position of the path
     * @return the generated path
     */
    public abstract List<Coord> getPath(World world, Coord start, Coord goal);

    /**
     * Factory for generating unique instances of Path
     */
    public static Factory<Path> factory = new Factory<Path>();
}
