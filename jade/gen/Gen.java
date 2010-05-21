package jade.gen;

import jade.core.World;
import jade.util.type.Rect;

/**
 * This interface represents something that can generate a map for a Jade World.
 */
public interface Gen
{
	/**
	 * Generates a map on the World with the specified seed.
	 */
	public void generate(World world, long seed);

	/**
	 * Generates a map on the World in the given bounds with the specified seed.
	 */
	public void generate(World world, long seed, Rect rect);

	public class GenFactory
	{
		private static Gen cellular;
		private static Gen bsp;
		private static Gen wilderness;
		private static Gen town;
		private static Gen maze;
		private static Gen traditional;

		/**
		 * Uses cellular automaton to generate cave like maps.
		 */
		public static Gen cellular()
		{
			if(cellular == null)
				cellular = new Cellular();
			return cellular;
		}

		/**
		 * Uses binary space partitioning to generate traditional maps with rooms
		 * and corridors.
		 */
		public static Gen bsp()
		{
			if(bsp == null)
				bsp = new BSP();
			return bsp;
		}

		/**
		 * Generates a fenced space with trees.
		 */
		public static Gen wilderness()
		{
			if(wilderness == null)
				wilderness = new Wilderness(false);
			return wilderness;
		}

		/**
		 * Generates a wilderness with rectangular buildings.
		 */
		public static Gen town()
		{
			if(town == null)
				town = new Wilderness(true);
			return town;
		}
		
		/**
		 * Generates a perfect maze
		 */
		public static Gen maze()
		{
			if(maze == null)
				maze = new Maze();
			return maze;
		}
		
		/**
		 * Generates a grid of interconnected rooms like the origional rogue
		 */
		public static Gen traditional(int roomsX, int roomsY)
		{
			if(traditional == null)
				traditional = new Traditional(roomsX, roomsY);
			((Traditional)traditional).setDims(roomsX, roomsY);
			return traditional;
		}
	}
}
