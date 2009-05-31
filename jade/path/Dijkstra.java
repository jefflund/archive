package jade.path;

import jade.core.World;
import jade.util.Coord;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Dijkstra implements Path
{
	private Map<Coord, Node> nodes;

	public Dijkstra()
	{
		nodes = new TreeMap<Coord, Node>();
	}

	public List<Coord> getPath(World world, Coord start, Coord goal)
	{
		initNodes(world, start);
		Node goalNode = getNode(goal);
		while(!nodes.isEmpty())
		{
			Node min = getMinDist();
			if(min.distance == Double.POSITIVE_INFINITY)
				break;
			removeNode(min);
			for(Node node : getAdjacentNodes(min))
			{
				double alt = min.distance + distance(min.coord, node.coord);
				if(alt < node.distance)
					node.distance = alt;
				node.previous = min;
			}
		}
		return reconstructPath(goalNode);
	}
	
	private List<Coord> reconstructPath(Node node)
  {
		if(node.previous != null)
		{
			List<Coord> path = reconstructPath(node.previous);
			path.add(node.coord);
			return path;
		}
		else
			return new LinkedList<Coord>();
  }

	private double distance(Coord c1, Coord c2)
  {
		int a = c1.x() - c2.x();
		int b = c1.y() - c2.y();
	  return Math.sqrt(a * a + b * b);
  }

	private Set<Node> getAdjacentNodes(Node node)
  {
		Set<Node> adjacent = new TreeSet<Node>();
		for(int x = node.coord.x() - 1; x <= node.coord.x() + 1; x++)
			for(int y = node.coord.y() - 1; y <= node.coord.y() + 1; y++)
			{
				Coord coord = new Coord(x, y);
				if(!coord.equals(node.coord) && nodes.containsKey(coord))
					adjacent.add(nodes.get(coord));
			}
		return adjacent;
  }

	private Node getMinDist()
	{
		Node min = null;
		for(Node node : nodes.values())
			if(min == null || node.distance < min.distance)
				min = node;
		return min;
	}

	private void initNodes(World world, Coord start)
	{
		nodes.clear();
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				if(world.passable(x, y))
				{
					Coord coord = new Coord(x, y);
					nodes.put(coord, new Node(coord));
				}
		getNode(start).distance = 0;
	}

	public Node getNode(Coord coord)
	{
		return nodes.get(coord);
	}

	public Node getNode(int x, int y)
	{
		return getNode(new Coord(x, y));
	}

	public void removeNode(Node node)
	{
		nodes.remove(node.coord);
	}

	private class Node implements Comparable<Node>
	{
		private Coord coord;
		private double distance;
		private Node previous;

		public Node(Coord coord)
		{
			this.coord = coord;
			distance = Double.POSITIVE_INFINITY;
			previous = null;
		}

		public int compareTo(Node other)
		{
			return coord.compareTo(other.coord);
		}
	}
}
