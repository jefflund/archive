package rl;

import jade.core.Console;
import jade.util.Config;
import java.awt.Color;
import java.io.FileReader;
import rl.creature.Player;
import rl.world.Dungeon;
import rl.world.Prototype;

public class Rogue extends Console
{
	private static final int WINDOW_SIZE = Player.VISION * 2 + 1;

	private Player player;
	private Dungeon dungeon;

	public static void main(String[] args)
	{
		Rogue rogue = new Rogue();
		frameConsole(rogue, "Jade");
		rogue.gameLoop();
	}

	public Rogue()
	{
		super();
		init();
		player = new Player(this);
		addCamera(player, Player.VISION, Player.VISION);
		dungeon = new Dungeon(10, 0, player);
	}
	
	private void gameLoop()
	{
		while(!player.isExpired())
		{
			clearBuffer();
			buffCamera(player);
			buffMessages(0, dungeon.getLevel().height, dungeon.getLevel());
			buffString(WINDOW_SIZE, 0, player.getStatus(), Color.white);
			refreshScreen();
			dungeon.getLevel().tick();
		}
		exit();
	}
	
	private void init()
	{
		try
		{
			Prototype.loadMonsters(new Config(new FileReader("monster.ini")));
			Prototype.loadItems(new Config(new FileReader("items.ini")));
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}
}
