package jade.fov;

import jade.core.Actor;
import jade.core.World;
import jade.util.Coord;
import java.util.Collection;

/**
 * This interface represents something that can calculate field of vision.
 */
public interface FoV
{
	/**
	 * Returns the Coords that are visible from the given location
	 */
	public Collection<Coord> calcFoV(World world, int x, int y, int range);

	/**
	 * Returns the Coords that are visible from the Actor's location
	 */
	public Collection<Coord> calcFoV(Actor actor, int range);

	public class FoVFactory
	{
		private static FoV raySquare;
		private static FoV rayCircle;
		private static FoV shadowSquare;
		private static FoV shadowCircle;

		/**
		 * Raycasting with a square bound.
		 */
		public static FoV raySquare()
		{
			if(raySquare == null)
				raySquare = new Raycast(false);
			return raySquare;
		}

		/**
		 * Raycasting with a circular bound.
		 */
		public static FoV rayCircle()
		{
			if(rayCircle == null)
				rayCircle = new Raycast(true);
			return rayCircle;
		}

		/**
		 * Shadowcasting with a square bound.
		 */
		public static FoV shadowSquare()
		{
			if(shadowSquare == null)
				shadowSquare = new Shadowcast(false);
			return shadowSquare;
		}

		/**
		 * Shadowcasting with a circular bound.
		 */
		public static FoV shadowCircle()
		{
			if(shadowCircle == null)
				shadowCircle = new Shadowcast(true);
			return shadowCircle;
		}
	}
}
