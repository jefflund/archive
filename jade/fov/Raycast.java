package jade.fov;

import jade.core.World;
import jade.path.Bresenham;
import jade.util.Coord;
import java.util.Collection;
import java.util.TreeSet;

/**
 * This implementation of FoV uses raycasting with a square range limit. It is
 * fast and simple. It works well when the screen is always centered on the
 * source of the field of vision. Optionally, the fov can be trimmed to a
 * circular radius.
 */
public class Raycast extends Bresenham implements FoV
{
	private boolean circular;

	protected Raycast(boolean circular)
	{
		this.circular = circular;
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
		if(circular)
			filterCircle(result, x, y, range);
		return result;
	}

	private void filterCircle(Collection<Coord> fov, int x, int y, int range)
	{
		Collection<Coord> out = new TreeSet<Coord>();
		range++;
		for(Coord coord : fov)
			if(!inCircle(x, y, coord.x(), coord.y(), range))
				out.add(coord);
		fov.removeAll(out);
	}

	private boolean inCircle(int x1, int y1, int x2, int y2, int r)
	{
		int x = x2 - x1;
		int y = y2 - y1;
		return x * x + y * y < r * r;
	}
}
