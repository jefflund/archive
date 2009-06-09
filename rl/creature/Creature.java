package rl.creature;

import java.awt.Color;
import jade.core.Actor;

public abstract class Creature extends Actor
{
	public Creature(char face, Color color)
	{
		super(face, color);
	}

	public void move(int dx, int dy)
	{
		int x = x() + dx;
		int y = y() + dy;
		Creature bumped = world().getActorAt2(x, y, Creature.class);
		if(bumped != null && bumped != this)
			attack(bumped);
		else if(world().passable(x() + dx, y() + dy))
			super.move(dx, dy);
	}

	private void attack(Creature bump)
	{
		appendMessage(this + " attacks " + bump);
	}
}
