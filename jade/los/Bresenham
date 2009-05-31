package jade.los;

import jade.core.World;
import jade.util.Coord;

public class BresenhamLoS implements LoS
{
	public boolean canSee(World world, Coord source, Coord target)
	{
		int x1 = source.x();
		int y1 = source.y();
		int x2 = target.x();
		int y2 = target.y();
		boolean steep = Math.abs(y2 - y1) > Math.abs(x2 - x1);
		if(steep)
		{
			int temp = x1;
			x1 = y1;
			y1 = temp;

			temp = x2;
			x2 = y2;
			y2 = temp;
		}
		if(x1 > x2)
		{
			int temp = x1;
			x1 = x2;
			x2 = temp;

			temp = y1;
			y1 = y2;
			y2 = temp;
		}
		int dx = x2 - x1;
		int dy = Math.abs(y2 - y1);
		int err = dx / 2;
		int y = y1;
		int ystep = y1 < y2 ? 1 : -1;
		for(int x = x1; x <= x2; x++)
		{
			if(steep ? !world.passable(y, x) : !world.passable(x, y))
				return false;
			err = err - dy;
			if(err < 0)
			{
				y = y + ystep;
				err = err + dx;
			}
		}
		return true;
	}
}
