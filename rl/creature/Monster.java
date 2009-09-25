package rl.creature;

import jade.util.Coord;
import java.awt.Color;
import java.io.Serializable;
import rl.world.Level;

public class Monster
		extends Creature implements Serializable
{
	public Monster(char face, Color color)
	{
		super(face, color, 10, 0, 10, 10, 1, 0, 0);
	}

	@Override
	public void act()
	{
		boolean sees = player().getFoV().contains(new Coord(x(), y()));
		if(sees)
		{
			int dx = player().x() < x() ? -1 : player().x() > x() ? 1 : 0;
			int dy = player().y() < y() ? -1 : player().y() > y() ? 1 : 0;
			move(dx, dy);
		}
		else
			move(dice.nextInt(-1, 1), dice.nextInt(-1, 1));
	}
	@Override
	public Coord getTarget()
	{
		return new Coord(x(), y());
	}

	private Player player()
	{
		return ((Level)world()).player();
	}
}
