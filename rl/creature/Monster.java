package rl.creature;

import jade.util.Coord;
import jade.util.Dice;
import java.awt.Color;
import java.io.Serializable;
import rl.world.Level;

public class Monster extends Creature implements Serializable
{
	private Dice dice;

	public Monster(char face, Color color)
	{
		super(face, color);
		dice = new Dice();
	}

	public void act()
	{
		if(player().getFoV().contains(new Coord(x(), y())))
			appendMessage(this + " sees " + player());
		move(dice.nextInt(-1, 1), dice.nextInt(-1, 1));
	}

	private Player player()
	{
		return ((Level)world()).player();
	}
}
