package rl;

import jade.core.Console;
import jade.util.Tools;
import java.awt.Color;
import java.util.Random;
import rl.creature.Monster;
import rl.creature.Player;
import rl.item.Item;
import rl.world.Level;

public class Demo
{
	public static void main(String[] args)
	{
		Level level = new Level();
		Console console = Console.getFramedConsole("Jade");
		Player player = new Player(console);
		level.addActor(player, new Random(0));
		level.addActor(new Monster('D', Color.red), new Random(0));
		level.addActor(new Item('$', Color.yellow), new Random(0));

		do
		{
			for(int x = 0; x < level.width; x++)
				for(int y = 0; y < level.height; y++)
					console.buffChar(x, y, level.look(x, y));
			console.buffString(0, level.height, Tools.strEnsureLength(level
			    .getMessages(), level.width), Color.white);
			console.repaint();
			level.tick();
		}
		while(!player.isExpired());
		
		System.exit(0);
	}
}
