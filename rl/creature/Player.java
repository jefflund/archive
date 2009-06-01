package rl.creature;

import jade.core.Console;
import jade.util.Coord;
import jade.util.Tools;
import java.awt.Color;

public class Player extends Creature
{
	private Console console;

	public Player(Console console)
	{
		super('@', Color.white);
		this.console = console;
	}

	public void act()
	{
		char key = '\0';
		boolean moved = false;
		while(!moved)
		{
			key = console.getKey();
			moved = true;
			switch(key)
			{
			case 'q':
				expire();
				break;
			default:
				Coord dir = Tools.keyToDir(key, true, false);
				if(dir != null)
					super.move(dir.x(), dir.y());
				else
					moved = false;
				break;
			}
		}
	}
}
