package jade.gen;

import jade.core.World;

public interface Gen
{
	public static final int SimpleDungeon = 0;
	public static final int SimpleWilderness = 1;
	public static final int CelluarDungeon = 2;
	
	public void generate(World world, long seed);

	public class GenFactory
	{
		public static Gen get(int algorithm)
		{
			switch(algorithm)
			{
			case SimpleDungeon:
				return new SimpleDunGen();
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
