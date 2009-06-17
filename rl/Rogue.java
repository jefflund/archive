package rl;

import jade.core.Console;
import jade.util.Coord;
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
			console.clearBuffer();
			for(int x = 0; x < level.width; x++)
				for(int y = 0; y < level.height; y++)
					console.buffChar(x, y, level.look(x, y).ch(), Color.gray);
			for(Coord coord : level.player().getFoV())
				console.buffChar(coord, level.look(coord));
			console.buffString(0, level.height, level.getMessages(), Color.white);
			console.refreshScreen();
			level.tick();
		}
		while(!player.isExpired());
		System.exit(0);
	}

	private static void init()
	{
		console = Console.getFramedConsole("Jade");
		dungeon = new Dungeon();
		player = new Player(console, dungeon);
		dungeon.getLevel().addActor(player, new Dice(0));
	}
}
