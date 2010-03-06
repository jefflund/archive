package rl;

import jade.core.Console;
import java.awt.Color;
import rl.creature.Player;
import rl.world.Dungeon;

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
}
