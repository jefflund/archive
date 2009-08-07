package jade.fov;

import jade.core.World;
import jade.util.Coord;
import jade.util.Tools;
import java.util.Collection;
import java.util.TreeSet;

/**
 * This implementation of FoV uses a recursive shadowcasting algorithm. This
 * algorithm has the advantage of raycasting in that it does need to visit near
 * tiles multiple times.
 */
public class Shadowcast implements FoV
{
	private final boolean circular;

	protected Shadowcast(boolean circular)
	{
		this.circular = circular;
	}

	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		final Collection<Coord> fov = new TreeSet<Coord>();
		final Coord orig = new Coord(x, y);
		fov.add(orig);
		for (int octant = 1; octant <= 8; octant++)
			scan(1, 1f, 0, orig, fov, world, range, octant);
		if (circular)
			Tools.filterCircle(fov, x, y, range);
		return fov;
	}

	private void scan(int depth, float startslope, float endslope, Coord orig,
			Collection<Coord> fov, World world, int range, int octant)
	{
		if (depth > range)
			return;
		int y = Math.round(startslope * depth);
		while (slope(depth, y) >= endslope)
		{
			final Coord curr = getCurr(orig, depth, y, octant);
			final Coord prev = getPrev(orig, depth, y, octant, world);
			if (world.passable(curr) && !world.passable(prev))
				startslope = newStartslope(depth, endslope, y);
			if (!world.passable(curr) && world.passable(prev))
				scan(depth + 1, startslope, newEndslope(depth, y), orig, fov, world,
						range, octant);
			fov.add(curr);
			y--;
		}
		y++;
		if (world.passable(getCurr(orig, depth, y, octant)))
			scan(depth + 1, startslope, endslope, orig, fov, world, range, octant);
	}

	private float newEndslope(int depth, int y)
	{
		return slope(depth - .5f, y + .5f);
	}

	private float newStartslope(int depth, float endslope, int y)
	{
		return Math.max(slope(depth + .5f, y - .5f), endslope);
	}

	private float slope(float x, float y)
	{
		if (x == 0)
			return Float.MAX_VALUE;
		return y / x;
	}

	private Coord getCurr(Coord orig, int x, int y, int octant)
	{
		switch (octant)
		{
		case 1:
			return new Coord(orig.x() + x, orig.y() + y);
		case 2:
			return new Coord(orig.x() + x, orig.y() - y);
		case 3:
			return new Coord(orig.x() + y, orig.y() + x);
		case 4:
			return new Coord(orig.x() - y, orig.y() + x);
		case 5:
			return new Coord(orig.x() - x, orig.y() + y);
		case 6:
			return new Coord(orig.x() - x, orig.y() - y);
		case 7:
			return new Coord(orig.x() + y, orig.y() - x);
		case 8:
			return new Coord(orig.x() - y, orig.y() - x);
		default:
			throw new IllegalArgumentException("octant must be between 1 and 8");
		}
	}

	private Coord getPrev(Coord orig, int x, int y, int octant, World world)
	{
		final Coord curr = getCurr(orig, x, y, octant);
		if (curr.x() == 0 || curr.y() == 0 || curr.x() == world.width - 1
				|| curr.y() == world.height - 1)
			return curr;
		switch (octant)
		{
		case 1:
			return curr.translate(0, 1);
		case 2:
			return curr.translate(0, -1);
		case 3:
			return curr.translate(1, 0);
		case 4:
			return curr.translate(-1, 0);
		case 5:
			return curr.translate(0, 1);
		case 6:
			return curr.translate(0, -1);
		case 7:
			return curr.translate(1, 0);
		case 8:
			return curr.translate(-1, 0);
		default:
			throw new IllegalArgumentException("octant must be between 1 and 8");
		}
	}
}
