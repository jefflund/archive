package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;

/**
 * The Camera interface allows the Jade Console to draw everything that the
 * camera sees. With the exception of getFov, the Actor class has all these
 * methods, so it would make sense to have an Actor implement the camera
 * interface and allow the console to draw what that actor sees.
 */
public interface Camera
{
	/**
	 * Returns a collection of Coord with all the coordinates that this camera can
	 * currently see.
	 * @return a collection of Coord with all the coordinates that this camera can
	 * currently see
	 */
	public Collection<Coord> getFoV();
	
	/**
	 * Returns the world the camera is currently viewing.
	 * @return the world the camera is currently viewing
	 */
	public World world();
	
	/**
	 * Returns the x position of the camera on the world.
	 * @return the x position of the camera on the world
	 */
	public int x();
	
	/**
	 * Returns the y position of the camera on the world.
	 * @return the y position of the camera on the world
	 */
	public int y();
}
