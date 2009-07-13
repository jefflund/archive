package jade;

import jade.core.Actor;
import jade.core.Console;
import jade.core.World;
import jade.fov.FoV;
import jade.fov.FoV.FoVFactory;
import jade.gen.Gen.GenFactory;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Dice;
import java.awt.Color;

public class Demo
{
	private static Dice dice;

	public static void main(String[] args)
	{
		dice = new Dice();
		DemoWorld world = new DemoWorld();
		resetDemo(world);
		Console console = Console.getFramedConsole("Jade Demo");
		char key = '\0';
		do
		{
			world.tick();
			console.clearBuffer();
			for(int x = 0; x < world.width; x++)
				for(int y = 0; y < world.height; y++)
					console.buffChar(x, y, world.look(x, y));
			console.refreshScreen();
			key = console.getKey();
			if(key == 'r')
				resetDemo(world);
			else if(key == 'b')
			{
				for(GooBomb bomb : world.getActors(GooBomb.class))
					bomb.act();
			}
		}
		while(key != 'q');
		System.exit(0);
	}

	private static void resetDemo(World world)
	{
		for(Actor actor : world.getActors(Actor.class))
			actor.expire();
		world.removeExpired();
		GenFactory.get(GenFactory.Cellular).generate(world, dice.nextLong());
		for(int i = 0; i < 100; i++)
			world.addActor(new Denizen(), dice);
		Denizen infected = new Denizen();
		Plague plague = new Plague();
		plague.attachTo(infected);
		world.addActor(infected, dice);
		world.addActor(new GooBomb(), dice);
	}

	private static class DemoWorld extends World
	{
		public DemoWorld()
		{
			super(80, 24);
		}

		public void tick()
		{
			for(Plague plague : getActors(Plague.class))
				plague.act();
			for(Denizen denizen : getActors(Denizen.class))
				denizen.act();
			removeExpired();
		}

		public ColoredChar look(int x, int y)
		{
			Denizen denizen = getActorAt(x, y, Denizen.class);
			GooBomb bomb = getActorAt(x, y, GooBomb.class);
			if(denizen != null)
				return denizen.look();
			else if(bomb != null)
				return bomb.look();
			else
				return super.look(x, y);
		}
	}

	private static class Denizen extends Actor
	{
		public Denizen()
		{
			super('@', Color.white);
		}

		public void act()
		{
			int dx = dice.nextInt(-1, 1);
			int dy = dice.nextInt(-1, 1);
			if(world().passable(x() + dx, y() + dy))
				move(dx, dy);
		}

		public ColoredChar look()
		{
			if(holds().size() > 0)
				return new ColoredChar(super.look().ch(), Color.red);
			else
				return super.look();
		}

		public void expire()
		{
			super.expire();
			world().tile(x(), y()).setTile('@', Color.darkGray, true);
		}
	}

	private static class Plague extends Actor
	{
		private int ticksUntilDeath;

		public Plague()
		{
			super('\0', Color.black);
			ticksUntilDeath = dice.diceXdY(10, 10);
		}

		public void act()
		{
			if(held())
			{
				if(--ticksUntilDeath <= 0)
					holder().expire();
				for(int x = x() - 1; x <= x() + 1; x++)
					for(int y = y() - 1; y <= y() + 1; y++)
						world().addActor(new Plague(), x, y);
			}
			else
			{
				Denizen denizen = world().getActorAt(x(), y(), Denizen.class);
				if(denizen != null && denizen.holds().size() == 0)
					attachTo(denizen);
				else
					expire();
			}
		}
	}

	private static class GooBomb extends Actor
	{
		private int range;
		public GooBomb()
		{
			super('*', Color.magenta);
			range = dice.diceXdY(10, 2);
		}

		public void act()
		{
			FoV shadowcast = FoVFactory.get(FoVFactory.CircularShadow);
			for(Coord coord : shadowcast.calcFoV(world(), x(), y(), range))
			{
				if(world().getActorAt(coord, Denizen.class) != null)
					world().getActorAt(coord, Denizen.class).expire();
				world().tile(coord).setTile(world().look(coord).ch(), Color.magenta,
						world().passable(coord));
			}
			expire();
		}
	}
}