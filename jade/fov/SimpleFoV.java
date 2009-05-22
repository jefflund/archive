package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;
import java.util.TreeSet;

public class SimpleFoV implements FoV
{
	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		Collection<Coord> result = new TreeSet<Coord>();
		result.add(new Coord(x, y));
		for(int dx = x - range; dx <= x + range; dx++)
		{
			castray(x, y, dx, y - range, result, world);
			castray(x, y, dx, y + range, result, world);
		}
		for(int dy = y - range; dy <= y + range; dy++)
		{
			castray(x, y, x - range, dy, result, world);
			castray(x, y, x + range, dy, result, world);
		}
		return result;
	}

	private void castray(int x1, int y1, int x2, int y2, Collection<Coord> sees,
	    World world)
	{
		int dx = Math.abs(x2 - x1) << 1;
		int dy = Math.abs(y2 - y1) << 1;
		int ix = x2 > x1 ? 1 : -1;
		int iy = y2 > y1 ? 1 : -1;
		if(dx >= dy)
		{
			int error = dy - (dx >> 1);
			while(x1 != x2)
			{
				if(error >= 0 && (error != 0 || ix > 0))
				{
					y1 += iy;
					error -= dx;
				}
				x1 += ix;
				error += dy;
				sees.add(new Coord(x1, y1));
				if(!world.passable(x1, y1))
					break;
			}
		}
		else
		// dx < dy
		{
			int error = dx - (dy >> 1);
			while(y1 != y2)
			{
				if(error >= 0 && (error != 0 || iy > 0))
				{
					x1 += ix;
					error -= dy;
				}
				y1 += iy;
				error += dx;
				sees.add(new Coord(x1, y1));
				if(!world.passable(x1, y1))
					break;
			}
		}
	}// end castray
}
