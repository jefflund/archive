package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;
import java.util.TreeSet;

/**
 * This implementation of FoV uses raycasting with a circular range limit. It is
 * fast and simple.
 */
public class CircularRay extends SquareRay
{
	protected CircularRay()
	{
	}

	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		Collection<Coord> result = super.calcFoV(world, x, y, range);
		Collection<Coord> out = new TreeSet<Coord>();
		range++;
		for(Coord coord : result)
			if(!inCircle(x, y, coord.x(), coord.y(), range))
				out.add(coord);
		result.removeAll(out);
		return result;
	}

	private boolean inCircle(int x1, int y1, int x2, int y2, int r)
	{
		int x = x2 - x1;
		int y = y2 - y1;
		return x * x + y * y < r * r;
	}
}
