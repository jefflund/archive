package jade.fov;

import jade.core.Actor;
import jade.core.World;
import jade.path.Bresenham;
import jade.util.Coord;
import jade.util.Tools;
import java.util.Collection;
import java.util.TreeSet;

/**
 * An implementation of FoV that utilizes a simple raycasting algorithm.
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

	public Collection<Coord> calcFoV(Actor actor, int range)
	{
		return calcFoV(actor.world(), actor.x(), actor.y(), range);
	}
}
