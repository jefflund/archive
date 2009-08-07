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
		WEAPON(true), ARMOR(true), SCROLL(false), POTION(false);

		private boolean equipable;

		private Type(boolean equipable)
		{
			this.equipable = equipable;
		}
	};

	private final Type type;
	private int modifier;
	private Instant enchant;

	public Item(char face, Color color, Type type, int modifier, Effect effect,
			int magnitude)
	{
		super(face, color);
		this.type = type;
		this.modifier = modifier;
		if (effect != null)
			enchant = new Instant(effect, magnitude);
	}

	@Override
	public void act()
	{
		// only expendable items should act
		// note that this means that undoable effects will be permanent
		assert (!type.equipable);
		switch (type)
		{
		case POTION:
			if (enchant != null)
				enchant.doIt(x(), y(), world());
			break;
		case SCROLL:
			final Player player = world().getActorAt(x(), y(), Player.class);
			player.addEffect(enchant.effect());
			break;
		}
		expire();
	}

	public void onEquip(Creature owner)
	{
		buffStats(owner);
		if (enchant != null)
			enchant.doIt(x(), y(), world());
	}

	public void onUnequip(Creature owner)
	{
		debuffStats(owner);
		if (enchant != null)
			enchant.undoIt();
	}

	private void buffStats(Creature owner)
	{
		switch (type)
		{
		case ARMOR:
			owner.def().buff(modifier);
			break;
		case WEAPON:
			owner.dmg().buff(modifier);
			break;
		}
	}

	private void debuffStats(Creature owner)
	{
		modifier *= -1;
		buffStats(owner);
		modifier *= -1;
	}

	public Type type()
	{
		return type;
	}

	public boolean equipable()
	{
		return type.equipable;
	}
}
