package rl.item;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import rl.creature.Creature;
import rl.item.Equipment.Slot;
import rl.item.Item.Type;

public class Inventory implements Serializable
{
	private final Creature owner;
	private final List<Item> inventory;
	private final Map<Slot, Equipment> equipment;

	public Inventory(Creature owner)
	{
		this.owner = owner;
		inventory = new LinkedList<Item>();
		equipment = new HashMap<Slot, Equipment>();
	}

	public void equip(Item item)
	{

		if(item == null || item.type() != Type.EQUIPMENT)
			owner.appendMessage("Invalid selection");
		else
		{
			Equipment equip = (Equipment)item;
			if(equipment.get(equip.slot()) != null)
				owner.appendMessage(equipment.get(equip.slot()) + " already equiped");
			else
			{
				inventory.remove(item);
				equipment.put(equip.slot(), equip);
				item.act();
				owner.appendMessage(equip + " equiped");
			}
		}
	}

	public void unequip(Item item)
	{
		if(item == null)
			owner.appendMessage("Invalid selection");
		else
		{
			Equipment equip = (Equipment)item;
			equipment.remove(equip.slot());
			inventory.add(item);
			equip.act();
			owner.appendMessage(owner + " unequips " + item);
		}
	}

	public void get()
	{
		final Item item = owner.world()
				.getActorAt(owner.x(), owner.y(), Item.class);
		if(item == null)
			owner.appendMessage("Nothing to pick up");
		else
		{
			item.attachTo(owner);
			inventory.add(item);
			owner.appendMessage(owner + " picks up " + item);
		}
	}

	public void drop(Item item)
	{
		drop(item, false);
	}

	private void drop(Item item, boolean suppressMsg)
	{
		if(item == null)
			owner.appendMessage(suppressMsg ? "" : "Invalid selection");
		else
		{
			item.detachFrom();
			inventory.remove(item);
			owner.appendMessage(suppressMsg ? "" : owner + " drops " + item);
		}
	}

	public List<Item> getTypedItems(Type type)
	{
		final List<Item> items = new LinkedList<Item>();
		for(final Item item : inventory)
			if(item.type() == type)
				items.add(item);
		return items;
	}
	
	public List<Item> getAllItems()
	{
		return inventory;
	}

	public List<Item> getEquiped()
	{
		return new LinkedList<Item>(equipment.values());
	}

	public void removeExpired()
	{
		final Collection<Item> expired = new HashSet<Item>();
		for(final Item item : inventory)
			if(item.isExpired())
				expired.add(item);
		for(final Item item : expired)
			drop(item, true);
	}
}
