package jade.path;

import jade.core.World;
import jade.util.Coord;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Path
{
	public static final int AStar = 0;
	public static final int Dijkstra = 1;
	public static final int Bresenham = 2;

	public List<Coord> getPath(World world, Coord start, Coord goal);

	public boolean hasPath(World world, Coord start, Coord goal);

	public class PathFactory
	{
		private static Map<Integer, Path> singletons = new HashMap<Integer, Path>();

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
