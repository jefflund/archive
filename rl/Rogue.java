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
			for(Coord coord : level.player().getFoV())
				console.buffChar(coord, level.look(coord));
			Player player = level.player();
			String status = "hp:" + player.hp() + "\tmp:"	+ player.mp();
			console.buffString(0, level.height, status, Color.white);
			console.buffString(0, level.height + 1, level.getMessages(), Color.white);
			console.refreshScreen();
			level.tick();
		}
		while(!player.isExpired());
		console.buffString(0, dungeon.getLevel().height + 1, "You're dead!", Color.white);
		console.refreshScreen();
		console.getKey();
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
