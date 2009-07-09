package jade.core;

import jade.util.ColoredChar;
import jade.util.Coord;
import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * This class represents any that could be placed on a jade world. Using the act
 * method, the actor can perform its specified action. Actors can also be
 * attached to other actors, so they move as one. Actors can also be expired,
 * meaning that they should be ignored by all actors and discarded as soon as
 * possible.
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
	 * Constructs an actor with the given appearance.
	 * 
	 * @param face the actor's ascii representation
	 * @param color the actor's color
	 */
	public Actor(char face, Color color)
	{
		this(new ColoredChar(face, color));
	}

	/**
	 * Constructs an actor with the given appearance.
	 * 
	 * @param face the actor's ascii representation
	 */
	public Actor(ColoredChar face)
	{
		this.face = face;
		pos = new Coord();
		holds = new HashSet<Actor>();
		expired = false;
	}

	/**
	 * Specifies the actors behavior. This method is intended to be called by the
	 * world's tick method call the act method on all actors who should have a
	 * turn.
	 */
	public abstract void act();

	/**
	 * Moves the actor to the specified location on the actor's world. The actor
	 * must belong to a world, and should not be attached to another actor.
	 * 
	 * @param x the new x location
	 * @param y the new y location
	 */
	public void setPos(int x, int y)
	{
		assert (bound());
		assert (!held());
		world.removeFromGrid(this);
		pos.move(x, y);
		world.addToGrid(this);
	}

	/**
	 * Moves uses setPos to move the actor by the specified amount. The actor must
	 * belong to a world, and should not be attached to another actor.
	 * 
	 * @param dx the amount to move horizontally
	 * @param dy the amount to move vertically
	 */
	public void move(int dx, int dy)
	{
		setPos(x() + dx, y() + dy);
	}

	/**
	 * Returns the x-coordinate of the actor. The actor must belong to a world. If
	 * it is attached to another actor, this will be the x-coordinate of the
	 * actor's holder.
	 * 
	 * @return the x-coordinate of the actor
	 */
	public int x()
	{
		assert (bound());
		return pos.x();
	}

	/**
	 * Returns the y-coordinate of the actor. The actor must belong to a world. If
	 * it is attached to another actor, this will be the y-coordinate of the
	 * actor's holder.
	 * 
	 * @return the y-coordinate of the actor
	 */
	public int y()
	{
		assert (bound());
		return pos.y();
	}

	/**
	 * Returns a collection of the actors this actor holds. The collection
	 * returned is not the actual backing collection, so any changes made to this
	 * collection will not be reflected in the actor.
	 * 
	 * @return a collection of the actors this actor holds.
	 */
	public Collection<Actor> holds()
	{
		return new HashSet<Actor>(holds);
	}

	/**
	 * Attaches this actor to another. The actor should not be held by another. If
	 * the actor belongs to a world, it will be removed and placed on the new
	 * holder's world. This actors position will then be the holder's position.
	 * 
	 * @param holder
	 */
	public void attachTo(Actor holder)
	{
		assert (!held());
		if(bound())
			world().removeActor(this);
		setWorld(holder.world);
		this.holder = holder;
		holder.holds.add(this);
		pos = holder.pos;
	}

	/**
	 * Detaches this actor from its holder. The actor must be held. The actor will
	 * be placed on the holder's world at the holder's position (assuming the
	 * holder belongs to a world).
	 */
	public void detachFrom()
	{
		assert (held());
		holder.holds.remove(this);
		Coord pos = new Coord(holder.pos);
		holder = null;
		if(bound())
			setPos(pos.x(), pos.y());
	}

	/**
	 * Returns true if the actor is held.
	 * 
	 * @return true if the actor is held.
	 */
	public boolean held()
	{
		return holder != null;
	}

	/**
	 * Returns the actor if the actor is held, or null if it is not.
	 * 
	 * @return the actor if the actor is held, or null if it is not.
	 */
	public Actor holder()
	{
		return holder;
	}

	/**
	 * Returns true if the actor belongs to a world.
	 * 
	 * @return true if the actor belongs to a world.
	 */
	public boolean bound()
	{
		return world != null;
	}

	/**
	 * Checks whether the actor belongs to the given world.
	 * 
	 * @param world the world to be checked
	 * @return true if the actor belongs to the world, false otherwise
	 */
	public boolean boundTo(World world)
	{
		assert (world != null);
		return this.world == world;
	}

	/**
	 * Returns true if the actor is expired.
	 * 
	 * @return true if the actor is expired.
	 */
	public boolean isExpired()
	{
		return expired;
	}

	/**
	 * Expires this actor. This will also expire all actors attached to this
	 * actor.
	 */
	public void expire()
	{
		expired = true;
		for(Actor held : holds)
			held.expire();
	}

	/**
	 * Returns the appearance of this actor.
	 * 
	 * @return the appearance of this actor.
	 */
	public ColoredChar look()
	{
		return face;
	}

	/**
	 * Returns the world the actor currently belongs too, or null if it does not
	 * belong to a world.
	 * 
	 * @return the world the actor currently belongs too, or null if it does not
	 * belong to a world.
	 */
	public World world()
	{
		return world;
	}

	void setWorld(World world)
	{
		assert (!held() || holder.world == world);
		this.world = world;
		for(Actor held : holds)
			held.setWorld(world);
	}

	/**
	 * Returns the character representation of this actor.
	 */
	@Override
	public String toString()
	{
		return face.toString();
	}
}