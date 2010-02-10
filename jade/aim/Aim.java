package jade.aim;

import java.util.ArrayList;
import java.util.List;
import jade.core.Actor;
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
		private static Letter letter;
		private static Select select;
		private static Closest closest;

		/**
		 * User can select any tile within the camera's FoV.
		 */
		public static Aim free()
		{
			if(free == null)
				free = new Free();
			return free;
		}

		/**
		 * User selects target by letter
		 */
		public static Aim letter(Class<? extends Actor> targetType)
		{
			if(letter == null)
				letter = new Letter();
			letter.setTargetType(targetType);
			return letter;
		}

		/**
		 * User selects target using directional keys
		 */
		public static Aim select(Class<? extends Actor> targetType)
		{
			if(select == null)
				select = new Select();
			select.setTargetType(targetType);
			return select;
		}

		/**
		 * Closest target is chosen automatically.
		 */
		public static Aim closest(Class<? extends Actor> targetType)
		{
			if(closest == null)
				closest = new Closest();
			closest.setTargetType(targetType);
			return closest;
		}
	}

	/**
	 * Represents an implementation of Aim that requires a target type
	 */
	abstract class BaseAim implements Aim
	{
		private Class<? extends Actor> targetType;

		protected BaseAim()
		{
			setTargetType(Actor.class);
		}

		protected void setTargetType(Class<? extends Actor> targetType)
		{
			this.targetType = targetType;
		}

		protected List<Coord> getTargets(Camera camera)
		{
			List<Coord> targets = new ArrayList<Coord>();
			for(Coord coord : camera.getFoV())
			{
				if(camera.world().getActorAt(coord, targetType) != null)
					targets.add(coord);
			}
			return targets;
		}
	}
}
