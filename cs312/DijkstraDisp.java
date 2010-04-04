package cs312;

import jade.core.Console;
import jade.core.World;
import jade.util.type.Coord;

public class DijkstraDisp extends AStarDisp
{
	public DijkstraDisp(Console console)
	{
		super(console);
	}
	
	protected double hEstimate(Coord c1, Coord c2, World world)
	{
		return 0;
	}
}
