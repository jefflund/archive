package rl.item;

import jade.core.Actor;
import java.awt.Color;
import java.io.Serializable;
import rl.creature.Creature;
import rl.creature.Player;
import rl.magic.Instant;
import rl.magic.Instant.Effect;

public class Item extends Actor implements Serializable
{
	public enum Type
	{
		WEAPON(true), ARMOR(true), SHIELD(true), SCROLL(false), POTION(false);

		private boolean isEquipment;

		private Type(boolean isEquipment)
		{
			this.isEquipment = isEquipment;
		}
	};

	private final Type type;
	private int modifier;
	private final Effect effect;
	private Instant enchant;
	private boolean equiped;

	public Item(char face, Color color, Type type, int modifier, Effect effect,
			int magnitude)
	{
		super(face, color);
		this.type = type;
		this.modifier = modifier;
		this.effect = effect;
		if(effect != null)
			enchant = new Instant(effect, magnitude);
		equiped = false;
	}

	@Override
	public void act()
	{
		if(isEquipment())
			handleEquipment();
		else
			switch(type)
			{
			case POTION:
				consume();
				break;
			case SCROLL:
				read();
				break;
			}
	}

	private void read()
	{
		world().getActorAt(x(), y(), Player.class).learn(effect);
	}

	private void consume()
	{
		assert (!effect.undoNeeded());
		enchant.doIt(x(), y(), world());
		expire();
	}

	private void handleEquipment()
	{
		if(equiped)
		{
			debuffStats();
			if(enchant != null)
				enchant.undoIt();
		}
		else
		{
			buffStats();
			if(enchant != null)
				enchant.doIt(x(), y(), world());
		}
		equiped = !equiped;
	}

	private void buffStats()
	{
		switch(type)
		{
		case ARMOR:
			owner().def().buff(modifier);
			break;
		case SHIELD:
			owner().def().buff(modifier);
			break;
		case WEAPON:
			owner().dmg().buff(modifier);
			break;
		}
	}

	private void debuffStats()
	{
		modifier *= -1;
		buffStats();
		modifier *= -1;
	}

	private Creature owner()
	{
		return world().getActorAt(x(), y(), Creature.class);
	}

	public Type type()
	{
		return type;
	}

	public boolean isEquipment()
	{
		return type.isEquipment;
	}
}
