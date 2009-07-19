package rl.creature;

import jade.util.Coord;
import java.awt.Color;
import java.io.Serializable;
import rl.world.Level;

public class Monster extends Creature implements Serializable
{
	public Monster(char face, Color color)
	{
		super(face, color, 10, 0, 10, 10, 1, 0, 0);
	}

	@Override
	public void act()
	{
		if(player().getFoV().contains(new Coord(x(), y())))
			appendMessage(this + " sees " + player());
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
