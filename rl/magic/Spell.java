package rl.magic;

import jade.util.Coord;
import rl.creature.Creature;
import rl.magic.Weave.Effect;

public class Spell
{
	public enum Target
	{
		Self, Other, Area
	}

	private Effect effect;
	private int magnitude;
	private int duration;
	private Target target;
	private int cost;

	public Spell(Effect effect, int magnitude, int duration, Target target)
	{
		this.effect = effect;
		this.magnitude = magnitude;
		this.duration = duration;
		this.target = target;
		cost = 1;
	}

	public boolean cast(Creature caster)
	{
		if(caster.mp().value() < cost)
		{
			caster.appendMessage("Insufficient mana");
			return false;
		}
		Weave weave = new Weave(effect, magnitude, duration);
		switch(target)
		{
		case Self:
			weave.attachTo(caster);
			break;
		case Other:
			Coord target = caster.getTarget();
			Creature other = caster.world().getActorAt(target, Creature.class);
			if(other != null)
				weave.attachTo(other);
			break;
		case Area:
			Coord area = caster.getTarget();
			caster.world().addActor(weave, area);
			break;
		}
		caster.mp().modifyValue(-cost);
		return true;
	}

	public String toString()
	{
		return effect + " for " + duration + " on " + target;
	}
}
