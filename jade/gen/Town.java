package jade.gen;

import jade.core.World;
import java.awt.Color;

/**
 * This implementation of Gen takes a simple wilderness and adds a few
 * buildings.
 */
public class Town extends Wilderness
{
	protected Town()
	{
	}

	public void generate(World world, long seed)
	{
		super.generate(world, seed);
		dice.setSeed(seed);
		for(int i = 0; i < dice.nextInt(5, 10); i++)
			buildBuilding(world);
	}

	private void buildBuilding(World world)
	{
		Site site = findBuildingSite(world);
		for(int x = site.x; x < site.x + site.w; x++)
			for(int y = site.y; y < site.y + site.h; y++)
				world.setTile(x, y, '#', Color.white, false);
	}

	private Site findBuildingSite(World world)
	{
		Site result;
		do
		{
			int x = dice.nextInt(1, world.width - 2);
			int y = dice.nextInt(1, world.height - 2);
			int w = dice.nextInt(4, 10);
			int h = dice.nextInt(4, 10);
			result = new Site(x, y, w, h);
		}
		while(!isValidSite(world, result));
		return result;
	}

	private boolean isValidSite(World world, Site site)
	{
		boolean result = true;
		for(int x = site.x; x < site.x + site.w; x++)
			for(int y = site.y; y < site.y + site.h; y++)
				if((x < 1 || x > world.width - 2 || y < 1 || y > world.height - 2)
				    || (world.look(x, y).ch() == '#'))
					result = false;
		return result;
	}

	private class Site
	{
		private int x;
		private int y;
		private int w;
		private int h;

		public Site(int x, int y, int w, int h)
		{
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}
}
