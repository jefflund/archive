package rl.world;

import jade.util.Config;
import jade.util.Dice;
import jade.util.Multimap;
import java.awt.Color;
import rl.creature.Monster;
import rl.creature.Creature.ProtoCreature;
import rl.item.Enchantment;
import rl.item.Item;
import rl.item.Item.ProtoItem;
import rl.item.Item.Slot;
import rl.magic.Weave.Effect;

public class Prototype
{
	private static Multimap<Integer, ProtoCreature> monsterPrototypes;
	private static Multimap<Integer, ProtoItem> itemPrototypes;

	public static void loadMonsters(Config data)
	{
		monsterPrototypes = new Multimap<Integer, ProtoCreature>();
		for(String monster : data.getSections())
		{
			char face = data.getProperty(monster, "face").charAt(0);
			int color = Integer.parseInt(data.getProperty(monster, "color"), 16);
			int hp = Integer.parseInt(data.getProperty(monster, "hp"));
			int mp = Integer.parseInt(data.getProperty(monster, "mp"));
			int atk = Integer.parseInt(data.getProperty(monster, "atk"));
			int def = Integer.parseInt(data.getProperty(monster, "def"));
			int dmg = Integer.parseInt(data.getProperty(monster, "dmg"));
			int fireRes = Integer.parseInt(data.getProperty(monster, "fireRes"));
			int depth = Integer.parseInt(data.getProperty(monster, "depth"));
			ProtoCreature prototype = new ProtoCreature(face, new Color(color), hp, mp, atk,
					def, dmg, fireRes);
			monsterPrototypes.put(depth, prototype);
		}
	}

	public static Monster getMonster(int depth)
	{
		assert(depth <= monsterPrototypes.lastKey());
		Multimap<Integer, ProtoCreature> possible = monsterPrototypes.headMap(depth, true);
		depth = Dice.global.nextValue(possible.keys());
		ProtoCreature prototype = Dice.global.nextValue(possible.get(depth));
		return new Monster(prototype);
	}
	
	public static void loadItems(Config data)
	{
		itemPrototypes = new Multimap<Integer, ProtoItem>();
		for(String item : data.getSections())
		{
			Slot slot = Slot.valueOf(data.getProperty(item, "slot").toUpperCase());
			char face = data.getProperty(item, "face").charAt(0);
			int rgb = Integer.parseInt(data.getProperty(item, "color"), 16);
			Color color = new Color(rgb);
			Enchantment enchantment = null;
			if(data.getProperty(item, "enchantment") != null)
			{
				String[] parts = data.getProperty(item, "enchantment").split(",");
				Effect effect = Effect.valueOf(parts[0].trim().toUpperCase());
				int magnitude = Integer.parseInt(parts[1].trim());
				enchantment = new Enchantment(effect, magnitude);
			}
			int depth = Integer.parseInt(data.getProperty(item, "depth"));
			ProtoItem prototype = new ProtoItem(slot, face, color, enchantment);
			itemPrototypes.put(depth, prototype);
		}
	}
	
	public static Item getItem(int depth)
	{
		assert(depth <= itemPrototypes.lastKey());
		Multimap<Integer, ProtoItem> possible = itemPrototypes.headMap(depth, true);
		depth = Dice.global.nextValue(possible.keys());
		ProtoItem prototype = Dice.global.nextValue(possible.get(depth));
		return new Item(prototype);
	}
}
