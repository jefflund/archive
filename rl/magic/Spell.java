package rl.magic;

import java.io.Serializable;
import rl.creature.Creature;

public class Spell implements Serializable
{
	private Creature caster;
	private int duration;
	private int cost;

	public Spell(Creature caster, int duration, int cost)
	{
		this.caster = caster;
		this.duration = duration;
		this.cost = cost;
	}

	public void cast()
	{
		if(caster.mp() < cost)
			caster.appendMessage("Insufficent mana");
		else
		{
			caster.mpFlow(-cost);
			Instant instant = new Instant();
			caster.world().addActor(new Weave(instant, duration), caster.x() + 1,
					caster.y());
			caster.appendMessage(caster + " cast " + this);
		}
	}

	@Override
	public String toString()
	{
		return "trap:" + duration;
	}
}
