package jade.gen;

import jade.core.World;
import java.awt.Color;

public class Town extends Wilderness
{
	private static final int MIN_BUILD = 5;
	private static final int MAX_BUILD = 10;
	private static final int MIN_SIZE = 3;
	private static final int MAX_SIZE = 7;
	private static final char BUILDING_TILE = '#';
	private static final Color BUILDING_COLOR = Color.white;

	protected Town()
	{
	}

	public void generate(World world, long seed)
	{
		super.generate(world, seed);
		dice.setSeed(seed);
		buildBuilding(world);
		for(int i = 1; i < dice.nextInt(MIN_BUILD, MAX_BUILD); i++)
			buildBuilding(world);
	}

	private void buildBuilding(World world)
	{
		Site site = findBuildingSite(world);
		for(int x = site.x; x < site.x + site.w; x++)
			for(int y = site.y; y < site.y + site.h; y++)
				world.setTile(x, y, BUILDING_TILE, BUILDING_COLOR, false);
	}

	private Site findBuildingSite(World world)
	{
		Site result;
		do
		{
			int x = dice.nextInt(1, world.width - 2);
			int y = dice.nextInt(1, world.height - 2);
			int w = dice.nextInt(MIN_SIZE, MAX_SIZE);
			int h = dice.nextInt(MIN_SIZE, MAX_SIZE);
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
				    || (world.look(x, y).ch() == BUILDING_TILE))
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
