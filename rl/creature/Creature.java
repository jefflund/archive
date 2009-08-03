package rl.creature;

import jade.core.Actor;
import jade.util.Coord;
import jade.util.Dice;
import java.awt.Color;
import java.io.Serializable;

public abstract class Creature extends Actor implements Serializable
{
	private static final float XP_ON_SUCCEED = .01f;
	private Stat hp;
	private Stat mp;
	private Stat atk;
	private Stat def;
	private Stat dmg;
	private Stat rfire;
	private Stat relec;
	protected Dice dice;

	public Creature(char face, Color color, int hp, int mp, int atk, int def,
			int dmg, int rfire, int relec)
	{
		super(face, color);
		this.hp = new Stat(hp)
		{
			public void buff(int buff)
			{
				super.buff(buff);
				if(value() < 0)
					expire();
			}
		};
		this.mp = new Stat(mp);
		this.atk = new Stat(atk);
		this.def = new Stat(def);
		this.dmg = new Stat(dmg);
		this.rfire = new Stat(rfire);
		this.relec = new Stat(relec);
		dice = new Dice();
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

	public abstract Coord getTarget();

	private void attack(Creature bump)
	{
		int hp = bump.hp().value();
		while(dice.nextFloat() < (float)atk.value / (atk.value + bump.def.value))
			bump.hp().buff(-dmg.value());
		if(hp == bump.hp().value())
		{
			appendMessage(this + " misses " + bump);
			bump.def.train(XP_ON_SUCCEED);
		}
		else 
		{
			appendMessage(this + (bump.isExpired() ? " slays" : " hits ") + bump);
			atk.train(XP_ON_SUCCEED);
		}
	}

	public Stat hp()
	{
		return hp;
	}

	public Stat mp()
	{
		return mp;
	}

	public Stat def()
	{
		return def;
	}
	
	public Stat atk()
	{
		return atk;
	}
	
	public Stat dmg()
	{
		return dmg;
	}
	
	public Stat rfire()
	{
		return rfire;
	}
	
	public Stat relec()
	{
		return relec;
	}

	public class Stat implements Serializable
	{
		private int value;
		private int base;
		private float train;

		public Stat(int base)
		{
			this(base, base);
		}

		public Stat(int base, int value)
		{
			this.base = base;
			this.value = value;
			train = 0;
		}
		
		public int value()
		{
			return value;
		}

		public void train(float advance)
		{
			train += advance;
			while(train > 1)
			{
				base++ ;
				value++ ;
				train--;
			}
		}
		
		public void buff(int buff)
		{
			value += buff;
		}
		
		public void cappedBuff(int buff)
		{
			value = Math.min(value + buff, base);
		}
		
		public String toString()
		{
			return Integer.valueOf(value).toString();
		}
	}
}
