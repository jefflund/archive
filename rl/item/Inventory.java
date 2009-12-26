package rl.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rl.creature.Creature;
import rl.item.Item.Slot;

public class Inventory
{
	private Creature owner;
	private List<Item> inventory;
	private Map<Slot, Item> equipment;

	public Inventory(Creature owner)
	{
		this.owner = owner;
		inventory = new ArrayList<Item>();
		equipment = new HashMap<Slot, Item>();
	}

	public List<Item> getInventory()
	{
		return inventory;
	}

	public List<Item> getInventory(Slot slot)
	{
		List<Item> items = new ArrayList<Item>();
		for(Item item : inventory)
			if(item.slot() == slot)
				items.add(item);
		return items;
	}

	public List<Item> getEquipment()
	{
		return new ArrayList<Item>(equipment.values());
	}

	public List<Item> getEquipable()
	{
		List<Item> equipable = new ArrayList<Item>();
		for(Item item : inventory)
			if(item.isEquipment())
				equipable.add(item);
		return equipable;
	}

	public Item getEquiped(Slot slot)
	{
		return equipment.get(slot);
	}

	public boolean pickup()
	{
		Item item = owner.world().getActorAt(owner.pos(), Item.class);
		if(item == null)
		{
			owner.appendMessage("Nothing to pick up");
			return false;
		}
		else
		{
			item.attachTo(owner);
			inventory.add(item);
			owner.appendMessage(owner + " picks up " + item);
			return true;
		}
	}

	public boolean drop(Item item)
	{
		if(item == null)
		{
			owner.appendMessage("Invalid selection");
			return false;
		}
		else
		{
			item.detachFrom();
			inventory.remove(item);
			owner.appendMessage(owner + " drops " + item);
			return true;
		}
	}

	public boolean equip(Item item)
	{
		if(item == null || !item.isEquipment())
		{
			owner.appendMessage("Invalid selection");
			return false;
		}
		else if(equipment.get(item.slot()) != null)
		{
			owner.appendMessage(item.slot() + " already equiped");
			return false;
		}
		else if(handsOccupied(item.slot()))
		{
			owner.appendMessage("Hands are occupied");
			return false;
		}
		else
		{
			inventory.remove(item);
			equipment.put(item.slot(), item);
			item.activateEnchant();
			owner.appendMessage(item + " equiped");
			return true;
		}
	}

	private boolean handsOccupied(Slot slot)
	{
		if(slot == Slot.Weapon)
			return equipment.containsKey(Slot.Bow);
		else if(slot == Slot.Bow)
			return equipment.containsKey(Slot.Weapon);
		else
			return true;
	}

	public boolean unequip(Item item)
	{
		if(item == null || !equipment.containsValue(item))
		{
			owner.appendMessage("Invalid selection");
			return false;
		}
		else
		{
			equipment.remove(item.slot());
			inventory.add(item);
			item.deactivateEnchant();
			owner.appendMessage(item + " is removed");
			return true;
		}
	}
}
