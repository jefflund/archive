package jade.path;

import jade.core.World;
import jade.util.Coord;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This implementation of Path uses the A* pathfinding algorithm. It is very
 * fast and efficient due to its use of a heuristic estimate. It will find the
 * shortest path from start to goal if one exist.
 */
public class AStar implements Path
{
	private final Map<Coord, Node> nodes;

	protected AStar()
	{
		nodes = new TreeMap<Coord, Node>();
	}

	public List<Coord> getPath(World world, Coord start, Coord goal)
	{
		nodes.clear();
		final Set<Node> closed = new TreeSet<Node>();
		final Set<Node> open = new TreeSet<Node>();
		open.add(getNode(start));
		getNode(start).gScore = 0;
		getNode(start).hScore = hEstimate(start, goal);
		getNode(start).fScore = getNode(start).hScore;
		while (!open.isEmpty())
		{
			final Node x = minFScore(open);
			if (x.coord.equals(goal))
				return reconstructPath(x);
			open.remove(x);
			closed.add(x);
			for (final Node y : getAdjacentNodes(x, world))
			{
				if (closed.contains(y))
					continue;
				final double tentativeGScore = x.gScore + hEstimate(x.coord, y.coord);
				boolean tentativeIsBetter = false;
				if (!open.contains(y))
				{
					open.add(y);
					y.hScore = hEstimate(y.coord, goal);
					tentativeIsBetter = true;
				}
				else if (tentativeGScore < y.gScore)
					tentativeIsBetter = true;
				if (tentativeIsBetter)
				{
					y.cameFrom = x;
					y.gScore = tentativeGScore;
					y.fScore = y.gScore + y.hScore;
				}
			}
		}
		return null;
	}

	public boolean hasPath(World world, Coord start, Coord goal)
	{
		return getPath(world, start, goal) != null;
	}

	private Set<Node> getAdjacentNodes(Node node, World world)
	{
		final Set<Node> adjacent = new TreeSet<Node>();
		adjacent.add(getNode(new Coord(node.coord.x() + 1, node.coord.y())));
		adjacent.add(getNode(new Coord(node.coord.x() + 1, node.coord.y() - 1)));
		adjacent.add(getNode(new Coord(node.coord.x() + 1, node.coord.y() + 1)));
		adjacent.add(getNode(new Coord(node.coord.x() - 1, node.coord.y())));
		adjacent.add(getNode(new Coord(node.coord.x() - 1, node.coord.y() - 1)));
		adjacent.add(getNode(new Coord(node.coord.x() - 1, node.coord.y() + 1)));
		adjacent.add(getNode(new Coord(node.coord.x(), node.coord.y() - 1)));
		adjacent.add(getNode(new Coord(node.coord.x(), node.coord.y() + 1)));
		final Set<Node> nonpassable = new TreeSet<Node>();
		for (final Node n : adjacent)
			if (!world.passable(n.coord.x(), n.coord.y()))
				nonpassable.add(n);
		adjacent.removeAll(nonpassable);
		return adjacent;
	}

	private List<Coord> reconstructPath(Node current)
	{
		if (current.cameFrom != null)
		{
			final List<Coord> path = reconstructPath(current.cameFrom);
			path.add(current.coord);
			return path;
		}
		return new LinkedList<Coord>();
	}

	private Node minFScore(Set<Node> set)
	{
		Node min = null;
		for (final Node node : set)
			if (min == null || node.fScore < min.fScore)
				min = node;
		return min;
	}

	/**
	 * This method is the heuristic estimate used to optimize A*. In other words,
	 * it is the likley hood that a square will be on the shortest path between
	 * two nodes. By default, it just calculates the distance from the c1 to c2,
	 * but could be overridden to factor in other cost in node traversal.
	 * @param c1 the first node in question
	 * @param c2 the second node in question
	 * @return the heuristic estimate of the cost to travel between c1 and c2
	 */
	protected double hEstimate(Coord c1, Coord c2)
	{
		final int a = c1.x() - c2.x();
		final int b = c1.y() - c2.y();
		return Math.sqrt(a * a + b * b);
	}

	private Node getNode(Coord coord)
	{
		Node result = nodes.get(coord);
		if (result == null)
		{
			result = new Node(coord);
			nodes.put(coord, result);
		}
		return result;
	}

	private class Node implements Comparable<Node>
	{
		private final Coord coord;
		private double gScore;
		private double hScore;
		private double fScore;
		private Node cameFrom;

		public Node(Coord coord)
		{
			this.coord = coord;
		}

		public int compareTo(Node other)
		{
			return coord.compareTo(other.coord);
		}
	}
}
