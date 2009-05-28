package jade;

import jade.core.Console;
import jade.core.World;
import jade.fov.FoV;
import jade.fov.FoV.FoVFactory;
import jade.gen.Gen;
import jade.gen.Gen.GenFactory;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Tools;
import java.awt.Color;
import java.util.Random;

public class Demo
{
	public static void main(String[] args)
	{
		Console console = Console.getFramedConsole("Demo");
		World world = new World(80, 24)
		{
			public void tick()
			{
			}
		};
		Gen gen = GenFactory.get(Gen.CellularDungeon);
		FoV fov = FoVFactory.get(FoV.ShadowCast);
		Coord player = null;
		player = generate(world, gen);
		char key = '\0';
		do
		{
			for(int x = 0; x < world.width; x++)
				for(int y = 0; y < world.height; y++)
					console.buffChar(x, y, new ColoredChar(world.look(x, y).ch(),
					    Color.darkGray));
			console.buffChar(player, new ColoredChar('@', Color.red));
			for(Coord coord : fov.calcFoV(world, player.x(), player.y(), 2))
				console.buffChar(coord, world.look(coord.x(), coord.y()));
			console.buffChar(player, new ColoredChar('@', Color.red));
			console.repaint();
			key = console.getKey();
			switch(key)
			{
			case 'r':
				player = generate(world, gen);
				break;
			default:
				Coord dir = Tools.keyToDir(key, true, false);
				if(dir != null)
					player.translate(dir);
			}
		}
		while(key != 'q');
		System.exit(0);
	}

	private static Coord generate(World world, Gen gen)
	{
		Coord player;
		gen.generate(world, new Random().nextLong());
		player = world.getOpenTile(new Random());
		return player;
	}
}
