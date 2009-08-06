package rl.item;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import rl.creature.Creature;
import rl.item.Item.Type;

public class Inventory implements Serializable
{
	private Creature owner;
	private List<Item> inventory;
	private Map<Type, Item> equipment;

	public Inventory(Creature owner)
	{
		this.owner = owner;
		inventory = new LinkedList<Item>();
		equipment = new HashMap<Type, Item>();
	}

	public List<Item> getEquiped()
	{
		return new LinkedList<Item>(equipment.values());
	}

	public List<Item> getItems()
	{
		return inventory;
	}

	public void equip(Item item)
	{
		if(item == null || !item.equipable())
			owner.appendMessage("Invalid selection");
		else
		{
			if(equipment.get(item.type()) != null)
				owner.appendMessage(equipment.get(item.type()) + " already equiped");
			else
			{
				inventory.remove(item);
				equipment.put(item.type(), item);
				item.onEquip(owner);
				owner.appendMessage(item + " equiped");
			}
		}
	}

	public void unequip(Item item)
	{
		if(item == null)
			owner.appendMessage("Invalid selection");
		else
		{
			equipment.remove(item.type());
			inventory.add(item);
			item.onUnequip(owner);
			owner.appendMessage(owner + " unequips " + item);
		}
	}

	public void get()
	{
		Item item = owner.world().getActorAt(owner.x(), owner.y(), Item.class);
		if(item == null)
			owner.appendMessage("Nothing to pick up");
		else
		{
			item.attachTo(owner);
			inventory.add(item);
			owner.appendMessage(owner + " picks up " + item);
		}
	}

	public void drop(Item item, boolean suppressMsg)
	{
		if(item == null)
		{
			if(!suppressMsg)
				owner.appendMessage("Invalid selection");
		}
		else
		{
			item.detachFrom();
			inventory.remove(item);
			if(!suppressMsg)
				owner.appendMessage(owner + " drops " + item);
		}
	}
	
	public List<Item> getTypedItems(Type type)
	{
		List<Item> items = new LinkedList<Item>();
		for(Item item : inventory)
			if(item.type() == type)
				items.add(item);
		return items;
	}
}
