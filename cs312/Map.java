package cs312;

import jade.core.Console;
import jade.core.World;
import jade.gen.Gen.GenFactory;

public class Map extends World
{
	public Map(int w, int h, long seed)
	{
		super(w, h);
		GenFactory.cellular().generate(this, seed);
	}
	
	public void tick()
	{
	}
	
	public void draw(Console console)
	{
		console.clearBuffer();
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				console.buffChar(x, y, look(x, y));
		console.refreshScreen();
	}
}
