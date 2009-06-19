package jade.gen;

import jade.core.World;
import jade.util.Coord;
import jade.util.Dice;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

/**
 * This implementation of Gen uses a binary space partition tree to generate a
 * traditional looking dungeon with a set of interconnected rectangular rooms.
 */
public class Traditional implements Gen
{
	private Dice dice;
	private static final int MIN_SIZE = 3;

	protected Traditional()
	{
		dice = new Dice();
	}

	public void generate(World world, long seed)
	{
		dice.setSeed(seed);
		floodWalls(world);
		BSP head = new BSP(world);
		head.divide();
		head.makeRooms(world);
		head.connect(world);
		head.removeUnconnected(world);
	}

	private void floodWalls(World world)
	{
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				world.tile(x, y).setTile('#', Color.white, false);
	}

	private class BSP
	{
		private int x1;
		private int y1;
		private int x2;
		private int y2;
		private int rx1;
		private int ry1;
		private int rx2;
		private int ry2;
		private BSP left;
		private BSP right;
		private BSP parent;
		private boolean connected;

		public BSP(World world)
		{
			x1 = 0;
			y1 = 0;
			x2 = world.width - 1;
			y2 = world.height - 1;
			connected = true;
		}

		private BSP(BSP parent, int div, boolean vert, boolean left)
		{
			this.parent = parent;
			if(vert)
			{
				if(left)
				{
					x1 = parent.x1;
					y1 = parent.y1;
					x2 = parent.x1 + div;
					y2 = parent.y2;
				}
				else
				{
					x1 = parent.x1 + div + 1;
					y1 = parent.y1;
					x2 = parent.x2;
					y2 = parent.y2;
				}
			}
			else
			{
				if(left)
				{
					x1 = parent.x1;
					y1 = parent.y1;
					x2 = parent.x2;
					y2 = parent.y1 + div;
				}
				else
				{
					x1 = parent.x1;
					y1 = parent.y1 + div + 1;
					x2 = parent.x2;
					y2 = parent.y2;
				}
			}
			connected = false;
		}

		public void removeUnconnected(World world)
		{
			if(leaf())
			{
				boolean connected = false;
				for(int x = rx1; x <= rx2; x++)
					if(world.passable(x, ry1 - 1) || world.passable(x, ry2 + 1))
						connected = true;
				for(int y = ry1; y <= ry2; y++)
					if(world.passable(rx1 - 1, y) || world.passable(rx2 + 1, y))
						connected = true;
				if(!connected)
					for(int x = rx1; x <= rx2; x++)
						for(int y = ry1; y <= ry2; y++)
							world.tile(x, y).setTile('#', Color.white, false);
			}
			else
			{
				left.removeUnconnected(world);
				right.removeUnconnected(world);
			}
		}

		public void makeRooms(World world)
		{
			if(leaf())
			{
				rx1 = dice.nextInt(x1 + 1, x2 - 1 - MIN_SIZE);
				rx2 = dice.nextInt(rx1 + MIN_SIZE, x2 - 1);
				ry1 = dice.nextInt(y1 + 1, y2 - 1 - MIN_SIZE);
				ry2 = dice.nextInt(ry1 + MIN_SIZE, y2 - 1);
				for(int x = rx1; x <= rx2; x++)
					for(int y = ry1; y <= ry2; y++)
						world.tile(x, y).setTile('.', Color.white, true);
			}
			else
			{
				left.makeRooms(world);
				right.makeRooms(world);
			}
		}

		public void divide()
		{
			while(divideAux())
				;
		}

		public void connect(World world)
		{
			if(!leaf())
			{
				left.connect(world);
				right.connect(world);
			}
			connectToSibling(world);
		}

		private boolean divideAux()
		{
			if(!leaf())
			{
				boolean divleft = left.divideAux();
				boolean divRight = right.divideAux();
				return divleft || divRight;
			}
			boolean vert = dice.nextBoolean();
			int min = MIN_SIZE + 4;
			if(divTooSmall(vert, min))
				vert = !vert;
			if(divTooSmall(vert, min))
				return false;
			int div = dice.nextInt(min, (vert ? x2 - x1 : y2 - y1) - min);
			left = new BSP(this, div, vert, true);
			right = new BSP(this, div, vert, false);
			return true;
		}

		private boolean divTooSmall(boolean vert, int min)
		{
			min *= 2;
			return vert ? (x2 - x1) < min : (y2 - y1) < min;
		}

		private boolean leaf()
		{
			return left == null && right == null;
		}

		private void connectToSibling(World world)
		{
			if(connected)
				return;
			BSP sibling = this == parent.left ? parent.right : parent.left;
			Coord start = world.getOpenTile(dice, x1, y1, x2, y2);
			Coord end = world.getOpenTile(dice, sibling.x1, sibling.y1, sibling.x2,
			    sibling.y2);
			Coord curr = new Coord(start);
			List<Coord> corridor = new LinkedList<Coord>();
			while(!curr.equals(end))
			{
				corridor.add(new Coord(curr));
				boolean digging = !world.passable(curr);
				if(curr.x() == end.x())
					curr.translate(0, curr.y() < end.y() ? 1 : -1);
				else
					curr.translate(curr.x() < end.x() ? 1 : -1, 0);
				if(digging && world.passable(curr))
				{
					if(sibling.inside(curr))
						break;
					corridor.clear();
				}
			}
			for(Coord coord : corridor)
				world.tile(coord).setTile('.', Color.white, true);
			connected = true;
			sibling.connected = true;
		}

		private boolean inside(Coord coord)
		{
			return coord.x() < x2 && coord.x() > x1 && coord.y() < y2
			    && coord.y() > y1;
		}
	}
}
