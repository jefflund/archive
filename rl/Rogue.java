package rl;

import jade.core.Console;
import jade.core.GConsole;
import jade.util.Dice;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import rl.creature.Player;
import rl.world.Dungeon;
import rl.world.Level;

public class Rogue
{
	private static Console console;
	private static Dungeon dungeon;
	private static Player player;
	private static List<String> params;

	public static void main(String[] args)
	{
		getParams(args);
		init();
		gameLoop();
		end();
	}

	private static void getParams(String[] args)
	{
		params = new LinkedList<String>();
		for(final String arg : args)
			params.add(arg);
	}

	private static void gameLoop()
	{
		do
		{
			final Level level = dungeon.getLevel();
			console.clearBuffer();
			console.buffCamera(player, Player.VISION, Player.VISION);
			console
					.buffString(2 * Player.VISION + 2, 0, player.status(), Color.white);
			console.buffString(0, 2 * Player.VISION + 1, level.getMessages(),
					Color.white);
			console.refreshScreen();
			level.tick();
		}
		while(player.playing());
	}

	private static void init()
	{
		console = params.contains("g") ? getGConsole() : Console
				.getFramedConsole("Jade");
		console.buffString(0, 0, "Enter your name:", Color.white);
		console.refreshScreen();
		final String name = console.echoString(0, 1, Color.white, '\n');
		load(name);
	}

	private static Console getGConsole()
	{
		final GConsole gConsole = GConsole.getFramedConsole("Jade Graphics");
		gConsole.registerImage("tiles", 0, 3, '@', Color.white);
		gConsole.registerImage("tiles", 21, 12, 'D', Color.red);
		gConsole.registerImage("tiles", 0, 22, '#', Color.white);
		gConsole.registerImage("tiles", 16, 22, '>', Color.white);
		gConsole.registerImage("tiles", 15, 22, '<', Color.white);
		gConsole.registerImage("tiles", 0, 23, '.', Color.white);
		gConsole.registerImage("tiles", 5, 23, '.', Color.green);
		gConsole.registerImage("tiles", 57, 22, '.', Color.gray);
		gConsole.registerImage("tiles", 55, 23, '%', Color.green);
		gConsole.registerImage("tiles", 55, 23, '%', Color.yellow);
		gConsole.registerImage("tiles", 3, 10, '|', Color.white);
		gConsole.registerImage("tiles", 48, 1, '*', Color.red);
		gConsole.registerImage("tiles", 45, 0, ']', Color.white);
		gConsole.registerImage("tiles", 25, 1, '^', Color.blue);
		return gConsole;
	}

	private static void end()
	{
		if(player.isExpired())
		{
			console.buffString(0, dungeon.getLevel().height + 1, "You're dead!",
					Color.white);
			console.refreshScreen();
			console.getKey();
			new File(player.toString()).deleteOnExit();
		}
		else if(params.contains("s"))
			save();
		System.exit(0);
	}

	private static void save()
	{
		try
		{
			final ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(player.toString()));
			out.writeObject(dungeon);
			out.close();
		}
		catch(final Exception e)
		{
		}
	}

	private static void load(String name)
	{
		try
		{
			final ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					name));
			dungeon = (Dungeon) in.readObject();
			in.close();
			player = dungeon.getLevel().player();
			player.onDeserialize(console);
		}
		catch(final Exception e)
		{
			dungeon = new Dungeon();
			player = new Player(console, dungeon, name);
			dungeon.getLevel().addActor(player, new Dice(0));
		}
	}
}
