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
import rl.creature.Player;
import rl.world.Dungeon;
import rl.world.Level;

public class Rogue
{
	private static Console console;
	private static Dungeon dungeon;
	private static Player player;

	public static void main(String[] args)
	{
		init(args.length > 0 && args[0] == "g");
		gameLoop();
		end();
	}

	private static void gameLoop()
	{
		do
		{
			Level level = dungeon.getLevel();
			console.clearBuffer();
			console.buffCamera(player, 4, 4);
			console.buffString(0, level.height, player.status(), Color.white);
			console.buffString(0, level.height + 1, level.getMessages(), Color.white);
			console.refreshScreen();
			level.tick();
		}
		while(player.playing());
	}

	private static void init(boolean graphics)
	{
		if(graphics)
			getGConsole();
		else
			console = Console.getFramedConsole("Jade");
		console.buffString(0, 0, "Enter your name:", Color.white);
		console.refreshScreen();
		String name = console.echoString(0, 1, Color.white, '\n');
		load(name);
	}

	private static void getGConsole()
	{
		console = GConsole.getFramedConsole("Jade");
		((GConsole)console).registerImage("tiles", 0, 3, '@', Color.white);
		((GConsole)console).registerImage("tiles", 21, 12, 'D', Color.red);
		((GConsole)console).registerImage("tiles", 0, 22, '#', Color.white);
		((GConsole)console).registerImage("tiles", 16, 22, '>', Color.white);
		((GConsole)console).registerImage("tiles", 15, 22, '<', Color.white);
		((GConsole)console).registerImage("tiles", 0, 23, '.', Color.white);
		((GConsole)console).registerImage("tiles", 5, 23, '.', Color.green);
		((GConsole)console).registerImage("tiles", 57, 22, '.', Color.gray);
		((GConsole)console).registerImage("tiles", 55, 23, '%', Color.green);
		((GConsole)console).registerImage("tiles", 55, 23, '%', Color.yellow);
		((GConsole)console).registerImage("tiles", 3, 10, '|', Color.white);
		((GConsole)console).registerImage("tiles", 48, 1, '*', Color.red);
		((GConsole)console).registerImage("tiles", 45, 0, ']', Color.white);
		((GConsole)console).registerImage("tiles", 25, 1, '^', Color.blue);
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
		else
			save();
		System.exit(0);
	}

	private static void save()
	{
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
					player.toString()));
			out.writeObject(dungeon);
			out.close();
		}
		catch(Exception e)
		{
		}
	}

	private static void load(String name)
	{
		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(name));
			dungeon = (Dungeon)in.readObject();
			in.close();
			player = dungeon.getLevel().player();
			player.onDeserialize(console);
		}
		catch(Exception e)
		{
			dungeon = new Dungeon();
			player = new Player(console, dungeon, name);
			dungeon.getLevel().addActor(player, new Dice(0));
		}
	}

}
