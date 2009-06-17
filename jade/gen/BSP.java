package jade.gen;

import jade.core.World;
import jade.util.Dice;
import java.awt.Color;

/**
 * This implementation of Gen uses a binary space partition tree
 */
public class BSP implements Gen
{
	private Dice dice;
	private static final int MIN_SIZE = 3;

	protected BSP()
	{
		dice = new Dice();
	}

	public void generate(World world, long seed)
	{
		dice.setSeed(seed);
		floodWalls(world);
		Node head = new Node(world);
		head.divide();
		head.makeRooms(world);
		head.connect(world);
	}

	private void floodWalls(World world)
	{
		for (int x = 0; x < world.width; x++)
			for (int y = 0; y < world.height; y++)
				world.tile(x, y).setTile('#', Color.white, false);
	}

	private class Node
	{
		private int x1;
		private int y1;
		private int x2;
		private int y2;
		private int rx1;
		private int ry1;
		private int rx2;
		private int ry2;
		private Node left;
		private Node right;
		private Node parent;

		public Node(World world)
		{
			x1 = 0;
			y1 = 0;
			x2 = world.width - 1;
			y2 = world.height - 1;
		}
		
		private Node(Node parent, int div, boolean vert, boolean left)
		{
			this.parent = parent;
			if (vert)
			{
				if (left)
				{
					x1 = parent.x1;
					y1 = parent.y1;
					x2 = parent.x1 + div;
					y2 = parent.y2;
				} else
				{
					x1 = parent.x1 + div + 1;
					y1 = parent.y1;
					x2 = parent.x2;
					y2 = parent.y2;
				}
			} else
			{
				if (left)
				{
					x1 = parent.x1;
					y1 = parent.y1;
					x2 = parent.x2;
					y2 = parent.y1 + div;
				} else
				{
					x1 = parent.x1;
					y1 = parent.y1 + div + 1;
					x2 = parent.x2;
					y2 = parent.y2;
				}
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
			for (int x = rx1; x <= rx2; x++)
				for (int y = ry1; y <= ry2; y++)
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
			while(divideAux());
		}

		private boolean divideAux()
		{
			if (!leaf())
			{
				boolean divleft = left.divideAux();
				boolean divRight = right.divideAux();
				return divleft || divRight;
			}
			boolean vert = dice.nextBoolean();
			int min = MIN_SIZE + 4;
			if (divTooSmall(vert, min))
				vert = !vert;
			if (divTooSmall(vert, min))
				return false;
			int div = dice.nextInt(min, (vert ? x2 - x1 : y2 - y1) - min);
			left = new Node(this, div, vert, true);
			right = new Node(this, div, vert, false);
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
		
		public void connect(World world)
		{
			if(!leaf())
			{
				left.connect(world);
				right.connect(world);
			}
			if(parent != null)
				connectToSibling(world);
		}

		private void connectToSibling(World world)
		{
			Node sibling = this == parent.left ? parent.right : parent.left;
			Color newColor = dice.nextColor();
			char newChar = dice.nextChar('a', 'z');
			colorRoom(world, newChar, newColor);
			sibling.colorRoom(world, newChar, newColor);
		}

		private void colorRoom(World world, char newChar, Color newColor)
		{
			if(leaf())
			{
			for (int x = rx1; x <= rx2; x++)
				for (int y = ry1; y <= ry2; y++)
					world.tile(x, y).setTile(newChar, newColor, true);
			}
			else
			{
				left.colorRoom(world, newChar, newColor);
				right.colorRoom(world, newChar, newColor);
			}
		}
	}
}
