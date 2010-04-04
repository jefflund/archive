package cs312;

import jade.core.Console;
import jade.path.Path;
import jade.util.Dice;
import jade.util.type.Coord;
import java.awt.Color;

public class Main
{
	private static final int t = 12;
	private static final int w = 80;
	private static final int h = 24;

	private static Console console = Console.getFramedConsole("CS 312", t, w, h);
	private static AStarDisp astar = new AStarDisp(console);
	private static DijkstraDisp dijk = new DijkstraDisp(console);

	private static Map map;
	private static Coord start;
	private static Coord goal;

	public static void main(String[] args)
	{
		while(true)
		{
			init();
			showPath(astar);
			reset();
			showPath(dijk);
		}
	}

	private static void showPath(Path path)
	{
		path.getPath(map, start, goal);
		console.getKey();
	}
	
	private static void reset()
	{
		map.draw(console);
		console.buffChar(start, '*', Color.green);
		console.buffChar(goal, '*', Color.red);
		console.refreshScreen();
		console.getKey();
	}

	private static void init()
	{
		map = new Map(w, h, System.currentTimeMillis());
		start = map.getOpenTile(Dice.global);
		goal = map.getOpenTile(Dice.global);
		reset();
	}
}
