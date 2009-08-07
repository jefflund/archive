package rl.magic;

import jade.core.World;
import java.awt.Color;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import rl.creature.Creature;

public class Instant implements Serializable
{
	public enum Effect
	{
		FIRE(Color.red, false), RFIRE(Color.red, true), ELEC(Color.yellow, false), RELEC(
				Color.yellow, true), STONEFALL(Color.gray, false), CHANNEL(Color.white,
				true);

		private Color color;
		private boolean undo;

		private Effect(Color color, boolean undo)
		{
			this.color = color;
			this.undo = undo;
		}

		public Color color()
		{
			return color;
		}

		public boolean undoNeeded()
		{
			return undo;
		}
	};

	private final Effect effect;
	private int magnitude;
	private final Set<Creature> undoList;

	public Instant(Effect effect, int magnitude)
	{
		this.effect = effect;
		this.magnitude = magnitude;
		undoList = new HashSet<Creature>();
	}

	public void doIt(int x, int y, World world)
	{
		final Creature target = world.getActorAt(x, y, Creature.class);
		switch (effect)
		{
		case FIRE:
			fire(target);
			break;
		case RFIRE:
			rfire(target);
			break;
		case ELEC:
			elec(target);
			break;
		case RELEC:
			relec(target);
			break;
		case STONEFALL:
			stonefall(x, y, world);
			break;
		case CHANNEL:
			channel(target);
			break;
		}
	}

	public void undoIt()
	{
		if (!effect.undoNeeded())
			return;
		switch (effect)
		{
		case RFIRE:
			undorRfire();
			break;
		case RELEC:
			undoRelec();
			break;
		case CHANNEL:
			undoChannel();
			break;
		default:
			break;
		}
		undoList.clear();
	}

	public Effect effect()
	{
		return effect;
	}

	private void undoChannel()
	{
		for (final Creature target : undoList)
			target.mp().buff(-magnitude);
	}

	private void channel(Creature target)
	{
		if (target == null || undoList.contains(target))
			return;
		target.mp().buff(magnitude);
		undoList.add(target);
	}

	private void stonefall(int x, int y, World world)
	{
		if (!world.passable(x, y))
			world.tile(x, y).setTile('.', Color.gray, true);
	}

	private void elec(Creature target)
	{
		if (target == null)
			return;
		final int damage = magnitude - target.relec().value();
		if (damage > 0)
		{
			target.hp().buff(damage);
			if (target.hp().value() < 0)
				target.appendMessage(target + " is fried");
			else
				target.appendMessage(target + " is shocked");
		}
		else
			target.appendMessage(target + " resist shock");
	}

	private void relec(Creature target)
	{
		if (target == null)
			return;
		target.relec().buff(magnitude);
	}

	private void undoRelec()
	{
		magnitude *= -1;
		for (final Creature target : undoList)
			relec(target);
		magnitude *= -1;
	}

	private void fire(Creature target)
	{
		if (target == null)
			return;
		final int damage = magnitude - target.rfire().value();
		if (damage > 0)
		{
			target.hp().buff(-damage);
			target.appendMessage(target
					+ (target.isExpired() ? " is consumed" : " burns"));
		}
		else
			target.appendMessage(target + " resists fire");
	}

	private void rfire(Creature target)
	{
		if (target == null)
			return;
		target.rfire().buff(magnitude);
	}

	private void undorRfire()
	{
		magnitude *= -1;
		for (final Creature target : undoList)
			rfire(target);
		magnitude *= -1;
	}
}

// * Fire *
// * fire dmg => nova
// * fire res => fire immune
// * Lightning
// * lightning dmg => animate lightning
// * res => lightning immune
// * Earth *
// * collapse => build
// * sheild => physical immune
// * Air *
// * poison => heal
// * deflect => reflect
// * Mana *
// * channel => transfigure
// * pure dmg => undo creation
// * iterfere => nullify
// * amplify => combine
// *
// * Fire/Fire Elemental, Nova
// * Fire/Light
// * Fire/Earth Volcano, Transmute
// * Fire/Air Purge Plague
// * Fire/Mana
// * Light/Light Chain Lightning, Elemental
// * Light/Earth Enchant
// * Light/Air Storm, Revive
// * Light/Mana
// * Earth/Earth Statue, Elemental
// * Earth/Air Fertile Land
// * Earth/Mana
// * Air/Air Tornado, Elemental, Plague
// * Air/Mana Resurrect
// * Mana/Mana Invulnerability, Elemental