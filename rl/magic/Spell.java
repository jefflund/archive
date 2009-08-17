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

	private final Creature caster;
	private final Effect effect;
	private final Target target;
	private final int magnitude;
	private final int duration;
	private final int cost;
	private final String name;

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
			final Weave weave = new Weave(effect, magnitude, duration);
			switch(target)
			{
			case AREA:
				final Coord area = caster.getTarget();
				caster.world().addActor(weave, area);
				break;
			case SELF:
				weave.attachTo(caster);
				break;
			case OTHER:
				final Creature other = caster.world().getActorAt(caster.getTarget(),
						Creature.class);
				if(other != null)
					weave.attachTo(other);
				break;
			}
			caster.mp().buff(-cost);
			caster.mp().train();
		}
	}

	@Override
	public String toString()
	{
		return name;
	}
}
