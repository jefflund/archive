package rl.item;

import jade.core.Actor;
import java.awt.Color;

public class Item extends Actor
{
	public enum Slot
	{
		Armor(true), Weapon(true), Bow(true), Scroll(false);

		private boolean isEquipment;

		Slot(boolean isEquipment)
		{
			this.isEquipment = isEquipment;
		}
	}

	private Slot slot;
	private Enchantment enchantment;

	public Item(Slot slot, char face, Enchantment enchantment)
	{
		super(face, Color.white);
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
