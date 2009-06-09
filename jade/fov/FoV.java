package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface FoV
{
	public static final int SquareRay = 0;
	public static final int CircularRay = 1;
	public static final int ShadowCast = 2;

	public Collection<Coord> calcFoV(World world, int x, int y, int range);

	public class FoVFactory
	{
		private static final Map<Integer, FoV> singletons = new HashMap<Integer, FoV>();

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
