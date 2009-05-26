package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;

public interface FoV
{
	public static final int Simple = 0;
	public static final int SimpleCircle = 1;
	public static final int ShadowCast = 2;
	
	public Collection<Coord> calcFoV(World world, int x, int y, int range);

	public class FoVFactory
	{
		public static FoV get(int algorithm)
		{
			switch(algorithm)
			{
			case Simple:
				return new SimpleFoV();
			case SimpleCircle:
				return new SimpleCircleFoV();
			case ShadowCast:
				return new ShadowCastFoV();
			default:
				throw new IllegalArgumentException();
			}
		}
	}
}
