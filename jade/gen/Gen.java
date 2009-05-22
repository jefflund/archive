package jade.gen;

import jade.core.World;

public interface Gen
{
	public static final int SimpleWilderness = 0;
	public static final int CelluarDungeon = 1;
	
	public void generate(World world, long seed);

	public class GenFactory
	{
		public static Gen get(int algorithm)
		{
			switch(algorithm)
			{
			case SimpleWilderness:
				return new SimpleWildGen();
			case CelluarDungeon:
				return new CelluarDunGen();
			default:
				throw new IllegalArgumentException();
			}
		}
	}
}
