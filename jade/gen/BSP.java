package jade.gen;

import jade.core.World;
import jade.util.Dice;
import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;

/**
 * This implementation of Gen uses a binary space partition tree
 * 
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
		Color[] colors =
		{Color.white, Color.green, Color.yellow, Color.blue, Color.gray,
		    Color.orange, Color.red, Color.darkGray, Color.lightGray,
		    Color.magenta, Color.pink};
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				world.tile(x, y).setTile('.', Color.white, true);
		Node bsp = new Node(world);
		Collection<Node> leaves = bsp.getLeaves();
		int color = 0;
		for(Node node : leaves)
		{
			for(int x = node.x1; x <= node.x2; x++)
				for(int y = node.y1; y <= node.y2; y++)
					world.tile(x, y).setTile('#', colors[color], false);
			color = (color + 1) % colors.length;
		}
	}

	private class Node
	{
		private int x1;
		private int y1;
		private int x2;
		private int y2;
		private Node left;
		private Node right;
		private Node parent;
		private boolean connected;

		public Node(World world)
		{
			x1 = 0;
			y1 = 0;
			x2 = world.width - 1;
			y2 = world.height - 1;
			divide();
		}

		private Node(Node parent, int div, boolean vert, boolean left)
		{
			this.parent = parent;
			connected = false;
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
		}

		private void divide()
		{
			while(divideAux());
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
			int min = MIN_SIZE + 3;
			if(divTooSmall(vert, min))
				vert = !vert;
			if(divTooSmall(vert, min))
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

		public Collection<Node> getLeaves()
		{
			Collection<Node> leaves = new HashSet<Node>();
			getLeavesAux(leaves);
			return leaves;
		}

		private Collection<Node> getLeavesAux(Collection<Node> leaves)
		{
			if(leaf())
				leaves.add(this);
			else
			{
				left.getLeavesAux(leaves);
				right.getLeavesAux(leaves);
			}
			return leaves;
		}

		private boolean leaf()
		{
			return left == null && right == null;
		}
	}
}
