package jade.path;

import jade.util.Coord;

public class Djikstra extends AStar
{
	protected Djikstra()
  {
  }
	
	protected double hEstimate(Coord c1, Coord c2)
	{
	  return 0;
	}
}
