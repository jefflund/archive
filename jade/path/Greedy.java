package jade.path;

import jade.core.World;
import jade.util.type.Coord;
import java.util.List;

public class Greedy implements Path
{
	@Override
	public List<Coord> getPath(World world, Coord start, Coord goal)
	{
		//TODO implement greedy best first search
		return null;
	}
	
	@Override
	public boolean hasPath(World world, Coord start, Coord goal)
	{
		return getPath(world, start, goal) != null;
	}
}
