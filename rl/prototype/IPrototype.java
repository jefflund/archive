package rl.prototype;

import jade.util.Config;
import jade.util.Dice;
import jade.util.Multimap;
import java.awt.Color;
import rl.item.Enchantment;
import rl.item.Item.Slot;
import rl.magic.Weave.Effect;

public class IPrototype
{
	public Slot slot;
	public char face;
	public Color color;
	public Enchantment enchantment;

	public IPrototype(Slot slot, char face, Color color, Enchantment enchantment)
	{
		this.slot = slot;
		this.face = face;
		this.color = color;
		this.enchantment = enchantment;
	}

	private static Multimap<Integer, IPrototype> prototypes;

	public static void load(Config data)
	{
		prototypes = new Multimap<Integer, IPrototype>();
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
			IPrototype prototype = new IPrototype(slot, face, color, enchantment);
			prototypes.put(depth, prototype);
		}
	}
	
	public static IPrototype get(int depth)
	{
		assert(depth <= prototypes.lastKey());
		Multimap<Integer, IPrototype> possible = prototypes.headMap(depth, true);
		depth = Dice.global.nextValue(possible.keys());
		return Dice.global.nextValue(possible.get(depth));
	}
}
