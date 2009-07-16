package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;

public interface Camera
{
	public Collection<Coord> getFoV();
	public World world();
	public int x();
	public int y();
}
