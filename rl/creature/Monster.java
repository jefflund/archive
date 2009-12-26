package rl.creature;

import jade.path.Path.PathFactory;
import jade.util.Coord;
import jade.util.Dice;
import jade.util.Tools;
import java.awt.Color;
import java.util.List;

public class Monster extends Creature
{
	public enum Prototype
	{
		Orc('o', Color.yellow, 5, 5, 5, 5, 1, 0),
		Ogre('O', Color.pink, 5, 5, 5, 5, 3, 10),
		Dragon('D', Color.red, 10, 10, 10, 10, 3, 90);

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

	public Monster(Prototype prototype)
	{
		super(prototype);
		target = new Coord();
	}

	@Override
	public void act()
	{
		if(world().player().getFoV().contains(pos()))
			target.move(world().player().pos());
		if(target.equals(pos()))
		{
			move(Dice.dice.nextDir());
			target.move(pos());
		}
		else
		{
			List<Coord> path = PathFactory.aStar().getPath(world(), pos(),
					getTarget());
			if(path == null)
				move(Dice.dice.nextDir());
			else
				move(Tools.directionTo(pos(), path.get(0)));
		}
	}

	@Override
	public Coord getTarget()
	{
		return target;
	}
}
