package rl.magic;

import java.io.Serializable;

import rl.creature.Creature;

public class Spell implements Serializable
{
	private Creature caster;
	private int duration;
	
	public Spell(Creature caster, int duration)
	{
		this.caster = caster;
		this.duration = duration;
	}
	
	public void cast()
	{
		Instant instant = new Instant();
		caster.world().addActor(new Weave(instant, duration), caster.x() + 1, caster.y());
	}
	
	public String toString()
	{
		return "trap:" + duration;
	}
}
