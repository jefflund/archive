package jade.aim;

import jade.core.Actor;
import jade.core.Console;
import jade.core.Console.Camera;
import jade.util.Coord;
import jade.util.Tools;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *	An implementation of Aim in which the user selects targets by letter.
 *	The target type must be set (default is Actor). 
 */
public class Letter implements Aim
{
	private Class<? extends Actor> cls;

	/**
	 * Returns a new Letter with Actor as its target type
	 */
	public Letter()
	{
		setTargetType(Actor.class);
	}

	/**
	 * Set the target type
	 */
	public void setTargetType(Class<? extends Actor> cls)
	{
		this.cls = cls;
	}

	/**
	 * Replaces all Actors of the target type with letters and lets the user select one
	 */
	public Coord getAim(Console console, Camera camera)
	{
		console.saveBuffer();
		List<Coord> targets = new ArrayList<Coord>();
		for(Coord coord : camera.getFoV())
		{
			if(camera.world().getActorAt(coord, cls) != null)
				targets.add(coord);
		}
		for(int i = 0; i < targets.size(); i++)
		{
			console.buffRelCamera(camera, targets.get(i), Tools.intToAlpha(i),
					Color.white);
		}
		console.refreshScreen();
		int selection = Tools.alphaToInt(console.getKey());
		console.recallBuffer();
		if(selection < 0 || selection >= targets.size())
			return null;
		else
			return targets.get(selection);
	}
}
