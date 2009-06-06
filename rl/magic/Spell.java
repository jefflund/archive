package rl.magic;

import rl.creature.Creature;

public class Spell
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
		caster.world().addActor(new Weave(duration), caster.x(), caster.y());
	}
}
