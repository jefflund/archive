package rl.creature;

import jade.core.Console;
import jade.fov.Camera;
import jade.fov.FoV.FoVFactory;
import jade.util.Coord;
import jade.util.Dice;
import jade.util.Tools;
import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import rl.item.Inventory;
import rl.item.Item;
import rl.magic.Spell;
import rl.magic.Instant.Effect;
import rl.magic.Spell.Target;
import rl.world.Dungeon;

public class Player extends Creature implements Serializable, Camera
{
	private Console console;
	private List<Spell> spellbook;
	private Collection<Coord> fov;
	private Inventory inventory;
	private Dungeon dungeon;
	private static final float REGEN = .05f;
	private Dice dice;

	public Player(Console console, Dungeon dungeon)
	{
		super('@', Color.white, 20, 10, 1, 0, 10);
		this.console = console;
		this.dungeon = dungeon;
		inventory = new Inventory(this);
		spellbook = new LinkedList<Spell>();
		spellbook.add(new Spell(this, Effect.FIRE, Target.AREA, 10, 5, 1,
				"fire trap"));
		spellbook.add(new Spell(this, Effect.ELEC, Target.OTHER, 10, 5, 1,
				"elec trap"));
		spellbook.add(new Spell(this, Effect.STONEFALL, Target.AREA, 10, 0, 1,
				"collapse"));
		spellbook.add(new Spell(this, Effect.CHANNEL, Target.SELF, 10, 10, 1,
				"concentrate"));
		spellbook.add(new Spell(this, Effect.RFIRE, Target.SELF, 20, 5, 1,
				"resist fire"));
		spellbook.add(new Spell(this, Effect.RELEC, Target.SELF, 20, 5, 1,
		"resist elec"));		
		dice = new Dice();
	}

	@Override
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
				inventory.get();
				break;
			case 'd':
				inventory.drop(inventory());
				break;
			case 'e':
				equipment();
				moved = false;
				break;
			case 'w':
				inventory.equip(inventory());
				break;
			case 't':
				inventory.unequip(equipment());
				break;
			case '>':
				dungeon.descend();
				break;
			case '<':
				dungeon.ascend();
				break;
			default:
				Coord dir = Tools.keyToDir(key, true, false);
				if(dir != null)
					move(dir.x(), dir.y());
				else
					moved = false;
				break;
			}
		}
		if(dice.nextFloat() < REGEN)
			hpHeal(1);
		if(dice.nextFloat() < REGEN)
			mpRestore(1);
		calcFoV();
	}

	public Coord getTarget()
	{
		console.saveBuffer();
		Coord target = new Coord(x(), y());
		char key = '\0';
		while(key != 't')
		{
			console.recallBuffer();
			console.buffChar(target, '*', Color.white);
			console.refreshScreen();
			key = console.getKey();
			Coord dir = Tools.keyToDir(key, true, false);
			if(dir != null)
			{
				target.translate(dir);
				if(!fov.contains(target))
					target.translate(-dir.x(), -dir.y());
			}
		}
		console.recallBuffer();
		console.refreshScreen();
		return target;
	}

	public void calcFoV()
	{
		fov = FoVFactory.get(FoVFactory.CircularShadow).calcFoV(world(), x(), y(),
				4);
	}

	public Collection<Coord> getFoV()
	{
		return fov;
	}
	
	public String status()
	{
		return "hp:" + hp() + "\tmp:"	+ mp();
	}

	private <T> T choose(List<T> elements, String title)
	{
		console.saveBuffer();
		console.buffString(0, 0, title, Color.white);
		int i = 0;
		for(Object element : elements)
		{
			console.buffString(0, i + 1, Tools.intToAlpha(i) + " " + element,
					Color.white);
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

	private Item inventory()
	{
		return choose(inventory.getItems(), "Inventory");
	}

	private Item equipment()
	{
		return choose(inventory.getEquiped(), "Equipment");
	}

	private Spell spellbook()
	{
		return choose(spellbook, "Spellbook");
	}

	private void cast()
	{
		Spell spell = spellbook();
		if(spell == null)
			appendMessage("Invalid selection");
		else
			spell.cast();
	}
}
