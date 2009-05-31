package jade.gen;

import jade.core.World;

public interface Gen
{
	public static final int Wilderness = 0;
	public static final int Cellular = 1;
	
	public void generate(World world, long seed);

	public class GenFactory
	{
		public static Gen get(int algorithm)
		{
			switch(algorithm)
			{
			case Wilderness:
				return new Wilderness();
			case Cellular:
				return new Cellular();
			default:
				throw new IllegalArgumentException();
			}
		}
	}
}
