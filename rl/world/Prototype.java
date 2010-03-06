package rl.world;

import jade.util.Config;
import jade.util.Dice;
import jade.util.Multimap;
import java.awt.Color;
import rl.creature.Monster;
import rl.creature.Creature.ProtoCreature;
import rl.item.Item;
import rl.item.Enchantment.ProtoEnchant;
import rl.item.Item.ProtoItem;
import rl.item.Item.Slot;
import rl.magic.Weave.Effect;

public class Prototype
{
	private Multimap<Integer, ProtoCreature> monsters;
	private Multimap<Integer, ProtoCreature> uniques;
	private Multimap<Integer, ProtoItem> items;
	private Multimap<Integer, ProtoItem> artifacts;
	
	public void loadMonsters(Config data)
	{
		monsters = loadCreatureConfig(data);
	}
	
	public void loadUnique(Config data)
	{
		uniques = loadCreatureConfig(data);
	}
	
	private Multimap<Integer, ProtoCreature> loadCreatureConfig(Config data)
	{
		Multimap<Integer, ProtoCreature> creatures = new Multimap<Integer, ProtoCreature>();
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
			creatures.put(depth, prototype);
		}
		return creatures;
	}
	
	public void loadItems(Config data)
	{
		items = loadItemConfig(data);
	}
	
	public void loadArtifacts(Config data)
	{
		artifacts = loadItemConfig(data);
	}
	
	private Multimap<Integer, ProtoItem> loadItemConfig(Config data)
	{
		Multimap<Integer, ProtoItem> items = new Multimap<Integer, ProtoItem>();
		for(String item : data.getSections())
		{
			Slot slot = Slot.valueOf(data.getProperty(item, "slot").toUpperCase());
			char face = data.getProperty(item, "face").charAt(0);
			int rgb = Integer.parseInt(data.getProperty(item, "color"), 16);
			Color color = new Color(rgb);
			ProtoEnchant enchantment = null;
			if(data.getProperty(item, "enchantment") != null)
			{
				String[] parts = data.getProperty(item, "enchantment").split(",");
				Effect effect = Effect.valueOf(parts[0].trim().toUpperCase());
				int magnitude = Integer.parseInt(parts[1].trim());
				enchantment = new ProtoEnchant(effect, magnitude);
			}
			int depth = Integer.parseInt(data.getProperty(item, "depth"));
			ProtoItem prototype = new ProtoItem(slot, face, color, enchantment);
			items.put(depth, prototype);
		}
		return items;
	}
	
	public Monster getMonster(int depth)
	{
		ProtoCreature prototype = getTemplatePrototype(monsters, depth);
		return prototype == null ? null : new Monster(prototype);
	}
	
	public Item getItem(int depth)
	{
		ProtoItem prototype = getTemplatePrototype(items, depth);
		return prototype == null ? null : new Item(prototype);
	}
	
	private <T> T getTemplatePrototype(Multimap<Integer, T> prototypes, Integer depth)
	{
		Multimap<Integer, T> headmap = prototypes.headMap(depth, true);
		depth = Dice.global.nextValue(headmap.keys());
		if(depth == null)
			return null;
		return Dice.global.nextValue(headmap.get(depth));
	}
	
	public Monster getUnique(int depth)
	{
		ProtoCreature prototype = getUniquePrototype(uniques, depth);
		return prototype == null ? null : new Monster(prototype);
	}
	
	public Item getArtifact(int depth)
	{
		ProtoItem prototype = getUniquePrototype(artifacts, depth);
		return prototype == null ? null : new Item(prototype);
	}
	
	private <T> T getUniquePrototype(Multimap<Integer, T> prototypes, Integer depth)
	{
		Multimap<Integer, T> headmap = prototypes.headMap(depth, true);
		depth = Dice.global.nextValue(headmap.keys());
		if(depth == null)
			return null;
		T prototype = Dice.global.nextValue(headmap.get(depth));
		prototypes.get(depth).remove(prototype);
		return prototype;
	}
}
