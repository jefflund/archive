package jade.path;

import jade.core.World;
import jade.util.Coord;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An interface for pathfinding on a jade world.
 */
public interface Path
{
	/**
	 * Uses heuristic estimates to quickly find the shortest path between two
	 * points.
	 */
	public static final int AStar = 0;
	/**
	 * I implemented Dijkstra's rather poorly. Until I fix it use AStar.
	 */
	public static final int Dijkstra = 1;
	/**
	 * Uses Bresenham's Line Drawing to draw a straight line. Suitible for line of
	 * sight.
	 */
	public static final int Bresenham = 2;

	/**
	 * Returns a list with a path from start to the goal, or null if no path
	 * exist. Depending on the algorithm, this may or not be the most efficient
	 * path, or it may erroneously fail to find a connectiong path.
	 * @param world the world on which to pathfind
	 * @param start the start of the path
	 * @param goal the end of the path
	 * @return a list with a path from start to the goal, or null if no path exist
	 */
	public List<Coord> getPath(World world, Coord start, Coord goal);

	/**
	 * Returns true if a path from start to the goal exist, false otherwise.
	 * Depending on the algorithm, it may erroneously fail to find a connectiong
	 * path.
	 * @param world the world on which to pathfind
	 * @param start the start of the path
	 * @param goal the end of the path
	 * @return true if a path from start to the goal exist, false otherwise
	 */
	public boolean hasPath(World world, Coord start, Coord goal);

	/**
	 * Used for retrieving varius singleton instances of Path implementing classes.
	 */
	public class PathFactory
	{
		private static Map<Integer, Path> singletons = new HashMap<Integer, Path>();

		/**
		 * Returns a instance implementing Gen based on the specified algorithm.
		 * 
		 * @param algorithm the algorithm to be implemented. Static integer
		 * constants are provided in Path
		 * @return a singleton instance of an object implementing Path with specified
		 * algorithm
		 */
		public static Path get(int algorithm)
		{
			Path result = singletons.get(algorithm);
			if(result == null)
				result = getNew(algorithm);
			return result;
		}

		private static Path getNew(int algorithm)
		{
			switch(algorithm)
			{
			case AStar:
				return new AStar();
			case Dijkstra:
				return new Dijkstra();
			case Bresenham:
				return new Bresenham();
			default:
				throw new IllegalArgumentException();
			}
		}
	}
}
