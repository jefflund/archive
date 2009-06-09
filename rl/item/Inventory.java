package rl.item;

import java.util.HashMap;
import java.util.LinkedList;
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
		inventory = new LinkedList<Item>();
		equipment = new HashMap<Slot, Item>();
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
		if(item == null)
			owner.appendMessage("Invalid selection");
		else
		{
			if(equipment.get(item.slot()) != null)
				owner.appendMessage(equipment.get(item.slot()) + " already equiped");
			else
			{
				inventory.remove(item);
				equipment.put(item.slot(), item);
				owner.appendMessage(item + " equiped");
			}
		}
	}

	public void unequip(Item item)
	{
		if(item == null)
			owner.appendMessage("Invalid selection");
	}

	public void get()
	{
		Item item = owner.world().getActorAt2(owner.x(), owner.y(), Item.class);
		if(item != null)
		{
			item.attachTo(owner);
			inventory.add(item);
			owner.appendMessage(owner + " picks up " + item);
		}
		else
			owner.appendMessage("Nothing to pick up");
	}

	public void drop(Item item)
	{
		if(item == null)
			owner.appendMessage("Invalid selection");
		else
		{
			item.detachFrom();
			inventory.remove(item);
			owner.appendMessage(owner + " drops " + item);
		}
	}
}
