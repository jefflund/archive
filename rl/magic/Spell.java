package rl.magic;

import jade.util.type.Coord;
import rl.creature.Creature;
import rl.magic.Weave.Effect;

public class Spell
{
	public enum Target
	{
		SELF, OTHER, AREA
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
		case SELF:
			weave.attachTo(caster);
			break;
		case OTHER:
			Coord target = caster.getTarget(Creature.class);
			if(target == null)
			{
				caster.appendMessage("Spell cancelled");
				return false;
			}
			Creature other = caster.world().getActorAt(target, Creature.class);
			if(other != null)
				weave.attachTo(other);
			break;
		case AREA:
			Coord area = caster.getTarget();
			if(area == null)
			{
				caster.appendMessage("Spell cancelled");
				return false;
			}
			caster.world().addActor(weave, area);
			break;
		}
		caster.mp().modifyValue(-cost);
		caster.mp().train(.05f);
		return true;
	}

	public String toString()
	{
		return effect + " for " + duration + " on " + target;
	}
}
