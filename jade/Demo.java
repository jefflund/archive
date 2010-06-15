package jade;

import jade.core.Actor;
import jade.core.Console;
import jade.core.World;
import jade.fov.FoV.FoVFactory;
import jade.gen.Gen.GenFactory;
import jade.util.Dice;
import jade.util.Tools;
import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import jade.util.type.Direction;
import java.awt.Color;
import java.util.Collection;

public class Demo
{
	private static Console console;
	private static boolean display = true;

	public static void main(String[] args) throws InterruptedException
	{
		console = Console.getFramedConsole("Demo");
		console.buffChar(0, 0, '@', Color.white);
		console.refreshScreen();
		World world = new DemoWorld();
		GenFactory.traditional(3, 3).generate(world, System.currentTimeMillis());
		Actor hero = new DemoActor();
		world.addActor(hero, Dice.global);
		for(int i = 0; i < 5; i++)
			world.addActor(new DemoTrap(), Dice.global);
		while(!hero.isExpired())
		{
			if(display)
				displayWorld(world);
			else
				displayWorld2(world, hero);
			world.tick();
		}
		console.clearBuffer();
		if(hero.isExpired())
			console.buffString(0, 0, "The trap got you!", Color.white);
		console.refreshScreen();
		console.getKey();
		console.exit();
	}

	private static void displayWorld(World world)
	{
		console.clearBuffer();
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				console.buffChar(x, y, world.look(x, y));
		console.refreshScreen();
	}

	private static void displayWorld2(World world, Actor actor)
	{
		Collection<Coord> fov = FoVFactory.shadowCircle().calcFoV(actor, 10);
		console.clearBuffer();
		for(Coord coord : fov)
			console.buffChar(coord, world.look(coord));
		console.refreshScreen();
	}

	private static class DemoWorld extends World
	{
		@SuppressWarnings("unchecked")
		public DemoWorld()
		{
			super(Console.DEFAULT_WIDTH, Console.DEFAULT_HEIGHT);
			setDrawOrder(DemoActor.class, DemoTrap.class);
		}

		@Override
		public void tick()
		{
			for(DemoActor actor : getActors(DemoActor.class))
				actor.act();
			for(DemoTrap trap : getActors(DemoTrap.class))
				trap.act();
			removeExpired();
		}
	}

	private static class DemoActor extends Actor
	{
		public DemoActor()
		{
			super('@', Color.white);
		}

		@Override
		public void act()
		{
			char key = console.getKey();
			Direction dir = Tools.keyToDir(key, true, false);
			if(dir != null)
				move(dir);
			else if(key == 'q')
				console.exit();
			else if(key == '\n')
				display = !display;
		}
		
		@Override
		public void move(int dx, int dy)
		{
			if(world().passable(x() + dx, y() + dy))
				super.move(dx, dy);
		}
	}

	private static class DemoTrap extends Actor
	{
		private int timer = 10;

		public DemoTrap()
		{
			super('*', Color.red);
		}

		@Override
		public void act()
		{
			if(!held())
			{
				DemoActor hero = world().getActorAt(pos(), DemoActor.class);
				if(hero != null)
				{
					attachTo(hero);
					hero.setFace(new ColoredChar('@', Color.red));
				}
			}
			else
			{
				if(timer-- == 0)
					holder().expire();
			}
		}
	}
}
