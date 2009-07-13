package rl.magic;

import java.io.Serializable;
import rl.creature.Creature;

public class Instant implements Serializable
{
	public enum Effect
	{
		FIRE
	};

	private Effect effect;
	private int magnitude;

	public Instant(Effect effect, int magnitude)
	{
		this.effect = effect;
		this.magnitude = magnitude;
	}

	public void doIt(Creature target)
	{
		switch(effect)
		{
		case FIRE:
			fire(target);
			break;
		}
	}

	private void fire(Creature target)
	{
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