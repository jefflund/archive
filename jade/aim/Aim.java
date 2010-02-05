package jade.aim;

import jade.core.Console;
import jade.core.Console.Camera;
import jade.util.Coord;

/**
 * This interface represents a user targeting system
 */
public interface Aim
{
	/**
	 * Returns the user selected taraget.
	 */
	public Coord getAim(Console console, Camera camera);
	
	public class AimFactory
	{
		private static Aim free;
		
		/**
		 * User can select any tile within the camera's FoV.
		 */
		public static Aim free()
		{
			if(free == null)
				free = new Free();
			return free;
		}
	}
}
