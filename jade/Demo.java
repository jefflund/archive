package jade;

import jade.core.Actor;
import jade.core.Console;
import jade.core.World;
import jade.gen.Gen.GenFactory;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Dice;
import jade.util.Tools;
import java.awt.Color;

public class Demo
{
	public static void main(String[] args)
	{
		Console console = Console.getFramedConsole("Demo");
		DemoPlayer player = new DemoPlayer(console);
		DemoWorld world = new DemoWorld(player);
		while(player.boundTo(world))
		{
			console.clearBuffer();
			for(int x = 0; x < world.width; x++)
				for(int y = 0; y < world.height; y++)
					console.buffChar(x, y, world.look(x, y));
			console.buffString(0, world.height, world.getMessages(), Color.white);
			console.refreshScreen();
			world.tick();
		}
		System.exit(0);
	}

	private static class DemoPlayer extends Actor
	{
		private Console console;

		public DemoPlayer(Console console)
		{
			super('@', Color.white);
			this.console = console;
		}

		@Override
		public void act()
		{
			char key = console.getKey();
			switch(key)
			{
			case 'q':
				expire();
				break;
			default:
				Coord dir = Tools.keyToDir(key, true, true);
				if(dir != null)
					move(dir.x(), dir.y());
			}
		}

		@Override
		public void move(int dx, int dy)
		{
			if(world().passable(x() + dx, y() + dy))
				super.move(dx, dy);
			else
				appendMessage(this + " bumps the wall");
		}
	}
	private static class DemoWorld extends World
	{
		private DemoPlayer player;

		public DemoWorld(DemoPlayer player)
		{
			super(80, 23);
			Dice random = new Dice();
			GenFactory.get(GenFactory.Cellular).generate(this, random.nextLong());
			addActor(player, random);
			this.player = player;
		}

		@Override
		public void tick()
		{
			player.act();
			retrieveMessages(player);
			removeExpired();
		}

		@Override
		public ColoredChar look(int x, int y)
		{
			DemoPlayer player = getActorAt(x, y, DemoPlayer.class);
			if(player != null)
				return player.look();
			return super.look(x, y);
		}
	}
}
