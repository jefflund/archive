package rl;

import jade.core.Console;
import jade.util.Coord;
import java.awt.Color;
import java.util.Random;
import rl.creature.Monster;
import rl.creature.Player;
import rl.item.Item;
import rl.item.Item.Slot;
import rl.world.Dungeon;
import rl.world.Feature;
import rl.world.Level;

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
			Level level = dungeon.getLevel();
			console.clearBuffer();
			for(Coord coord : level.player().getFoV())
				console.buffChar(coord, level.look(coord));
			console.buffString(0, level.height, level.getMessages(), Color.white);
			console.repaint();
			level.tick();
		}
		while(!player.isExpired());
		System.exit(0);
	}
}
