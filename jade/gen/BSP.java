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

	protected BSP()
	{
		dice = new Dice();
		// throw new UnsupportedOperationException("BSP not implemented yet");
	}

	public void generate(World world, long seed)
	{
		dice.setSeed(seed);
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				world.tile(x, y).setTile('.', Color.white, true);
		Node bsp = new Node(world, 3);
		Collection<Node> leaves = bsp.getLeaves();
		for(Node node : leaves)
		{
			for(int x = node.x1; x <= node.x2; x++)
			{
				world.tile(x, node.y1).setTile('#', Color.white, false);
				world.tile(x, node.y2).setTile('#', Color.white, false);
			}
			for(int y = node.y1; y <= node.y2; y++)
			{
				world.tile(node.x1, y).setTile('#', Color.white, false);
				world.tile(node.x2, y).setTile('#', Color.white, false);
			}
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
		private int depth;

		public Node(World world, int depth)
		{
			this.depth = depth;
			x1 = 0;
			y1 = 0;
			x2 = world.width - 1;
			y2 = world.height - 1;
			divide();
		}

		private Node(Node parent, int div, boolean vert, boolean left, int depth)
		{
			this.depth = depth;
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
			divide();
		}

		private void divide()
		{
			if(depth > 0)
			{
				boolean vert = dice.nextBoolean();
				int div = vert ? (x2 - x1) / 2 : (y2 - y1) / 2;
				left = new Node(this, div, vert, true, depth - 1);
				right = new Node(this, div, vert, false, depth - 1);
			}
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
