package jade.path;

import jade.core.World;
import jade.util.Coord;
import java.util.List;

/**
 * This interface represents something that can calculate paths.
 */
public interface Path
{
	/**
	 * Gets a path from start to goal
	 */
	public List<Coord> getPath(World world, Coord start, Coord goal);

	/**
	 * Returns true if there is a path from start to the goal.
	 */
	public boolean hasPath(World world, Coord start, Coord goal);

	public static class PathFactory
	{
		private static Path aStar;
		private static Path bresenham;

		/**
		 * Uses A* to find the shortest path.
		 */
		public static Path aStar()
		{
			if(aStar == null)
				aStar = new AStar();
			return aStar;
		}

		/**
		 * Uses bresenham's line drawing to find a straight path.
		 */
		public static Path bresenham()
		{
			if(bresenham == null)
				bresenham = new Bresenham();
			return bresenham;
		}
	}
}
