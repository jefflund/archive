package rl.prototype;

import jade.util.Config;
import jade.util.Dice;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Prototype
{
	public char face;
	public Color color;
	public int hp;
	public int mp;
	public int atk;
	public int def;
	public int dmg;
	public int fireRes;

	private Prototype(char face, Color color, int hp, int mp, int atk, int def,
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

	private static List<Prototype> prototypes;

	public static void loadAll(Config data)
	{
		prototypes = new ArrayList<Prototype>();
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
			Prototype prototype = new Prototype(face, new Color(color), hp, mp, atk,
					def, dmg, fireRes);
			prototypes.add(prototype);
		}
	}

	public static Prototype getAny()
	{
		return prototypes.get(Dice.global.nextInt(prototypes.size()));
	}
}
