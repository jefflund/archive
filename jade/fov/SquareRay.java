package jade.fov;

import jade.core.World;
import jade.path.Bresenham;
import jade.util.Coord;
import java.util.Collection;
import java.util.TreeSet;

public class SquareRay implements FoV
{
	private Bresenham raycaster;

	public SquareRay()
	{
		raycaster = new Bresenham();
	}

	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		Collection<Coord> result = new TreeSet<Coord>();
		result.add(new Coord(x, y));
		for(int dx = x - range; dx <= x + range; dx++)
		{
			result.addAll(raycaster.castray(world, x, y, dx, y - range));
			result.addAll(raycaster.castray(world, x, y, dx, y + range));
		}
		for(int dy = y - range; dy <= y + range; dy++)
		{
			result.addAll(raycaster.castray(world, x, y, x + range, dy));
			result.addAll(raycaster.castray(world, x, y, x - range, dy));
		}
		return result;
	}
}
