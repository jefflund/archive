package rl.creature;

import jade.core.Console;
import jade.core.World;
import jade.fov.FoV;
import jade.fov.FoV.FoVFactory;
import jade.util.Coord;
import jade.util.Tools;
import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import rl.item.Item;
import rl.item.Item.Slot;
import rl.magic.Spell;
import rl.world.Dungeon;

public class Player extends Creature
{
	private Console console;
	private List<Item> inventory;
	private Map<Slot, Item> equipment;
	private List<Spell> spellbook;
	private Collection<Coord> fov;
	private Dungeon dungeon;

	public Player(Console console, Dungeon dungeon)
	{
		super('@', Color.white);
		this.console = console;
		this.dungeon = dungeon;
		inventory = new LinkedList<Item>();
		equipment = new HashMap<Slot, Item>();
		spellbook = new LinkedList<Spell>();
		spellbook.add(new Spell(this, 15));
	}

	public void act()
	{
		char key = '\0';
		boolean moved = false;
		while(!moved)
		{
			key = console.getKey();
			moved = true;
			switch(key)
			{
			case 'q':
				expire();
				break;
			case 'p':
				spellbook();
				moved = false;
				break;
			case 'm':
				cast();
				break;
			case 'i':
				inventory();
				moved = false;
				break;
			case 'g':
				get();
				break;
			case 'd':
				drop();
				break;
			case 'e':
				equipment();
				moved = false;
				break;
			case 'w':
				equip();
				break;
			case 't':
				unequip();
				break;
			case '>':
				descend();
				break;
			default:
				Coord dir = Tools.keyToDir(key, true, false);
				if(dir != null)
					super.move(dir.x(), dir.y());
				else
					moved = false;
				break;
			}
		}
		calcFoV();
	}

	private void descend()
	{
		dungeon.descend();
		world().removeActor(this);
		dungeon.getLevel().addActor(this, new Random());
	}

	private void calcFoV()
	{
		fov = FoVFactory.get(FoV.SquareRay).calcFoV(world(), x(), y(), 5);
	}

	public Collection<Coord> getFoV()
	{
		return fov;
	}

	public void setWorld(World world)
	{
		super.setWorld(world);
	}

	private int spellbook()
	{
		return choose(spellbook, "Spellbook");
	}

	private int choose(Collection elements, String title)
	{
		console.saveBuffer();
		console.buffString(0, 0, title, Color.gray);
		int i = 0;
		for(Object element : elements)
		{
			console.buffString(0, i + 1, Tools.intToAlpha(i) + " " + element,
			    Color.gray);
			i++;
		}
		console.repaint();
		int result = Tools.alphaToInt(console.getKey());
		console.repaint();
		return result;
	}

	private void cast()
	{
		int index = spellbook();
		if(index < 0 || index >= spellbook.size())
			appendMessage("Invalid selection");
		else
		{
			spellbook.get(index).cast();
		}
	}

	private int equipment()
	{
		return choose(equipment.values(), "Equipment");
	}

	private void equip()
	{
		int index = inventory();
		if(index < 0 || index >= inventory.size())
			appendMessage("Invalid selection");
		else
		{
			Item item = inventory.get(index);
			if(equipment.get(item.slot()) != null)
				appendMessage(equipment.get(item.slot()) + " already equiped");
			else
			{
				inventory.remove(item);
				equipment.put(item.slot(), item);
				appendMessage(item + " equiped");
			}
		}
	}

	private void unequip()
	{
		
	}

	private int inventory()
	{
		return choose(inventory, "Inventory");
	}

	private void get()
	{
		Item item = (Item)world().getActorAt(x(), y(), Item.class);
		if(item != null)
		{
			item.attachTo(this);
			inventory.add(item);
			appendMessage(this + " picks up " + item);
		}
		else
			appendMessage("Nothing to pick up");
	}

	private void drop()
	{
		int index = inventory();
		if(index < 0 || index >= inventory.size())
			appendMessage("Invalid selection");
		else
		{
			Item item = inventory.get(index);
			item.detachFrom();
			inventory.remove(item);
			appendMessage(this + " drops " + item);
		}
	}
}
