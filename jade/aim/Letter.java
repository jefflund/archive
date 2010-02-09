package jade.aim;

import jade.aim.Aim.BaseAim;
import jade.core.Console;
import jade.core.Console.Camera;
import jade.util.Coord;
import jade.util.Tools;
import java.awt.Color;
import java.util.List;

/**
 *	An implementation of Aim in which the user selects targets by letter.
 *	The target type must be set (default is Actor). 
 */
public class Letter extends BaseAim
{
	public Coord getAim(Console console, Camera camera)
	{
		List<Coord> targets = getTargets(camera);
		if(targets.isEmpty())
			return null;
		console.saveBuffer();
		for(int i = 0; i < targets.size(); i++)
		{
			console.buffRelCamera(camera, targets.get(i), Tools.intToAlpha(i),
					Color.white);
		}
		console.refreshScreen();
		int selection = Tools.alphaToInt(console.getKey());
		console.recallBuffer();
		console.refreshScreen();
		if(selection < 0 || selection >= targets.size())
			return null;
		else
			return targets.get(selection);
	}
}
