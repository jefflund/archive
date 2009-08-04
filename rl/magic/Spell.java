package rl.magic;

import jade.util.Coord;
import java.io.Serializable;
import rl.creature.Creature;
import rl.magic.Instant.Effect;

public class Spell implements Serializable
{
	public enum Target
	{
		SELF, AREA, OTHER
	};

	private Creature caster;
	private Effect effect;
	private Target target;
	private int magnitude;
	private int duration;
	private int cost;
	private String name;

	public Spell(Creature caster, Effect effect, Target target, int magnitude,
			int duration, int cost, String name)
	{
		this.caster = caster;
		this.effect = effect;
		this.target = target;
		this.magnitude = magnitude;
		this.duration = duration;
		this.cost = cost;
		this.name = name;
	}

	public void cast()
	{
		if(caster.mp().value() < cost)
			caster.appendMessage("Insufficient mana");
		else
		{
			Weave weave = new Weave(effect, magnitude, duration);
			switch(target)
			{
			case AREA:
				Coord area = caster.getTarget();
				caster.world().addActor(weave, area);
				break;
			case SELF:
				weave.attachTo(caster);
				break;
			case OTHER:
				Creature other = caster.world().getActorAt(caster.getTarget(),
						Creature.class);
				if(other != null)
					weave.attachTo(other);
				break;
			}
			caster.mp().buff(-cost);
			caster.mp().train();
		}
	}

	public String toString()
	{
		return name;
	}
}
