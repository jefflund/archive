package rl.prototype;

import jade.util.Config;
import jade.util.Dice;
import jade.util.Multimap;
import java.awt.Color;

public class MPrototype
{
	public char face;
	public Color color;
	public int hp;
	public int mp;
	public int atk;
	public int def;
	public int dmg;
	public int fireRes;

	private MPrototype(char face, Color color, int hp, int mp, int atk, int def,
			int dmg, int fireRes)
	{
		this.face = face;
		this.color = color;
		this.hp = hp;
		this.mp = mp;
		this.atk = atk;
		this.def = def;
		this.dmg = dmg;
		this.fireRes = fireRes;
	}

	private static Multimap<Integer, MPrototype> prototypes;

	public static void load(Config data)
	{
		prototypes = new Multimap<Integer, MPrototype>();
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
			MPrototype prototype = new MPrototype(face, new Color(color), hp, mp, atk,
					def, dmg, fireRes);
			prototypes.put(depth, prototype);
		}
	}

	public static MPrototype get(int depth)
	{
		assert(depth <= prototypes.lastKey());
		Multimap<Integer, MPrototype> possible = prototypes.headMap(depth, true);
		depth = Dice.global.nextValue(possible.keys());
		return Dice.global.nextValue(possible.get(depth));
	}
}
