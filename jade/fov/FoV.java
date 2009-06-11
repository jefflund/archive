package jade.fov;

import jade.core.World;
import jade.util.Coord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This interface represents field of vision calculation on a jade World.
 */
public interface FoV
{
	/**
	 * Uses raycasting in a square. Useful for player centric cameras.
	 */
	public static final int SquareRay = 0;
	/**
	 * Uses raycasting but limits its range to a circular radius.
	 */
	public static final int CircularRay = 1;
	/**
	 * Uses spiral path shadowcasting. Useful for traditional roguelike maps.
	 */
	public static final int ShadowCast = 2;

	/**
	 * Calculates field of vision from a given tile on world. The passable method
	 * is used to determine if light can continue through a square. The field of
	 * vision is limited by the range.
	 * 
	 * @param world the world on which the field will be calculated
	 * @param x the x-coordinate of the origion of the field of vision
	 * @param y the y-coordinate of the origion of the field of vision
	 * @param range the maximum range for the field of vision
	 * @return a Collection<Coord> of tiles in the field of vision
	 */
	public Collection<Coord> calcFoV(World world, int x, int y, int range);

	/**
	 * Used for retrieving varius singleton instances of FoV implementing classes.
	 */
	public class Factory
	{
		private static final Map<Integer, FoV> singletons = new HashMap<Integer, FoV>();

		/**
		 * Returns a instance implementing FoV based on the specified algorithm.
		 * 
		 * @param algorithm the algorithm to be implemented. Static integer
		 * constants are provided in FoV
		 * @return a singleton instance of an object implementing FoV with specified
		 * algorithm
		 */
		public static FoV get(int algorithm)
		{
			FoV result = singletons.get(algorithm);
			if(result == null)
				result = getNew(algorithm);
			return result;
		}

		private static FoV getNew(int algorithm)
		{
			switch(algorithm)
			{
			case SquareRay:
				return new SquareRay();
			case CircularRay:
				return new CircularRay();
			case ShadowCast:
				return new ShadowCast();
			default:
				throw new IllegalArgumentException();
			}
		}
	}
}
