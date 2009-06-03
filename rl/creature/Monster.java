package rl.creature;

import jade.util.Dice;
import java.awt.Color;

public class Monster extends Creature
{
	private Dice dice;
	
	public Monster(char face, Color color)
	{
		super(face, color);
		dice = new Dice();
	}

	public void act()
	{
		move(dice.nextInt(-1, 1), dice.nextInt(-1, 1));
	}
}
