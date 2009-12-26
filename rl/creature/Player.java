package rl.creature;

import jade.core.Console;
import jade.core.Console.Camera;
import jade.fov.FoV.FoVFactory;
import jade.path.Path.PathFactory;
import jade.util.Coord;
import jade.util.Dice;
import jade.util.Direction;
import jade.util.Tools;
import java.awt.Color;
import java.util.Collection;
import java.util.List;
import rl.item.Inventory;
import rl.item.Item;
import rl.item.Item.Slot;
import rl.magic.Spell;
import rl.magic.Spellbook;

public class Player extends Creature implements Camera
{
	public static final int VISION = 5;
	private static final float REGEN = .05f;
	private Console console;
	private Collection<Coord> fov;
	private Spellbook spellbook;
	private Inventory inventory;

	public Player(Console console)
	{
		super('@', Color.white, 20, 10, 10, 10, 1, 0);
		this.console = console;
		spellbook = new Spellbook();
		inventory = new Inventory(this);
	}

	@Override
	public void act()
	{
		char key;
		boolean moved;
		Item item;
		do
		{
			key = console.getKey();
			switch(key)
			{
			case 'Q':
				expire();
				moved = true;
				break;
			case 'P':
				choose(spellbook.getSpells(), "Known spells:");
				moved = false;
				break;
			case 'G':
				spellbook.makeSpell(console);
				moved = false;
				break;
			case 'D':
				Spell spell = choose(spellbook.getSpells(), "Delete which spell?");
				spellbook.removeSpell(spell);
				moved = false;
				break;
			case 'm':
				moved = magic();
				break;
			case 'i':
				choose(inventory.getInventory(), "Inventory:");
				moved = false;
				break;
			case 'g':
				moved = inventory.pickup();
				break;
			case 'd':
				item = choose(inventory.getInventory(), "Drop which item?");
				moved = inventory.drop(item);
				break;
			case 'e':
				choose(inventory.getEquipment(), "Equipment:");
				moved = false;
				break;
			case 'w':
				item = choose(inventory.getInventory(), "Equip what?");
				moved = inventory.equip(item);
				break;
			case 't':
				item = choose(inventory.getEquipment(), "Unequip what?");
				moved = inventory.unequip(item);
				break;
			case 'f':
				moved = ranged();
				break;
			case 'r':
				moved = read();
				break;
			case '>':
				moved = world().descend();
				break;
			case '<':
				moved = world().ascend();
				break;
			case '%':
				handleMacros();
				moved = false;
				break;
			default:
				Direction dir = Tools.keyToDir(key, true, false);
				if(dir != null)
				{
					move(dir);
					moved = true;
				}
				else
					moved = false;
				break;
			}
			displayIfFailed(moved);
		}
		while(!moved);
		if(Dice.dice.nextFloat() < REGEN)
			hp().modifyValueCapped(1);
		if(Dice.dice.nextFloat() < REGEN)
			mp().modifyValueCapped(1);
		calcFoV();
	}

	private void displayIfFailed(boolean succeeded)
	{
		if(!succeeded)
		{
			console.clearLine(world().height);
			console.buffMessages(0, world().height, this);
			console.refreshScreen();
		}
	}

	private boolean ranged()
	{
		Item bow = inventory.getEquiped(Slot.Bow);
		if(bow == null)
		{
			appendMessage("No bow equiped");
			return false;
		}
		else
		{
			Coord aim = getTarget();
			List<Coord> path = PathFactory.bresenham().getPath(world(), pos(), aim);
			for(Coord trajectory : path)
			{
				Monster target = world().getActorAt(trajectory, Monster.class);
				if(target != null)
					move(target.x() - x(), target.y() - y());
			}
			return true;
		}
	}

	private boolean magic()
	{
		Spell spell = choose(spellbook.getSpells(), "Choose a spell:");
		if(spell != null)
			return spell.cast(this);
		else
		{
			appendMessage("Invalid selection");
			return false;
		}
	}

	private boolean read()
	{
		List<Item> scrolls = inventory.getInventory(Slot.Scroll);
		Item scroll = choose(scrolls, "Choose a scroll:");
		if(scroll == null)
		{
			appendMessage("Invalid selection");
			return false;
		}
		else
		{
			appendMessage(this + " reads " + scroll);
			return true;
		}
	}

	private void handleMacros()
	{
		console.saveBuffer();
		console.clearBuffer();
		char key;
		do
		{
			console.buffLine(0, "Macro Editor", Color.white);
			console.buffLine(1, "a. add macro", Color.white);
			console.buffLine(2, "b. remove macro", Color.white);
			console.refreshScreen();
			key = console.getKey();
			if(key == 'a')
				addMacro();
			if(key == 'b')
				removeMacro();
		}
		while(key != 27);
		console.recallBuffer();
		console.refreshScreen();
	}

	private void addMacro()
	{
		String prompt = "Which key? ";
		console.buffLine(3, prompt, Color.white);
		console.refreshScreen();
		char key = console.echoChar(prompt.length(), 3, Color.white);
		prompt = "Enter macro: ";
		console.buffLine(4, prompt, Color.white);
		console.refreshScreen();
		String macro = console.echoString(prompt.length(), 4, Color.white, '\n');
		console.addMacro(key, macro);
		console.buffLine(5, "Macro added", Color.white);
		console.refreshScreen();
		console.getKey();
		for(int i = 3; i <= 5; i++)
			console.clearLine(i);
	}

	private void removeMacro()
	{
		String prompt = "Which key? ";
		console.buffLine(3, prompt, Color.white);
		console.refreshScreen();
		char key = console.echoChar(prompt.length(), 3, Color.white);
		console.removeMacro(key);
		console.buffLine(4, "Macro removed", Color.white);
		console.getKey();
		console.clearLine(3);
		console.clearLine(4);
	}

	@Override
	public Coord getTarget()
	{
		console.saveBuffer();
		final Coord target = new Coord(x(), y());
		char key = '\0';
		while(key != 't')
		{
			console.recallBuffer();
			console.buffRelCamera(this, target, '*', Color.white);
			console.refreshScreen();
			key = console.getKey();
			Direction dir = Tools.keyToDir(key, true, false);
			if(dir != null)
			{
				target.translate(dir);
				if(!fov.contains(target))
					target.translate(dir.opposite());
			}
		}
		console.recallBuffer();
		console.refreshScreen();
		return target;
	}

	public void calcFoV()
	{
		fov = FoVFactory.raySquare().calcFoV(this, VISION);
	}

	public Collection<Coord> getFoV()
	{
		return fov;
	}

	private <T> T choose(List<T> elements, String title)
	{
		console.saveBuffer();
		console.buffLine(0, title, Color.white);
		int i = 0;
		for(Object element : elements)
		{
			console.buffLine(i + 1, Tools.intToAlpha(i) + " " + element, Color.white);
			i++;
		}
		console.refreshScreen();
		int index = Tools.alphaToInt(console.getKey());
		console.recallBuffer();
		console.refreshScreen();
		if(index < 0 || index >= elements.size())
			return null;
		return elements.get(index);
	}

	public String getStatus()
	{
		return "hp:" + hp() + "\n" + "mp:" + mp() + "\n" + "atk:" + atk() + "\n"
				+ "def:" + def() + "\n" + "dmg:" + dmg() + "\n";
	}
}
