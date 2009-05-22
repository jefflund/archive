package jade.core;

import jade.util.ColoredChar;
import jade.util.Coord;
import java.awt.Color;
import java.util.HashSet;

public abstract class Actor extends Messenger
{
	private ColoredChar face;
	private Coord pos;
	private World world;
	private Actor holder;
	private HashSet<Actor> holds;
	private boolean expired;

	public Actor(char face, Color color)
	{
		this.face = new ColoredChar(face, color);
		pos = new Coord();
		holds = new HashSet<Actor>();
		expired = false;
	}

	public abstract void act();

	public void setPos(int x, int y)
	{
		assert (bound());
		assert (!held());
		world.removeFromGrid(this);
		pos.move(x, y);
		world.addToGrid(this);
	}

	public void move(int dx, int dy)
	{
		setPos(x() + dx, y() + dy);
	}

	public int x()
	{
		assert (bound());
		assert (!held());
		return pos.x();
	}

	public int y()
	{
		assert (bound());
		assert (!held());
		return pos.y();
	}

	public HashSet<Actor> holds()
	{
		return holds;
	}

	public void attachTo(Actor holder)
	{
		assert (!bound());
		assert (!held());
		setWorld(holder.world);
		this.holder = holder;
		holder.holds.add(this);
	}

	public void detachFrom()
	{
		assert (held());
		assert (bound());
		holder.holds.remove(this);
		setPos(holder.x(), holder.y());
		holder = null;
	}

	public boolean held()
	{
		return holder != null;
	}

	public boolean bound()
	{
		return world != null;
	}

	public boolean boundTo(World world)
	{
		assert (world != null);
		return this.world == world;
	}

	public boolean isExpired()
	{
		return expired;
	}

	public void expire()
	{
		expired = true;
		for(Actor held : holds)
			held.expire();
	}

	public ColoredChar look()
	{
		return face;
	}

	public World world()
	{
		return world;
	}

	void setWorld(World world)
	{
		this.world = world;
		for(Actor held : holds)
			held.setWorld(world);
	}
}