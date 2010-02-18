package jade.path;

import jade.util.Coord;

/**
 * Implements path with Dijkstra's algorithm using A* with a heuristic estimate
 * of 0. This will always produce the optimal shortest path if there is one.
 */
public class Dijkstra extends AStar
{
	/**
	 * Dijkstra's heuristic estimate is always 0.
	 */
	protected final double hEstimate(Coord c1, Coord c2)
	{
		return 0;
	}
}
