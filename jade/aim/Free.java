package jade.aim;

import jade.core.Console;
import jade.core.Console.Camera;
import jade.util.Coord;
import jade.util.Direction;
import jade.util.Tools;
import java.awt.Color;
import java.util.Collection;

/**
 * An implementation of Aim allowing selection from the camera's FoV.
 */
public class Free implements Aim
{
	public Coord getAim(Console console, Camera camera)
	{
		console.saveBuffer();
		Coord target = new Coord(camera.x(), camera.y());
		Collection<Coord> fov = camera.getFoV();
		char key = '\0';
		while(key != 't')
		{
			console.recallBuffer();
			console.buffRelCamera(camera, target, '*', Color.white);
			console.refreshScreen();
			key = console.getKey();
			Direction dir = Tools.keyToDir(key, true, false);
			if(dir != null)
			{
				target.translate(dir);
				if(!fov.contains(target))
					target.translate(dir.opposite());
			}
		}
		console.recallBuffer();
		console.refreshScreen();
		return target;
	}
}
