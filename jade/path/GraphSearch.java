package jade.path;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The base class for any graph search path finding algorithms. These algorithms
 * all have the same form, differing only in how the calculate and estimate path
 * traversal costs. The work by exanding the node with the best estimated cost
 * towards the goal. This is done by computing the actual cost of the path to
 * get to the node, and the estimated cost from that node to the goal. The
 * process is repeated until the goal is found.
 */
public abstract class GraphSearch extends Path
{
    @Override
    public List<Coord> calcPath(World world, Coord start, Coord end)
    {
        Map<Coord, Node> nodes = new HashMap<Coord, Node>();
        Set<Node> closed = new HashSet<Node>();
        TreeSet<Node> open = new TreeSet<Node>();
        open.add(getNode(nodes, start));
        getNode(nodes, start).gScore = 0;
        getNode(nodes, start).hScore = hEstimate(world, start, end);
        getNode(nodes, start).fScore = getNode(nodes, start).hScore;
        while(!open.isEmpty())
        {
            Node x = open.first();
            if(x.pos.equals(end))
                return reconstructPath(x);
            open.remove(x);
            closed.add(x);
            for(Coord pos : expandNode(x.pos, world))
            {
                Node y = getNode(nodes, pos);
                if(closed.contains(y))
                    continue;
                double tentativeGScore = x.gScore + gCost(world, x.pos, y.pos);
                boolean tentativeIsBetter = false;
                if(!open.contains(y))
                {
                    open.add(y);
                    y.hScore = hEstimate(world, y.pos, end);
                    tentativeIsBetter = true;
                }
                else if(tentativeGScore < y.gScore)
                    tentativeIsBetter = true;
                if(tentativeIsBetter)
                {
                    open.remove(y);
                    y.prev = x;
                    y.gScore = tentativeGScore;
                    y.fScore = y.gScore + y.hScore;
                    open.add(y);
                }
            }
        }
        return partialPath(nodes, world, end);
    }

    /**
     * Expands a node, yield the valid transitions from that node. Normally this
     * will just be the passable neighbors to a node.
     * @param pos the position being expanded
     * @param world the world to which the position refers
     * @return the neighboring locations to pos
     */
    protected Collection<Coord> expandNode(Coord pos, World world)
    {
        Collection<Coord> expanded = new LinkedList<Coord>();
        for(int x = pos.x() - 1; x <= pos.x() + 1; x++)
            for(int y = pos.y() - 1; y <= pos.y() + 1; y++)
                if(world.passable(x, y))
                    expanded.add(new Coord(x, y));
        return expanded;
    }

    /**
     * Estimates the cost of going from start to end, without actually
     * calculating the cost. This heurstic can be used to inform searches and
     * reduce the run time. Some algorithms, such as A*, can be guaranteed to
     * yield optimal results so long as the heuristic is admissible, meaning it
     * will never overestimate the true cost.
     * @param world the world on which the cost is being estimated
     * @param start the start of the estimated path cost
     * @param end the end of the estimated path cost
     * @return the heurstic estimate of the cost from end to start
     */
    protected abstract double hEstimate(World world, Coord start, Coord end);

    /**
     * Calculates the cost of going from start to end. This method should only
     * be called with there is a valid transition from start to end, so normally
     * start and end will be adjacent tiles.
     * @param world the world on which the cost is being calculated
     * @param start the start of transition
     * @param end the endof transition
     * @return the cacluated cost from end to start
     */
    protected abstract double gCost(World world, Coord start, Coord end);

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

    private List<Coord> partialPath(Map<Coord, Node> nodes, World world,
            Coord end)
    {
        double bestDist = Double.POSITIVE_INFINITY;
        Node bestNode = null;
        for(Node node : nodes.values())
        {
            double dist = hEstimate(world, node.pos, end);
            if(dist < bestDist)
            {
                bestDist = dist;
                bestNode = node;
            }
        }
        return reconstructPath(bestNode);
    }

    protected final Node getNode(Map<Coord, Node> nodes, Coord pos)
    {
        Node node = nodes.get(pos);
        if(node == null)
        {
            node = new Node(pos);
            nodes.put(pos, node);
            return node;
        }
        return node;
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
            Node node = (Node)obj;
            return node.pos.equals(pos);
        }

        @Override
        public int hashCode()
        {
            return pos.hashCode();
        }

        @Override
        public int compareTo(Node other)
        {
            if(fScore < other.fScore)
                return -1;
            else if(fScore > other.fScore)
                return 1;
            else if(other.pos.x() == pos.x())
                return pos.y() - other.pos.y();
            else
                return pos.x() - other.pos.x();
        }
    }
}
