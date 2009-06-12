package jade.gen;

import jade.core.World;
import jade.util.Dice;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Traditional implements Gen
{
	private static final int MAX_SIZE = 10;
	private static final int MIN_SIZE = 4;
	private Dice dice;

	protected Traditional()
	{
		dice = new Dice();
	}

	public void generate(World world, long seed)
	{
		dice.setSeed(seed);
		floodWalls(world);
		List<Room> rooms = getRooms(world);
		digRooms(world, rooms);
		boolean left = false;
		for(Room start : rooms)
		{
			Room end = rooms.get(dice.nextInt(rooms.size()));
			int startX = dice.nextInt(start.x1, start.x2);
			int startY = dice.nextInt(start.y1, start.y2);
			int endX = dice.nextInt(end.x1, end.x2);
			int endY = dice.nextInt(end.y1, end.y2);
			for(int x = startX; x != endX; x += startX < endX ? 1 : -1)
			{
				if(!world.passable(x, startY))
					left = true;
				if(left && world.passable(x, startY))
					endX = x;
				world.setTile(x, startY, '.', Color.white, true);
			}
			for(int y = startY; y != endY; y += startY < endY ? 1 : -1)
			{
				if(world.passable(endX, y))
					endY = y;
				world.setTile(endX, y, '.', Color.white, true);
			}
		}
	}

	private void digRooms(World world, List<Room> rooms)
	{
		for(Room room : rooms)
			for(int x = room.x1; x <= room.x2; x++)
				for(int y = room.y1; y <= room.y2; y++)
					world.setTile(x, y, '.', Color.white, true);
	}

	private List<Room> getRooms(World world)
	{
		List<Room> rooms = new ArrayList<Room>();
		for(int x = 0; x + MAX_SIZE < world.width - 2; x += MAX_SIZE)
			for(int y = 0; y + MAX_SIZE < world.height - 2; y += MAX_SIZE)
				rooms.add(new Room(x, y));
		return rooms;
	}

	private void floodWalls(World world)
	{
		for(int x = 0; x < world.width; x++)
			for(int y = 0; y < world.height; y++)
				world.setTile(x, y, '#', Color.white, false);
	}

	private class Room
	{
		private int x1;
		private int y1;
		private int x2;
		private int y2;

		public Room(int x, int y)
		{
			x1 = dice.nextInt(x + 1, x + MAX_SIZE - MIN_SIZE - 1);
			x2 = dice.nextInt(x1 + MIN_SIZE - 1, x + MAX_SIZE - 1);
			y1 = dice.nextInt(y + 1, y + MAX_SIZE - MIN_SIZE - 1);
			y2 = dice.nextInt(y1 + MIN_SIZE - 1, y + MAX_SIZE - 1);
		}
	}
}
