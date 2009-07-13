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
		FIRE(Color.red, false), ELEC(Color.yellow, false),
		STONEFALL(Color.gray, false), CHANNEL(Color.white, true);

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

	private Effect effect;
	private int magnitude;
	private Set<Creature> undoList;

	public Instant(Effect effect, int magnitude)
	{
		this.effect = effect;
		this.magnitude = magnitude;
		undoList = new HashSet<Creature>();
	}

	public void doIt(int x, int y, World world)
	{
		Creature target = world.getActorAt(x, y, Creature.class);
		switch(effect)
		{
		case FIRE:
			fire(target);
			break;
		case ELEC:
			elec(target);
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
		if(!effect.undoNeeded())
			return;
		switch(effect)
		{
		case CHANNEL:
			undoChannel();
			break;
		default:
			break;
		}
		undoList.clear();
	}

	private void undoChannel()
	{
		for(Creature target : undoList)
			target.mpFlow(-magnitude);
	}
	
	private void channel(Creature target)
	{
		if(target == null || undoList.contains(target))
			return;
		target.mpFlow(magnitude);
		undoList.add(target);
	}

	private void stonefall(int x, int y, World world)
	{
		if(!world.passable(x, y))
			world.tile(x, y).setTile('.', Color.gray, true);
	}

	private void elec(Creature target)
	{
		if(target == null)
			return;
		int damage = magnitude - target.relec();
		if(damage > 0)
		{
			target.hpHurt(damage);
			if(target.hp() < 0)
				target.appendMessage(target + " is fried");
			else
				target.appendMessage(target + " is shocked");
		}
		else
			target.appendMessage(target + " resist shock");
	}

	private void fire(Creature target)
	{
		if(target == null)
			return;
		int damage = magnitude - target.rfire();
		if(damage > 0)
		{
			target.hpHurt(damage);
			if(target.hp() < 0)
				target.appendMessage(target + " is consumed");
			else
				target.appendMessage(target + " burns");
		}
		else
			target.appendMessage(target + " resists fire");
	}
}

// * Fire
// * fire dmg => nova
// * fire res => fire immune
// * Lightning
// * lightning dmg => animate lightning
// * res => lightning immune
// * Earth
// * collapse => build
// * sheild => physical immune
// * Air
// * poison => heal
// * deflect => reflect
// * Mana
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