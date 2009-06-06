package rl.creature;

import jade.core.Console;
import jade.util.Coord;
import jade.util.Tools;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import rl.item.Item;
import rl.magic.Spell;

public class Player extends Creature
{
	private Console console;
	private List<Item> inventory;
	private List<Item> equipment;
	private List<Spell> spellbook;

	public Player(Console console)
	{
		super('@', Color.white);
		this.console = console;
		inventory = new LinkedList<Item>();
		equipment = new LinkedList<Item>();
		spellbook = new LinkedList<Spell>();
		spellbook.add(new Spell(this, 5));
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
			case 'b':
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
			default:
				Coord dir = Tools.keyToDir(key, true, false);
				if(dir != null)
					super.move(dir.x(), dir.y());
				else
					moved = false;
				break;
			}
		}
	}
	
	private int spellbook()
	{
		console.saveBuffer();
		console.buffString(0, 0, "Spellbook", Color.gray);
		for(int i = 0; i < spellbook.size(); i++)
			console.buffString(0, i + 1, Tools.intToAlpha(i) + " " + spellbook.get(i),
			    Color.gray);
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
		console.saveBuffer();
		console.buffString(0, 0, "Equipment", Color.gray);
		for(int i = 0; i < equipment.size(); i++)
			console.buffString(0, i + 1, Tools.intToAlpha(i) + " " + equipment.get(i),
			    Color.gray);
		console.repaint();
		int result = Tools.alphaToInt(console.getKey());
		console.repaint();
		return result;		
  }
	
	private void equip()
  {
		int index = inventory();
		if(index < 0 || index >= inventory.size())
			appendMessage("Invalid selection");
		else
		{
			Item item = inventory.remove(index);
			equipment.add(item);
			appendMessage(item + " equiped");
		}
  }
	
	private void unequip()
  {
		int index = equipment();
		if(index < 0 || index >= equipment.size())
			appendMessage("Invalid selection");
		else
		{
			Item item = equipment.remove(index);
			inventory.add(item);
			appendMessage(item + " unequiped");
		}
  }

	private int inventory()
	{
		console.saveBuffer();
		console.buffString(0, 0, "Inventory", Color.gray);
		for(int i = 0; i < inventory.size(); i++)
			console.buffString(0, i + 1, Tools.intToAlpha(i) + " " + inventory.get(i),
			    Color.gray);
		console.repaint();
		int result = Tools.alphaToInt(console.getKey());
		console.repaint();
		return result;
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
