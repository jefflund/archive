package jade.fov;

import jade.core.World;
import jade.path.Bresenham;
import jade.util.Coord;
import java.util.Collection;
import java.util.TreeSet;

/**
 * This implementation of FoV uses raycasting with a square range limit. It is
 * fast and simple. It works well when the screen is always centered on the
 * source of the field of vision.
 */
public class SquareRay extends Bresenham implements FoV
{
	protected SquareRay()
	{
	}

	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		Collection<Coord> result = new TreeSet<Coord>();
		result.add(new Coord(x, y));
		for(int dx = x - range; dx <= x + range; dx++)
		{
			result.addAll(castray(world, x, y, dx, y - range));
			result.addAll(castray(world, x, y, dx, y + range));
		}
		for(int dy = y - range; dy <= y + range; dy++)
		{
			result.addAll(castray(world, x, y, x + range, dy));
			result.addAll(castray(world, x, y, x - range, dy));
		}
		return result;
	}
}
