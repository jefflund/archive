package rl;

import jade.core.Console;
import jade.util.Tools;
import java.awt.Color;
import java.util.Random;
import rl.creature.Monster;
import rl.creature.Player;
import rl.item.Item;
import rl.item.Item.Slot;
import rl.world.Dungeon;
import rl.world.Feature;

public class Demo
{
	public static void main(String[] args)
	{
		Console console = Console.getFramedConsole("Jade");
		Dungeon dungeon = new Dungeon();
		Player player = new Player(console, dungeon);
		Random random = new Random();
		dungeon.getLevel().addActor(player, random);
		dungeon.getLevel().addActor(new Monster('D', Color.red), random);
		dungeon.getLevel()
		    .addActor(new Item('|', Color.white, Slot.Weapon), random);
		dungeon.getLevel().addActor(new Feature('^', Color.blue), random);
		do
		{
			for(int x = 0; x < dungeon.getLevel().width; x++)
				for(int y = 0; y < dungeon.getLevel().height; y++)
					console.buffChar(x, y, dungeon.getLevel().look(x, y));
			console.buffString(0, dungeon.getLevel().height, Tools.strEnsureLength(
			    dungeon.getLevel().getMessages(), dungeon.getLevel().width),
			    Color.white);
			console.repaint();
			dungeon.getLevel().tick();
		}
		while(!player.isExpired());
		System.exit(0);
	}
}
