package jade.gen;

import jade.core.World;
import jade.util.Dice;
import jade.util.type.Coord;
import jade.util.type.Rect;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Uses binary space partitioning to make a map with rooms and corridors. By
 * virtue of the partitioning tree, the maps are guaranteed to be connected. As
 * a final step, some cycles are added to the tree to make the maps more
 * interesting and playable.
 */
public class BSP implements Gen
{
	private final Dice dice;
	private static final int MIN_SIZE = 3;

	protected BSP()
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
		floodWithWall(world, rect);
		final BSPNode head = new BSPNode(rect);
		head.divide();
		head.makeRooms(world);
		head.connect(world);
		head.addCycles(world);
	}

	private void floodWithWall(World world, Rect rect)
	{
		for(int x = rect.xMin(); x < rect.xMax(); x++)
			for(int y = rect.yMin(); y < rect.yMax(); y++)
				world.tile(x, y).setTile('#', Color.white, false);
	}

	private class BSPNode
	{
		private final int x1;
		private final int y1;
		private final int x2;
		private final int y2;
		private int rx1;
		private int ry1;
		private int rx2;
		private int ry2;
		private BSPNode left;
		private BSPNode right;
		private BSPNode parent;
		private boolean connected;
		private boolean readyConnect;

		public BSPNode(Rect rect)
		{
			connected = true;
			readyConnect = true;
			x1 = rect.xMin();
			y1 = rect.yMin();
			x2 = rect.xMax() - 1;
			y2 = rect.yMax() - 1;
		}

		private BSPNode(BSPNode parent, int div, boolean vert, boolean left)
		{
			this.parent = parent;
			connected = false;
			readyConnect = true;
			x1 = parent.x1 + (vert && !left ? div + 1 : 0);
			y1 = parent.y1 + (!vert && !left ? div + 1 : 0);
			x2 = vert && left ? parent.x1 + div : parent.x2;
			y2 = !vert && left ? parent.y1 + div : parent.y2;
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
			readyConnect = true;
			connectToSibling(world);
		}

		public void addCycles(World world)
		{
			List<BSPNode> leaves = new ArrayList<BSPNode>(getLeaves());
			if(leaves.size() < 5)
				return;
			for(int i = 0; i < dice.nextInt(leaves.size() / 2, leaves.size()); i++)
			{
				BSPNode leaf1 = leaves.remove(leaves.size() - 1);
				BSPNode leaf2 = leaves.get(dice.nextInt(leaves.size()));
				leaf1.connectTo(world, leaf2);
			}
		}

		private boolean divideAux()
		{
			if(!leaf())
			{
				final boolean divleft = left.divideAux();
				final boolean divRight = right.divideAux();
				return divleft || divRight;
			}
			boolean vert = dice.nextBoolean();
			final int min = MIN_SIZE + 4;
			if(divTooSmall(vert, min))
				vert = !vert;
			if(divTooSmall(vert, min))
				return false;
			final int div = dice.nextInt(min, (vert ? x2 - x1 : y2 - y1) - min);
			left = new BSPNode(this, div, vert, true);
			right = new BSPNode(this, div, vert, false);
			readyConnect = false;
			return true;
		}

		private boolean divTooSmall(boolean vert, int min)
		{
			min *= 2;
			return vert ? (x2 - x1) < min : (y2 - y1) < min;
		}

		public Set<BSPNode> getLeaves()
		{
			Set<BSPNode> leaves = new HashSet<BSPNode>();
			getLeavesAux(leaves);
			return leaves;
		}

		private void getLeavesAux(Set<BSPNode> leaves)
		{
			if(leaf())
				leaves.add(this);
			else
			{
				if(left != null)
					left.getLeavesAux(leaves);
				if(right != null)
					right.getLeavesAux(leaves);
			}
		}

		private boolean leaf()
		{
			return left == null && right == null;
		}

		private void connectToSibling(World world)
		{
			if(connected)
				return;
			final BSPNode sibling = this == parent.left ? parent.right : parent.left;
			if(!sibling.readyConnect)
				return;
			final Coord start = world.getOpenTile(dice, x1, y1, x2, y2);
			final Coord end = world.getOpenTile(dice, sibling.x1, sibling.y1,
					sibling.x2, sibling.y2);
			final List<Coord> corridor = new LinkedList<Coord>();
			final Coord curr = new Coord(start);
			while(!curr.equals(end))
			{
				corridor.add(new Coord(curr));
				final boolean digging = !world.passable(curr);
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
			for(final Coord coord : corridor)
				world.tile(coord).setTile('.', Color.white, true);
			connected = true;
			sibling.connected = true;
		}

		public void connectTo(World world, BSPNode leaf)
		{
			final Coord start = world.getOpenTile(dice, x1, y1, x2, y2);
			final Coord end = world.getOpenTile(dice, leaf.x1, leaf.y1, leaf.x2,
					leaf.y2);
			final List<Coord> corridor = new LinkedList<Coord>();
			final Coord curr = new Coord(start);
			while(!curr.equals(end))
			{
				corridor.add(new Coord(curr));
				final boolean digging = !world.passable(curr);
				if(curr.x() == end.x())
					curr.translate(0, curr.y() < end.y() ? 1 : -1);
				else
					curr.translate(curr.x() < end.x() ? 1 : -1, 0);
				if(digging && world.passable(curr))
				{
					if(leaf.inside(curr))
						break;
					corridor.clear();
				}
			}
			for(final Coord coord : corridor)
				world.tile(coord).setTile('.', Color.white, true);
		}

		private boolean inside(Coord coord)
		{
			return coord.x() < x2 && coord.x() > x1 && coord.y() < y2
					&& coord.y() > y1;
		}
	}
}
