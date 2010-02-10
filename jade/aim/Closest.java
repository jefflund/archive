package jade.aim;

import jade.aim.Aim.BaseAim;
import jade.core.Console;
import jade.core.Console.Camera;
import jade.util.Coord;
import java.util.List;

/**
 * An implementation of Aim in which the closest target is selected
 * automatically.
 */
public class Closest extends BaseAim
{
	public Coord getAim(Console console, Camera camera)
	{
		List<Coord> targets = getTargets(camera);
		Coord self = new Coord(camera.x(), camera.y());
		Coord target = null;
		for(Coord coord : targets)
		{
			if(coord.equals(self))
				continue;
			if(target == null || self.distTo(coord) < self.distTo(target))
				target = coord;
		}
		return target;
	}
}
