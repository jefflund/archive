package rl.creature;

import jade.core.Actor;
import jade.core.Console;
import jade.path.Path.PathFactory;
import jade.util.Coord;
import jade.util.Dice;
import jade.util.Tools;
import java.awt.Color;
import java.util.List;
import rl.world.Script;

public class Monster extends Creature
{
	public enum Prototype
	{
		Orc('o', Color.yellow, 5, 5, 5, 5, 1, 0),
		Ogre('O', Color.pink, 5, 5, 5, 5, 3, 10),
		Dragon('D', Color.red, 10, 10, 10, 10, 3, 90),
		Boss('U', Color.orange, 50, 50, 50, 50, 10, 120);

		public char face;
		public Color color;
		public int hp;
		public int mp;
		public int atk;
		public int def;
		public int dmg;
		public int fireRes;

		private Prototype(char face, Color color, int hp, int mp, int atk, int def,
				int dmg, int resFire)
		{
			this.face = face;
			this.color = color;
			this.hp = hp;
			this.mp = mp;
			this.atk = atk;
			this.def = def;
			this.dmg = dmg;
		}
	};

	private Coord target;
	private boolean alert;

	public Monster(Prototype prototype)
	{
		super(prototype);
		target = new Coord();
		alert = false;
		if(prototype == Prototype.Boss)
			attachBossScript();
	}

	@Override
	public void act()
	{
		if(world().player().getFoV(this).contains(pos()))
		{
			if(!alert)
				appendMessage(this + " wakes up");
			alert = true;
			target.move(world().player().pos());
		}
		if(target.equals(pos()))
		{
			move(Dice.global.nextDir());
			target.move(pos());
		}
		else
		{
			List<Coord> path = PathFactory.aStar().getPath(world(), pos(),
					getTarget(Player.class));
			if(path == null);
//				move(Dice.global.nextDir());
			else
				move(Tools.directionTo(pos(), path.get(0)));
		}
	}

	public Coord getTarget(Class<? extends Actor> targetType)
	{
		if(targetType == Player.class)
			return target;
		else
			return null;
	}

	public Coord getTarget()
	{
		return target;
	}
	
	public boolean alert()
	{
		return alert;
	}

	private void attachBossScript()
	{
		Script script = new Script()
		{
			public void act()
			{
				if(player().getFoV((Monster)holder()).contains(pos()))
				{
					Console console = console();
					console.saveBuffer();
					console.clearBuffer();
					console.buffLine(0, "Boss: I shall crush your big toe!", Color.white);
					console.refreshScreen();
					console.getKey();
					console.buffLine(1, "@: You'll pay for your evil-ness!", Color.white);
					console.refreshScreen();
					console.getKey();
					console.buffLine(2, "Boss: Not if you pay first!", Color.white);
					console.refreshScreen();
					console.getKey();
					console.buffLine(3, "@: You suck at come backs!", Color.white);
					console.refreshScreen();
					console.getKey();
					console.recallBuffer();
					console.refreshScreen();
					expire();
				}
			}
		};
		script.attachTo(this);
	}
}
