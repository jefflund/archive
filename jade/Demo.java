package jade;

import jade.core.Actor;
import jade.core.Console;
import jade.core.World;
import jade.core.Console.Camera;
import jade.fov.FoV.FoVFactory;
import jade.gen.Gen.GenFactory;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Dice;
import jade.util.Direction;
import jade.util.Rect;
import jade.util.Tools;
import java.awt.Color;
import java.util.Collection;

public class Demo
{
	private static Console console;

	public static void main(String[] args) throws InterruptedException
	{
		console = Console.getFramedConsole("Jade Demo");
		World world = new DemoWorld();
		Hero hero = new Hero();
		setupWorld(world, hero);
		mainLoop(world, hero);
		console.exit();
	}

	private static void mainLoop(World world, Hero hero)
	{
		// Most game loops will look similar to this
		while(!hero.isExpired())
		{
			display(world, hero);
			world.tick();
		}
	}

	private static void display(World world, Hero hero)
	{
		// Should probably do this every tick
		console.clearBuffer();
		// Show everything darkened
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				console.buffChar(x, y, world.look(x, y).darker());
		// Show what the hero sees
		console
				.clearBuffer(new Rect(0, 0, Hero.VISION * 2 + 1, Hero.VISION * 2 + 1));
		console.buffCamera(hero);// hero was registred in setupWorld
		// Shows all the messages, even those of the Actors (see tick)
		console.buffMessages(world.height, world);
		// Nothing actually happens until this call
		console.refreshScreen();
	}

	private static void setupWorld(World world, Hero hero)
	{
		// Be sure to generate the world map *before* adding actors
		GenFactory.traditional().generate(world, Dice.global.nextLong());
		world.addActor(hero, Dice.global);
		for(int i = 0; i < 5; i++)
			world.addActor(new Monster(), Dice.global);
		// Before using a camera in display, it must be registered
		console.addCamera(hero, Hero.VISION, Hero.VISION);
	}

	private static class DemoWorld extends World
	{
		public DemoWorld()
		{
			super(Console.DEFAULT_WIDTH, Console.DEFAULT_HEIGHT - 1);
		}

		public void tick()
		{
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException exception)
			{
				exception.printStackTrace();
			}
			// Probably you should let the hero act, then the monsters
			// That or make sure display gets called right before the hero acts.
			for(Hero hero : getActors(Hero.class))
				hero.act();
			for(Monster monster : getActors(Monster.class))
				monster.act();
			// This allows us to gather all the messages to one place
			for(Actor actor : getActors(Actor.class))
				retrieveMessages(actor);
			// This call is very important or expired Actors will persist
			removeExpired();
		}

		public ColoredChar look(int x, int y)
		{
			// check for the Hero first, then the monsters
			Actor actor = getActorAt(x, y, Hero.class);
			// obviously in this case it is redundant, but multiple actors can
			// be at the same location, and you must establish a display priority
			actor = (actor == null) ? getActorAt(x, y, Actor.class) : actor;
			// Note that getActorAt returns null if no one is there
			if(actor == null)
				return super.look(x, y);
			else
				return actor.look();
		}
	}

	private static class Hero extends Actor implements Camera
	{
		public static final int VISION = 5;

		public Hero()
		{
			super('@', Color.white);
		}

		public void act()
		{
			char key = console.tryGetKey();
			console.clearKeyBuffer();
			Direction dir = Tools.keyToDir(key, true, false);
			// caution: keyToDir() returns null if it gets a non directional key
			if(dir != null && dir != Direction.O)
				move(dir);
			if(key == 'q')// kill the player
				expire();
		}

		public void move(int dx, int dy)
		{
			Coord target = pos().getTranslated(dx, dy);
			Actor bumped = world().getActorAt(target, Actor.class);
			// getActorAt() returns null if nothing is there
			if(bumped != null)
			{
				appendMessage(this + " bumps the " + bumped);
				bumped.expire();// kill the monster.
			}
			else if(world().passable(target))
				super.move(dx, dy);
		}

		public String toString()
		{
			return "Hero";
		}

		public Collection<Coord> getFoV()
		{
			return FoVFactory.rayCircle().calcFoV(this, VISION);
		}
	}

	private static class Monster extends Actor
	{
		public Monster()
		{
			super('o', Color.yellow);
		}

		public void act()
		{
			Direction dir = Dice.global.nextDir();
			Actor bumped = world().getActorAt(pos().getTranslated(dir), Actor.class);
			if(bumped != null)
				appendMessage(this + " misses " + bumped);
			else if(world().passable(pos().getTranslated(dir)))
				move(dir);
		}

		public String toString()
		{
			return "orc";
		}
	}
}
