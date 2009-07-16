package rl;

import jade.core.Console;
import jade.util.Dice;
import java.awt.Color;
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
		init();
		do
		{
			Level level = dungeon.getLevel();
			Player player = level.player();
			console.clearBuffer();
			console.buffFoV(player, 4, 4, level);
			console.buffString(0, level.height, player.status(), Color.white);
			console.buffString(0, level.height + 1, level.getMessages(), Color.white);
			console.refreshScreen();
			level.tick();
		}
		while(!player.isExpired());
		end();
	}

	private static void init()
	{
		console = Console.getFramedConsole("Jade");
		dungeon = new Dungeon();
		player = new Player(console, dungeon);
		dungeon.getLevel().addActor(player, new Dice(0));
	}
	
	private static void end()
	{
		console.buffString(0, dungeon.getLevel().height + 1, "You're dead!", Color.white);
		console.refreshScreen();
		console.getKey();
		System.exit(0);
	}
}
