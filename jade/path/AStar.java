package jade.path;

import jade.core.World;
import jade.util.Coord;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class AStar implements Path
{
	private Map<Coord, Node> nodes;

	public AStar()
	{
		nodes = new TreeMap<Coord, Node>();
	}

	public List<Coord> getPath(World world, Coord start, Coord goal)
	{
		nodes.clear();
		Set<Node> closed = new TreeSet<Node>();
		Set<Node> open = new TreeSet<Node>();
		open.add(getNode(start));
		getNode(start).gScore = 0;
		getNode(start).hScore = hEstimate(start, goal);
		getNode(start).fScore = getNode(start).hScore;
		while(!open.isEmpty())
		{
			Node x = minFScore(open);
			if(x.coord.equals(goal))
				return reconstructPath(x);
			open.remove(x);
			closed.add(x);
			for(Node y : getAdjacentNodes(x, world))
			{
				if(closed.contains(y))
					continue;
				double tentativeGScore = x.gScore + hEstimate(x.coord, y.coord);
				boolean tentativeIsBetter = false;
				if(!open.contains(y))
				{
					open.add(y);
					y.hScore = hEstimate(y.coord, goal);
					tentativeIsBetter = true;
				}
				else if(tentativeGScore < y.gScore)
					tentativeIsBetter = true;
				if(tentativeIsBetter)
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
		Set<Node> adjacent = new TreeSet<Node>();
		adjacent.add(getNode(new Coord(node.coord.x() + 1, node.coord.y())));
		adjacent.add(getNode(new Coord(node.coord.x() + 1, node.coord.y() - 1)));
		adjacent.add(getNode(new Coord(node.coord.x() + 1, node.coord.y() + 1)));
		adjacent.add(getNode(new Coord(node.coord.x() - 1, node.coord.y())));
		adjacent.add(getNode(new Coord(node.coord.x() - 1, node.coord.y() - 1)));
		adjacent.add(getNode(new Coord(node.coord.x() - 1, node.coord.y() + 1)));
		adjacent.add(getNode(new Coord(node.coord.x(), node.coord.y() - 1)));
		adjacent.add(getNode(new Coord(node.coord.x(), node.coord.y() + 1)));
		Set<Node> nonpassable = new TreeSet<Node>();
		for(Node n : adjacent)
			if(!world.passable(n.coord.x(), n.coord.y()))
				nonpassable.add(n);
		adjacent.removeAll(nonpassable);
		return adjacent;
	}

	private List<Coord> reconstructPath(Node current)
	{
		if(current.cameFrom != null)
		{
			List<Coord> path = reconstructPath(current.cameFrom);
			path.add(current.coord);
			return path;
		}
		return new LinkedList<Coord>();
	}

	private Node minFScore(Set<Node> set)
	{
		Node min = null;
		for(Node node : set)
			if(min == null || node.fScore < min.fScore)
				min = node;
		return min;
	}

	private double hEstimate(Coord c1, Coord c2)
	{
		int a = c1.x() - c2.x();
		int b = c1.y() - c2.y();
		return Math.sqrt(a * a + b * b);
	}

	private Node getNode(Coord coord)
	{
		Node result = nodes.get(coord);
		if(result == null)
		{
			result = new Node(coord);
			nodes.put(coord, result);
		}
		return result;
	}

	private class Node implements Comparable<Node>
	{
		private Coord coord;
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
