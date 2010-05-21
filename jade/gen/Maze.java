package jade.gen;

import jade.core.World;
import jade.util.Dice;
import jade.util.type.Coord;
import jade.util.type.Rect;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class Maze implements Gen
{
	private Dice dice;

	public Maze()
	{
		dice = new Dice();
	}

	public void generate(World world, long seed)
	{
		generate(world, seed, new Rect(world.width, world.height));
	}

	public void generate(World world, long seed, Rect rect)
	{
		dice.setSeed(seed);
		Set<Coord> cells = new TreeSet<Coord>();
		for(int x = rect.xMin(); x < rect.xMax(); x++)
			for(int y = rect.yMin(); y < rect.yMax(); y++)
			{
				if((x + 1) % 2 == 0 && (y + 1) % 2 == 0 && x < world.width - 1
						&& y < world.height - 1)
				{
					world.tile(x, y).setTile('.', Color.white, true);
					cells.add(new Coord(x, y));
				}
				else
					world.tile(x, y).setTile('#', Color.white, false);
			}
		Stack<Coord> stack = new Stack<Coord>();
		stack.push(world.getOpenTile(dice));
		cells.remove(stack.peek());
		while(!stack.isEmpty())
		{
			Coord curr = stack.peek();
			Coord next = getNeighbor(curr, cells);
			if(next == null)
				stack.pop();
			else
			{
				Coord dig = curr.getTranslated(curr.directionTo(next));
				world.tile(dig).setTile('.', Color.white, true);
				stack.push(next);
			}
		}
	}

	private Coord getNeighbor(Coord coord, Set<Coord> cells)
	{
		List<Coord> possible = new ArrayList<Coord>();
		tryAddPossible(possible, cells, coord.getTranslated(0, -2));
		tryAddPossible(possible, cells, coord.getTranslated(0, 2));
		tryAddPossible(possible, cells, coord.getTranslated(2, 0));
		tryAddPossible(possible, cells, coord.getTranslated(-2, 0));
		if(possible.size() == 0)
			return null;
		else
		{
			Coord neighbor = possible.get(dice.nextInt(possible.size()));
			cells.remove(neighbor);
			return neighbor;
		}
	}

	private void tryAddPossible(List<Coord> list, Set<Coord> cells, Coord coord)
	{
		if(cells.contains(coord))
			list.add(coord);
	}
}
