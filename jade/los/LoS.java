package jade.los;

import jade.core.World;
import jade.util.Coord;

public interface LoS
{
	public static final int Bresenham = 0;

	public boolean canSee(World world, Coord source, Coord target);

	public class LoSFactory
	{
		public static LoS get(int algorithm)
		{
			switch(algorithm)
			{
			case Bresenham:
				return new BresenhamLoS();
			default:
				throw new IllegalArgumentException();
			}
		}
	}
}
