package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;

/**
 * This implementation of FoV uses a recursive shadowcasting algorithm. This
 * algorithm has the advantage of raycasting in that it does need to visit near
 * tiles multiple times.
 */
public class Shadowcast implements FoV
{
	protected Shadowcast()
	{
		throw new UnsupportedOperationException("Shadowcast not implemented yet");
	}

	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		return null;
	}
}
