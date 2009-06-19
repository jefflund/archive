package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;

/**
 * This implementation of FoV uses Precise Permissive Field of View. This
 * algorithm allows for peaking around corners, and may allow for more realistic
 * lighting.
 */
public class Permissive implements FoV
{
	protected Permissive()
	{
		throw new UnsupportedOperationException("Permissive not implemented yet");
	}

	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		return null;
	}
}
