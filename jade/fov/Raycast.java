package jade.fov;

import jade.core.World;
import jade.path.Bresenham;
import jade.util.Coord;
import jade.util.Tools;
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
	private final boolean circular;

	protected Raycast(boolean circular)
	{
		this.circular = circular;
	}

	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		final Collection<Coord> result = new TreeSet<Coord>();
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
			Tools.filterCircle(result, x, y, range);
		return result;
	}
}
