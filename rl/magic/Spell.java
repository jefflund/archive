package rl.magic;

import java.io.Serializable;
import rl.creature.Creature;
import rl.magic.Instant.Effect;

public class Spell implements Serializable
{
	private Creature caster;
	private Effect effect;
	private int magnitude;
	private int duration;
	private int cost;
	private String name;

	public Spell(Creature caster, Effect effect, int magnitude, int duration,
			int cost, String name)
	{
		this.caster = caster;
		this.effect = effect;
		this.magnitude = magnitude;
		this.duration = duration;
		this.cost = cost;
		this.name = name;
	}

	public void cast()
	{
		if(caster.mp() < cost)
			caster.appendMessage("Insufficient mana");
		else
		{
			Weave weave = new Weave(effect, magnitude, duration);
			caster.world().addActor(weave, caster.x() + 1, caster.y());
			caster.mpFlow(-cost);
		}
	}
	
	public String toString()
	{
		return name;
	}
}
