package jade.fov;

import jade.core.World;
import jade.util.Coord;
import java.util.Collection;

/**
 * This implementation of FoV uses a recursive shadowcasting algorithm. This
 * algorithm has the advantage of raycasting in that it does need to visit near
 * tiles multiple times.
 */
public class Shadowcast implements FoV
{
	protected Shadowcast()
	{
		throw new UnsupportedOperationException("Shadowcast not implemented yet");
	}

	public Collection<Coord> calcFoV(World world, int x, int y, int range)
	{
		
//		 Scan(depth, startslope, endslope)
//		 
//	   init y
//	   init x
//
//	   while current_slope has not reached endslope do
//	     if (x,y) within visual range then
//	       if (x,y) blocked and prior not blocked then
//	         Scan(depth + 1, startslope, new_endslope)
//	       if (x,y) not blocked and prior blocked then
//	         new_startslope
//	       set (x,y) visible
//	     progress (x,y)
//
//	   regress (x,y)
//
//	   if depth < visual range and (x,y) not blocked
//	     Scan(depth + 1, startslope, endslope)
//	 end

		return null;
	}
}
