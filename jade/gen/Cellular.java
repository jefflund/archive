package jade.gen;

import jade.core.World;
import jade.util.Coord;
import jade.util.Dice;
import java.awt.Color;
import java.util.Stack;

public class Cellular implements Gen
{
	private static final char UNCONNECTED_TILE = '%';
	private static final char OPEN_TILE = '.';
	private static final char CLOSED_TILE = '#';
	private static final int MIN_COUNT1 = 5;
	private static final int MAX_COUNT2 = 3;
	private static final float TILE_PERCENT = .4f;
	private Dice random;

	public Cellular()
	{
		random = new Dice();
	}

	public void generate(World world, long seed)
	{
		do
		{
			initialize(world);
			char[][] buffer = new char[world.width][world.height];
			for(int i = 0; i < 5; i++)
				apply45(buffer, world);
		}
		while(!connected(world));
	}

	private boolean connected(World world)
	{
		float filled = floodAreaOpen(world, world.getOpenTile(random))
		    / (world.width * world.height);
		if(filled > TILE_PERCENT)
		{
			deleteUnconnected(world);
			return true;
		}
		else
			return false;
	}

	private void deleteUnconnected(World world)
	{
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				if(world.look(x, y).ch() == UNCONNECTED_TILE)
					world.tile(x, y).setTile(CLOSED_TILE, Color.white, false);
	}

	private float floodAreaOpen(World world, Coord coord)
	{
		Stack<Coord> stack = new Stack<Coord>();
		stack.push(coord);
		int count = 1;
		while(!stack.isEmpty())
		{
			Coord curr = stack.pop();
			if(world.look(curr.x(), curr.y()).ch() == UNCONNECTED_TILE)
			{
				count++;
				world.tile(curr.x(), curr.y()).setTile(OPEN_TILE, Color.white, true);
				stack.push(new Coord(curr.x() + 1, curr.y()));
				stack.push(new Coord(curr.x() - 1, curr.y()));
				stack.push(new Coord(curr.x(), curr.y() + 1));
				stack.push(new Coord(curr.x(), curr.y() - 1));
			}
		}
		return count;
	}

	private void initialize(World world)
	{
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
			{
				if(x == 0 || x == world.width - 1 || y == 0 || y == world.height - 1)
					setWallTile(world, x, y);
				else if(random.nextDouble() < TILE_PERCENT)
					setWallTile(world, x, y);
				else
					setOpenTile(world, x, y);
			}
	}

	private void apply45(char[][] buffer, World world)
	{
		for(int x = 1; x < world.width - 1; x++)
			for(int y = 1; y < world.height - 1; y++)
			{
				if(wallcount(world, x, y, 1) >= MIN_COUNT1
				    || wallcount(world, x, y, 2) <= MAX_COUNT2)
					buffer[x][y] = CLOSED_TILE;
				else
					buffer[x][y] = OPEN_TILE;
			}
		for(int x = 1; x < world.width - 1; x++)
			for(int y = 1; y < world.height - 1; y++)
				if(buffer[x][y] == OPEN_TILE)
					setOpenTile(world, x, y);
				else
					setWallTile(world, x, y);
	}

	private int wallcount(World world, int x, int y, int range)
	{
		int count = 0;
		for(int dx = x - range; dx <= x + range; dx++)
			for(int dy = y - range; dy <= y + range; dy++)
			{
				if(outOfBounds(world, dx, dy))
					continue;
				if(!world.passable(dx, dy))
					count++;
			}
		return count;
	}

	private boolean outOfBounds(World world, int x, int y)
	{
		return x < 0 || x >= world.width || y < 0 || y >= world.height;
	}

	private void setOpenTile(World world, int x, int y)
	{
		world.tile(x, y).setTile(UNCONNECTED_TILE, Color.white, true);
	}

	private void setWallTile(World world, int x, int y)
	{
		world.tile(x, y).setTile(CLOSED_TILE, Color.white, false);
	}
}
