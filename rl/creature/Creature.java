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
		this.hp = new Stat(hp);
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
		int hp = bump.hp();
		while(dice.nextFloat() < (float)atk.value / (atk.value + bump.def.value))
			bump.hpHurt(dmg.value);
		if(hp == bump.hp())
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

	public void hpHurt(int damage)
	{
		hp.value -= damage;
		if(hp.value < 0)
			expire();
	}

	public void hpHeal(int cure)
	{
		hp.value = Math.min(hp.value + cure, hp.base);
	}

	public int hp()
	{
		return hp.value;
	}

	public void mpFlow(int flow)
	{
		mp.value += flow;
	}

	public void mpRestore(int restore)
	{
		if(mp.value < mp.base)
			mp.value += restore;
	}

	public int mp()
	{
		return mp.value;
	}

	public int rfire()
	{
		return rfire.value;
	}

	public void rfireBuff(int buff)
	{
		rfire.value += buff;
	}

	public int relec()
	{
		return relec.value;
	}

	public void relecBuff(int buff)
	{
		relec.value += buff;
	}

	public void defBuff(int modifier)
	{
		def.value += modifier;
	}

	public void dmgBuff(int modifier)
	{
		dmg.value += modifier;
	}

	protected class Stat implements Serializable
	{
		public int value;
		public int base;
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
	}
}
