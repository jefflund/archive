package jade.aim;

import jade.aim.Aim.BaseAim;
import jade.core.Console;
import jade.core.Console.Camera;
import jade.util.Tools;
import jade.util.type.Coord;
import jade.util.type.Direction;
import java.awt.Color;
import java.util.List;

/**
 * Allows the user to select targets using directional chooses
 */
public class Select extends BaseAim
{
	public Coord getAim(Console console, Camera camera)
	{
		console.saveBuffer();
		List<Coord> targets = getTargets(camera);
		if(targets.isEmpty())
			return returnFail(console);
		Coord target = new Coord(camera.x(), camera.y());
		char key = '\0';
		while(key != 't')
		{
			console.recallBuffer();
			console.buffRelCamera(camera, target, '*', Color.white);
			console.refreshScreen();
			key = console.getKey();
			if(key == Console.ESC)
				return returnFail(console);
			Direction dir = Tools.keyToDir(key, true, false);
			if(dir != null)
			{
				Coord newTarget = null;
				for(Coord coord : targets)
				{
					if(coord.equals(target) || (dir.dx < 0 && coord.x() > target.x())
							|| (dir.dx > 0 && coord.x() < target.x())
							|| (dir.dy < 0 && coord.y() > target.y())
							|| (dir.dy > 0 && coord.y() < target.y()))
						continue;
					if(newTarget == null
							|| target.distTo(coord) < target.distTo(newTarget))
						newTarget = coord;
				}
				if(newTarget != null)
					target = newTarget;
			}
		}
		console.recallBuffer();
		console.refreshScreen();
		return target;
	}

	private Coord returnFail(Console console)
	{
		console.recallBuffer();
		console.refreshScreen();
		return null;
	}
}
