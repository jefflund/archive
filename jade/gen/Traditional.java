package jade.gen;

import jade.core.World;
import jade.util.Dice;
import jade.util.type.Rect;
import java.awt.Color;

public class Traditional implements Gen
{
	private static final int MIN_WIDTH = 3;
	private static final int MIN_HEIGHT = 3;
	private int roomsX;
	private int roomsY;
	private Dice dice;

	public Traditional(int roomsX, int roomsY)
	{
		dice = new Dice();
		setDims(roomsX, roomsY);
	}

	public void setDims(int roomsX, int roomsY)
	{
		this.roomsX = roomsX;
		this.roomsY = roomsY;
	}

	public void generate(World world, long seed)
	{
		dice.setSeed(seed);
		generate(world, seed, new Rect(world.width, world.height));
	}

	public void generate(World world, long seed, Rect rect)
	{
		floodWithWall(world, rect);
		Room[][] rooms = new Room[roomsX][roomsY];
		int xWidth = rect.width() / roomsX;
		int yHeight = rect.height() / roomsY;
		for(int x = 0; x < roomsX; x++)
			for(int y = 0; y < roomsY; y++)
				rooms[x][y] = new Room(x * xWidth, y * yHeight, xWidth, yHeight);
		for(int x = 0; x < roomsX; x++)
			for(int y = 0; y < roomsY; y++)
				rooms[x][y].drawRoom(world);
	}
	
	private void floodWithWall(World world, Rect rect)
	{
		for(int x = rect.xMin(); x < rect.xMax(); x++)
			for(int y = rect.yMin(); y < rect.yMax(); y++)
				world.tile(x, y).setTile('#', Color.white, false);
	}

	private class Room
	{
		public int xMin;
		public int xMax;
		public int yMin;
		public int yMax;

		public Room(int xMin, int yMin, int width, int height)
		{
			this.xMin = xMin;
			this.xMax = xMin + width;
			this.yMin = yMin;
			this.yMax = yMin + height;
			resize();
		}
		
		private void resize()
		{
			xMin = dice.nextInt(xMin + 1, xMax - 1 - MIN_WIDTH);
			xMax = dice.nextInt(xMin + MIN_WIDTH, xMax - 1);
			yMin = dice.nextInt(yMin + 1, yMax - 1 - MIN_HEIGHT);
			yMax = dice.nextInt(yMin + MIN_HEIGHT, yMax - 1);
		}
		
		public void drawRoom(World world)
		{
			for(int x = xMin; x < xMax; x++)
				for(int y = yMin; y < yMax; y++)
					world.tile(x, y).setTile('.', Color.white, true);
		}
	}
}
