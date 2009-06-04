package jade.path;

import jade.core.World;
import jade.util.Coord;

import java.util.List;

public interface Path
{
	public static final int AStar = 0;
	public static final int Dijkstra = 1;
	public static final int Bresenham = 2;
	
	public List<Coord> getPath(World world, Coord start, Coord goal);
	public boolean hasPath(World world, Coord start, Coord goal);

	public class PathFactory
	{
		public static Path get(int algorithm)
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
