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
	}

	private void digRooms(World world, List<Room> rooms)
	{
		for(Room room : rooms)
			for(int x = room.x1; x <= room.x2; x++)
				for(int y = room.y1; y <= room.y2; y++)
					world.tile(x, y).setTile('.', Color.white, true);
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
				world.tile(x, y).setTile('#', Color.white, false);
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
