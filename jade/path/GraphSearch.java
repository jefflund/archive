package jade.path;

import jade.core.World;
import jade.util.type.Coord;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base class for the general graph search. In general, the algorithm has a cost
 * function and a heuristic. The cost function describes the cost in going from
 * one node to another, and should be exact. The heuristic function is an
 * estimate of the total cost of a path from one node to the goal node. This
 * estimate need not be exact, but in some cases must be admissible, never
 * overestimating the cost of the optimal path. These algorithms tend to be
 * complete, even if non-optimal.
 */
public abstract class GraphSearch implements Path
{
    @Override
    public List<Coord> getPath(World world, Coord start, Coord goal)
    {
        Node[][] nodes = new Node[world.width()][world.height()];
        Set<Node> closed = new TreeSet<Node>();
        TreeSet<Node> open = new TreeSet<Node>();
        open.add(getNode(nodes, start));
        getNode(nodes, start).gScore = 0;
        getNode(nodes, start).hScore = hEstimate(world, start, goal);
        getNode(nodes, start).fScore = getNode(nodes, start).hScore;
        while(!open.isEmpty())
        {
            Node x = open.first();
            if(x.pos.equals(goal))
                return reconstructPath(x);
            open.remove(x);
            closed.add(x);
            for(Node y : expandNode(nodes, x, world))
            {
                if(closed.contains(y))
                    continue;
                double tempG = x.gScore + gFunction(world, x.pos, y.pos);
                boolean useTempG = false;
                if(!open.contains(y))
                {
                    open.add(y);
                    y.hScore = hEstimate(world, y.pos, goal);
                    useTempG = true;
                }
                else if(tempG < y.gScore)
                    useTempG = true;
                if(useTempG)
                {
                    open.remove(y);
                    y.prev = x;
                    y.gScore = tempG;
                    y.fScore = y.gScore + y.hScore;
                    open.add(y);
                }
            }
        }
        return null;
    }

    protected abstract double hEstimate(World world, Coord start, Coord goal);

    protected abstract double gFunction(World world, Coord start, Coord goal);

    protected Set<Node> expandNode(Node[][] nodes, Node node, World world)
    {
        Set<Node> adjacent = new TreeSet<Node>();
        for(int dx = -1; dx <= 1; dx++)
            for(int dy = -1; dy <= 1; dy++)
            {
                Coord coord = node.pos.getTranslated(dx, dy);
                if(world.passable(coord))
                    adjacent.add(getNode(nodes, coord));
            }
        return adjacent;
    }

    private List<Coord> reconstructPath(Node node)
    {
        if(node.prev != null)
        {
            List<Coord> path = reconstructPath(node.prev);
            path.add(node.pos);
            return path;
        }
        return new LinkedList<Coord>();
    }

    private Node getNode(Node[][] nodes, Coord pos)
    {
        if(nodes[pos.x()][pos.y()] == null)
            nodes[pos.x()][pos.y()] = new Node(pos);
        return nodes[pos.x()][pos.y()];
    }

    private class Node implements Comparable<Node>
    {
        private final Coord pos;
        private double gScore;
        private double hScore;
        private double fScore;
        private Node prev;

        public Node(Coord pos)
        {
            this.pos = pos;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj == null || !(obj instanceof Coord))
                return false;
            Coord othercoord = ((Node)obj).pos;
            return othercoord.equals(pos);
        }

        @Override
        public int hashCode()
        {
            return pos.hashCode();
        }

        public int compareTo(Node other)
        {
            if(fScore < other.fScore)
                return -1;
            else if(fScore > other.fScore)
                return 1;
            else if(pos.x() == other.pos.x())
                return pos.y() - other.pos.y();
            else
                return pos.x() - other.pos.x();
        }
    }
}
