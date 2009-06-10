package jade.gen;

import jade.core.World;
import java.util.HashMap;
import java.util.Map;

/**
 * An inteface used for generating random maps on a jade world.
 */
public interface Gen
{
	/**
	 * A simple wilderness.
	 */
	public static final int Wilderness = 0;
	/**
	 * Uses cellular autamaton to generate interesting caves.
	 */
	public static final int Cellular = 1;
	/**
	 * Takes a simple wilderness and adds a few buildings.
	 */
	public static final int Town = 2;

	/**
	 * Generates a random map on the given world based on a seed. Implementations
	 * of this method should generate the same map for any particular seed.
	 * @param world the world on which to generate the map
	 * @param seed the seed to base the random map on
	 */
	public void generate(World world, long seed);

	/**
	 * Used for retrieving varius singleton instances of Gen implementing classes.
	 */
	public class GenFactory
	{
		private static Map<Integer, Gen> singletons = new HashMap<Integer, Gen>();

		/**
		 * Returns a instance implementing Gen based on the specified algorithm.
		 * 
		 * @param algorithm the algorithm to be implemented. Static integer
		 * constants are provided in Gen
		 * @return a singleton instance of an object implementing Gen with specified
		 * algorithm
		 */
		public static Gen get(int algorithm)
		{
			Gen result = singletons.get(algorithm);
			if(result == null)
				result = getNew(algorithm);
			return result;
		}

		private static Gen getNew(int algorithm)
		{
			switch(algorithm)
			{
			case Wilderness:
				return new Wilderness();
			case Cellular:
				return new Cellular();
			case Town:
				return new Town();
			default:
				throw new IllegalArgumentException();
			}
		}
	}
}
