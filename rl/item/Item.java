package rl.item;

import jade.core.Actor;
import java.awt.Color;
import rl.creature.Player;
import rl.prototype.IPrototype;

public class Item extends Actor
{
	public enum Slot
	{
		ARMOR(true), WEAPON(true), BOW(true), SCROLL(false);
		private boolean isEquipment;

		Slot(boolean isEquipment)
		{
			this.isEquipment = isEquipment;
		}
	}

	private Slot slot;
	private Enchantment enchantment;

	public Item(IPrototype prototype)
	{
		this(prototype.slot, prototype.face, prototype.color, prototype.enchantment);
	}

	protected Item(Slot slot, char face, Color color, Enchantment enchantment)
	{
		super(face, color);
		this.slot = slot;
		if(enchantment != null)
			enchant(enchantment);
	}

	private void enchant(Enchantment enchantment)
	{
		this.enchantment = enchantment;
		enchantment.attachTo(this);
	}

	public Slot slot()
	{
		return slot;
	}

	@Override
	public void act()
	{
		if(slot == Slot.SCROLL)
		{
			if(enchantment != null)
			{
				Player player = world().getActorAt(pos(), Player.class);
				player.learnSpell(enchantment.effect());
				player.appendMessage(player + " learns " + enchantment.effect());
				appendMessage(this + " vanishes in a puff of smoke");
				expire();
			}
			else
				appendMessage("Scroll is blank");
		}
	}

	public boolean isEquipment()
	{
		return slot.isEquipment;
	}

	public void activateEnchant()
	{
		if(enchantment != null)
			enchantment.activate();
	}

	public void deactivateEnchant()
	{
		if(enchantment != null)
			enchantment.deactivate();
	}
}
