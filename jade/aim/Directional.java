package jade.aim;

import jade.aim.Aim.BaseAim;
import jade.core.Console;
import jade.core.Console.Camera;
import jade.util.Tools;
import jade.util.type.Coord;
import jade.util.type.Direction;

/**
 *	Allows the user to select the closest target in 
 */
public class Directional extends BaseAim
{
	public Coord getAim(Console console, Camera camera)
	{
		Direction dir = null;
		while(dir == null || dir == Direction.O)
		{
			char key = console.getKey();
			if(key == Console.ESC)
				return null;
			dir = Tools.keyToDir(key, true, false);
		}
		Coord target = null;
		double dist = 0;
		Coord start = new Coord(camera.x(), camera.y());
		for(Coord coord : getTargets(camera))
			if((start.directionTo(coord) == dir)
					&& (target == null || start.distTo(coord) < dist))
			{
				target = coord;
				dist = start.distTo(target);
			}
		return target;
	}
}
