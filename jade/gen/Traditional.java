package jade.gen;

import jade.core.World;
import jade.util.Coord;
import jade.util.Dice;
import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This implementation of Gen generates traditional looking dungeons. That is to
 * say, it will be a set of rectangular rooms connected by corridors.
 */
public class Traditional implements Gen
{
	private static final int MAX_SIZE = 10;
	private static final int MIN_SIZE = 3;
	private final Dice dice;
	private Set<Room> unconnected;
	/**
	 * A set of rooms that have been connected and dug out. It is made protected
	 * so child classes of Traditional can easily do things like add doors.
	 */
	protected Set<Room> connected;

	protected Traditional()
	{
		dice = new Dice();
	}

	public void generate(World world, long seed)
	{
		floodWall(world);
		int hRooms = (world.width - 1) / MAX_SIZE;
		int vRooms = (world.height - 1) / MAX_SIZE;
		unconnected = new HashSet<Room>();
		connected = new HashSet<Room>();
		for(int x = 0; x < hRooms; x++)
			for(int y = 0; y < vRooms; y++)
				unconnected.add(new Room(x, y));
		Room orig = removeRandom(unconnected);
		connected.add(orig);
		orig.digRoom(world);
		while(!unconnected.isEmpty())
		{
			Room room = removeRandom(unconnected);
			connectRoom(room, world, getRandom(connected));
			room.digRoom(world);
			connected.add(room);
		}
	}

	private Room getRandom(Set<Room> rooms)
	{
		Iterator<Room> iter = rooms.iterator();
		for(int i = 0; i < dice.nextInt(rooms.size()); i++)
			iter.next();
		return iter.next();
	}

	private Room removeRandom(Set<Room> rooms)
	{
		Room result = getRandom(rooms);
		rooms.remove(result);
		return result;
	}

	private void connectRoom(Room room, World world, Room orig)
	{
		int startX = dice.nextInt(room.x1, room.x2);
		int startY = dice.nextInt(room.y1, room.y2);
		int endX = dice.nextInt(orig.x1, orig.x2);
		int endY = dice.nextInt(orig.y1, orig.y2);
		int currX = startX;
		int currY = startY;
		List<Coord> path = new LinkedList<Coord>();
		while(currX != endX && !touching(world, currX, currY))
		{
			currX += startX < endX ? 1 : -1;
			path.add(new Coord(currX, currY));
		}
		while(currY != endY && !touching(world, currX, currY))
		{
			currY += startY < endY ? 1 : -1;
			path.add(new Coord(currX, currY));
		}
		for(Coord coord : path)
			world.tile(coord).setTile('.', Color.white, true);
	}

	private boolean touching(World world, int x, int y)
	{
		return world.passable(x + 1, y) || world.passable(x - 1, y)
		    || world.passable(x, y + 1) || world.passable(x, y - 1);
	}

	private void floodWall(World world)
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
			x *= MAX_SIZE;
			y *= MAX_SIZE;
			x1 = dice.nextInt(x + 1, x + MAX_SIZE - MIN_SIZE - 1);
			x2 = dice.nextInt(x1 + MIN_SIZE - 1, x + MAX_SIZE - 1);
			y1 = dice.nextInt(y + 1, y + MAX_SIZE - MIN_SIZE - 1);
			y2 = dice.nextInt(y1 + MIN_SIZE - 1, y + MAX_SIZE - 1);
		}

		public void digRoom(World world)
		{
			for(int x = x1; x <= x2; x++)
				for(int y = y1; y <= y2; y++)
					world.tile(x, y).setTile('.', Color.white, true);
		}
	}
}
