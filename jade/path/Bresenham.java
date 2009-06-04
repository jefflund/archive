package jade.path;

import jade.core.World;
import jade.util.Coord;
import java.util.LinkedList;
import java.util.List;

public class Bresenham implements Path
{
	public List<Coord> getPath(World world, Coord start, Coord goal)
	{
		int x1 = start.x();
		int y1 = start.y();
		int x2 = goal.x();
		int y2 = goal.y();
		List<Coord> path = new LinkedList<Coord>();
		boolean steep = Math.abs(y2 - y1) > Math.abs(x2 - x1);
		if(steep)
		{
			int temp = x1;
			x1 = y1;
			y1 = temp;
			temp = x2;
			x2 = y2;
			y2 = temp;
		}
		if(x1 > x2)
		{
			int temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}
		int dx = x2 - x1;
		int dy = Math.abs(y2 - y1);
		int err = dx / 2;
		int y = y1;
		int ystep = y1 < y2 ? 1 : -1;
		for(int x = x1; x <= x2; x++)
		{
			Coord coord = steep? new Coord(y, x) : new Coord(x, y);
			if(world.passable(coord))
				path.add(coord);
			else
				return null;
			err = err - dy;
			if(err < 0)
			{
				y = y + ystep;
				err = err + dx;
			}
		}
		return path;
	}

	public boolean hasPath(World world, Coord start, Coord goal)
	{
		return getPath(world, start, goal) != null;
	}
}
