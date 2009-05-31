package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;

public interface FoV
{
	public static final int SquareRay = 0;
	public static final int CircularRay = 1;
	public static final int ShadowCast = 2;
	
	public Collection<Coord> calcFoV(World world, int x, int y, int range);

	public class FoVFactory
	{
		public static FoV get(int algorithm)
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
