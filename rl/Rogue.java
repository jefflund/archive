package rl;

import jade.core.Console;
import jade.core.TileConsole;
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
		console = TileConsole.getFramedConsole("Jade");
		((TileConsole)console).registerImage("tiles", 0, 3, '@', Color.white);
		((TileConsole)console).registerImage("tiles", 21, 12, 'D', Color.red);
		((TileConsole)console).registerImage("tiles", 0, 22, '#', Color.white);
		((TileConsole)console).registerImage("tiles", 16, 22, '>', Color.white);
		((TileConsole)console).registerImage("tiles", 15, 22, '<', Color.white);
		((TileConsole)console).registerImage("tiles", 0, 23, '.', Color.white);
		((TileConsole)console).registerImage("tiles", 5, 23, '.', Color.green);
		((TileConsole)console).registerImage("tiles", 57, 22, '.', Color.gray);
		((TileConsole)console).registerImage("tiles", 55, 23, '%', Color.green);
		((TileConsole)console).registerImage("tiles", 55, 23, '%', Color.yellow);
		((TileConsole)console).registerImage("tiles", 3, 10, '|', Color.white);
		((TileConsole)console).registerImage("tiles", 48, 1, '*', Color.red);
		((TileConsole)console).registerImage("tiles", 45, 0, ']', Color.white);
		((TileConsole)console).registerImage("tiles", 25, 1, '^', Color.blue);
		dungeon = new Dungeon();
		player = new Player(console, dungeon);
		dungeon.getLevel().addActor(player, new Dice(0));
	}
}
