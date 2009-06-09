package jade.gen;

import jade.core.World;
import java.util.HashMap;
import java.util.Map;

public interface Gen
{
	public static final int Wilderness = 0;
	public static final int Cellular = 1;
	public static final int Town = 2;
	
	public void generate(World world, long seed);

	public class GenFactory
	{
		private static Map<Integer, Gen> singletons = new HashMap<Integer, Gen>();
		
		public static Gen get(int algorithm)
		{
			Gen result = singletons.get(algorithm);
			if(result == null)
				result = getNew(algorithm);
			return result;
		}
		
		private static Gen getNew(int algorithm)
		{
			switch(algorithm)
			{
			case Wilderness:
				return new Wilderness();
			case Cellular:
				return new Cellular();
			case Town:
				return new Town();
			default:
				throw new IllegalArgumentException();
			}
		}
	}
}
