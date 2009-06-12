package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

/**
 * This implementation of FoV uses spiral path shadowcasting. It works well in
 * traditional roguelike dungeons but is very strange in other map types.
 */
public class ShadowCast implements FoV
{
	private enum Quad
	{
		E, NE, N, NW, W, SW, S, SE, C
	};

	protected ShadowCast()
	{
		throw new IllegalAccessError("This algorithm needs work");
	}

	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		Collection<Coord> fov = new TreeSet<Coord>();
		Queue<Coord> queue = new LinkedList<Coord>();
		queue.add(new Coord(x, y));
		while(!queue.isEmpty())
		{
			Coord coord = queue.remove();
			fov.add(coord);
			if(world.passable(coord.x(), coord.y()))
				passLight(coord, getQuadrant(coord, x, y), queue, fov);
		}
		return fov;
	}

	private void passLight(Coord coord, Quad quadrant, Queue<Coord> queue,
	    Collection<Coord> fov)
	{
		switch(quadrant)
		{
		case C:
			tryAdd(coord.x() + 1, coord.y(), queue, fov);
			tryAdd(coord.x(), coord.y() - 1, queue, fov);
			tryAdd(coord.x() - 1, coord.y(), queue, fov);
			tryAdd(coord.x(), coord.y() + 1, queue, fov);
			break;
		case E:
			tryAdd(coord.x(), coord.y() + 1, queue, fov);
			tryAdd(coord.x() + 1, coord.y(), queue, fov);
			tryAdd(coord.x(), coord.y() - 1, queue, fov);
			break;
		case NE:
			tryAdd(coord.x() + 1, coord.y(), queue, fov);
			tryAdd(coord.x(), coord.y() - 1, queue, fov);
			break;
		case N:
			tryAdd(coord.x() + 1, coord.y(), queue, fov);
			tryAdd(coord.x(), coord.y() - 1, queue, fov);
			tryAdd(coord.x() - 1, coord.y(), queue, fov);
			break;
		case NW:
			tryAdd(coord.x(), coord.y() - 1, queue, fov);
			tryAdd(coord.x() - 1, coord.y(), queue, fov);
			break;
		case W:
			tryAdd(coord.x(), coord.y() - 1, queue, fov);
			tryAdd(coord.x() - 1, coord.y(), queue, fov);
			tryAdd(coord.x(), coord.y() + 1, queue, fov);
			break;
		case SW:
			tryAdd(coord.x() - 1, coord.y(), queue, fov);
			tryAdd(coord.x(), coord.y() + 1, queue, fov);
			break;
		case S:
			tryAdd(coord.x() - 1, coord.y(), queue, fov);
			tryAdd(coord.x(), coord.y() + 1, queue, fov);
			tryAdd(coord.x() + 1, coord.y(), queue, fov);
			break;
		case SE:
			tryAdd(coord.x(), coord.y() + 1, queue, fov);
			tryAdd(coord.x() + 1, coord.y(), queue, fov);
			break;
		}
	}

	private void tryAdd(int x, int y, Queue<Coord> queue, Collection<Coord> fov)
	{
		Coord coord = new Coord(x, y);
		boolean contains = false;
		for(Coord queued : queue)
			if(coord.equals(queued))
				contains = true;
		for(Coord lit : fov)
			if(coord.equals(lit))
				contains = true;
		if(!contains)
			queue.add(coord);
	}

	private Quad getQuadrant(Coord coord, int x0, int y0)
	{
		if(coord.y() < y0)
		{
			if(coord.x() < x0)
				return Quad.NW;
			else if(coord.x() > x0)
				return Quad.NE;
			else
				return Quad.N;
		}
		else if(coord.y() > y0)
		{
			if(coord.x() < x0)
				return Quad.SW;
			else if(coord.x() > x0)
				return Quad.SE;
			else
				return Quad.S;
		}
		else
		{
			if(coord.x() < x0)
				return Quad.W;
			else if(coord.x() > x0)
				return Quad.E;
			else
				return Quad.C;
		}
	}
}
