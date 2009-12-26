package rl.creature;

import jade.core.Actor;
import jade.util.Coord;
import jade.util.Dice;
import java.awt.Color;
import rl.creature.Monster.Prototype;
import rl.world.Level;

public abstract class Creature extends Actor
{
	private Stat hp;
	private Stat mp;
	private Stat atk;
	private Stat def;
	private Stat dmg;
	private Stat fireRes;

	public Creature(Prototype type)
	{
		this(type.face, type.color, type.hp, type.mp, type.atk, type.def, type.dmg,
				type.fireRes);
	}

	protected Creature(char face, Color color, int hp, int mp, int atk, int def,
			int dmg, int fireRes)
	{
		super(face, color);
		this.hp = getNewHP(hp);
		this.mp = new Stat(mp);
		this.atk = new Stat(atk);
		this.def = new Stat(def);
		this.dmg = new Stat(dmg);
		this.fireRes = new Stat(fireRes);
	}

	@Override
	public void move(int dx, int dy)
	{
		int x = x() + dx;
		int y = y() + dy;
		Creature bumped = world().getActorAt(x, y, Creature.class);
		if(bumped != null && bumped != this)
			melee(bumped);
		else if(world().passable(x, y))
			super.move(dx, dy);
	}

	public Level world()
	{
		return (Level)super.world();
	}

	private void melee(Creature bumped)
	{
		int oldHp = bumped.hp.value();
		float chance = (float)atk.value() / (atk.value() + bumped.def.value());
		while(Dice.dice.nextFloat() < chance)
			bumped.hp.modifyValue(-dmg.value());
		if(oldHp == bumped.hp.value())
			appendMessage(this + " misses " + bumped);
		else
		{
			String verb = !bumped.isExpired() ? " hits " : " slays ";
			appendMessage(this + verb + bumped);
		}
	}

	public abstract Coord getTarget();

	public Stat hp()
	{
		return hp;
	}

	public Stat mp()
	{
		return mp;
	}

	public Stat atk()
	{
		return atk;
	}

	public Stat def()
	{
		return def;
	}

	public Stat dmg()
	{
		return dmg;
	}

	public Stat fireRes()
	{
		return fireRes;
	}

	private Stat getNewHP(int hp)
	{
		return new Stat(hp)
		{
			@Override
			public void modifyValue(int change)
			{
				super.modifyValue(change);
				if(value() < 0)
					expire();
			}
		};
	}
}
