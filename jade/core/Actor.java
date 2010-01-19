package jade.core;

import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Direction;
import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a basic Actor in a Jade World.
 */
public abstract class Actor extends Messenger implements Serializable
{
	private ColoredChar face;
	private Coord pos;
	private World world;
	private Actor holder;
	private HashSet<Actor> holds;
	private boolean expired;

	/**
	 * Creates an Actor
	 */
	public Actor(char face, Color color)
	{
		this(new ColoredChar(face, color));
	}

	/**
	 * Creates an Actor
	 */
	public Actor(ColoredChar face)
	{
		this.face = face;
		pos = new Coord();
		holds = new HashSet<Actor>();
		expired = false;
	}

	/**
	 * Default behavior for the actor. Called by World.tick()
	 */
	public abstract void act();

	/**
	 * Sets the position of the Actor
	 */
	public final void setPos(int x, int y)
	{
		assert (bound());
		assert (!held());
		world.removeFromGrid(this);
		pos.move(x, y);
		world.addToGrid(this);
	}

	/**
	 * Sets the position of the Actor
	 */
	public final void setPos(Coord pos)
	{
		setPos(pos.x(), pos.y());
	}

	/**
	 * Moves the Actor by a specified amount
	 */
	public void move(int dx, int dy)
	{
		setPos(x() + dx, y() + dy);
	}

	/**
	 * Moves the Actor one tile in a specified direction
	 */
	public final void move(Direction dir)
	{
		move(dir.dx, dir.dy);
	}

	/**
	 * Gets the x location of the Actor
	 */
	public final int x()
	{
		assert (bound());
		return pos.x();
	}

	/**
	 * Gets the x location of the Actor
	 */
	public final int y()
	{
		assert (bound());
		return pos.y();
	}

	/**
	 * Returns the position of the Actor. Changes to this Coord don't affect the
	 * Actor.
	 */
	public final Coord pos()
	{
		return new Coord(pos);
	}

	/**
	 * Returns the Actors held by this Actor. Changes to this collection don't
	 * affect the Actor.
	 */
	public final Collection<Actor> holds()
	{
		return new HashSet<Actor>(holds);
	}

	/**
	 * Attaches this Actor to another.
	 */
	public final void attachTo(Actor actor)
	{
		assert (!held());
		if(bound())
			world().removeActor(this);
		if(actor.bound())
		{
			setWorld(actor.world);
			world.registerActor(this);
		}
		this.holder = actor;
		actor.holds.add(this);
		pos = actor.pos;
		getHolderPos();
	}

	private void getHolderPos()
	{
		for(Actor held : holds)
		{
			held.pos = pos;
			held.getHolderPos();
		}
	}

	/**
	 * Detaches this Actor from its holder
	 */
	public final void detachFrom()
	{
		assert (held());
		holder.holds.remove(this);
		Coord holderPos = new Coord(holder.pos);
		holder = null;
		if(bound())
			setPos(holderPos.x(), holderPos.y());
	}

	/**
	 * Returns true if this Actor is held.
	 */
	public final boolean held()
	{
		return holder != null;
	}

	/**
	 * Returns the Actor that holds this Actor.
	 */
	public final Actor holder()
	{
		return holder;
	}

	/**
	 * True if the Actor is bound to a World
	 */
	public final boolean bound()
	{
		return world != null;
	}

	/**
	 * Returns the World this Actor is bound to.
	 */
	public final boolean boundTo(World world)
	{
		assert (world != null);
		return this.world == world;
	}

	/**
	 * Returns true if this Actor is bound
	 */
	public final boolean isExpired()
	{
		return expired;
	}

	/**
	 * Expires this Actor and all those it holds. This is a mechanism for marking
	 * actors for removal.
	 */
	public void expire()
	{
		expired = true;
		for(Actor held : holds)
			held.expire();
	}

	/**
	 * Returns the face of this Actor
	 */
	public final ColoredChar look()
	{
		return face;
	}

	/**
	 * Changes the face of this Actor
	 */
	public final void setFace(ColoredChar face)
	{
		this.face = face;
	}

	/**
	 * Returns the World this Actor is bound to.
	 */
	public World world()
	{
		return world;
	}

	final void setWorld(World world)
	{
		assert (!held() || holder.world == world);
		this.world = world;
		for(final Actor held : holds)
			held.setWorld(world);
	}

	@Override
	public String toString()
	{
		return face.toString();
	}
}