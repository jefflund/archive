package rl.creature;

import java.awt.Color;
import java.io.Serializable;
import jade.core.Actor;

public abstract class Creature extends Actor implements Serializable
{
	protected Stat hp;
	
	public Creature(char face, Color color, int hp)
	{
		super(face, color);
		this.hp = new Stat(hp);
	}

	@Override
	public void move(int dx, int dy)
	{
		int x = x() + dx;
		int y = y() + dy;
		Creature bumped = world().getActorAt(x, y, Creature.class);
		if(bumped != null && bumped != this)
			attack(bumped);
		else if(world().passable(x() + dx, y() + dy))
			super.move(dx, dy);
	}

	private void attack(Creature bump)
	{
		bump.hurt(1);
		if(bump.isExpired())
			appendMessage(this + " slays " + bump);
		else
			appendMessage(this + " hits " + bump);
	}
	
	public void hurt(int damage)
	{
		hp.value -= damage;
		if(hp.value < 0)
			expire();
	}
	
	public void heal(int cure)
	{
		hp.value = Math.min(hp.value + 1, hp.base);
	}
	
	protected class Stat
	{
		public int value;
		public int base;
		
		public Stat(int base)
		{
			this.base = base;
			value = base;
		}
		
		public Stat(int base, int value)
		{
			this.base = base;
			this.value = value;
		}
	}
}
