package jade.rl;

import jade.core.Actor;
import java.awt.Color;

public abstract class Creature extends Actor
{
	public Creature(char face, Color color)
	{
		super(face, color);
	}
	
	public void move(int dx, int dy)
	{
		if(world().passable(x() + dx, y() + dy))
			super.move(dx, dy);
	}
}
