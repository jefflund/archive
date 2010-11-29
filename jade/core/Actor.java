package jade.core;

import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import java.util.HashSet;
import java.util.Set;

/**
 * Actor represent something that can preform an action on a World. Actors can
 * also be attached to one another in such a way that the attached Actor goes
 * where its holder goes. The Actor also extends Messenger so it can pass its
 * action messeges to other messengers.
 * 
 * Note Actor is not limited to representing monsters, but can be used for
 * spells, items, timers, traps, etc. Attaching a spell to a player, or a timer
 * to a spell can be very useful.
 */
public abstract class Actor extends Messenger
{
    private ColoredChar face;
    private Coord pos;
    private World world;
    private Actor holder;
    private Set<Actor> holds;
    private boolean expired;

    /**
     * Creates a new Actor with the given face, which is how the Actor should be
     * drawn.
     */
    public Actor(ColoredChar face)
    {
        this.face = face;
        this.pos = new Coord(0, 0);
        holds = new HashSet<Actor>();
    }

    public abstract void act();
    
    /**
     * Returns the face of the Actor
     * @return the face of the Actor
     */
    public ColoredChar look()
    {
        return face;
    }

    /**
     * Returns the x position of the Actor. Note that if the Actor is not bound
     * to a world, this method raises an exception.
     * @return the x position of the Actor
     */
    public int x()
    {
        validateBound();
        return pos.x();
    }

    /**
     * Returns the y position of the Actor. Note that if the Actor is not bound
     * to a world, this method raises an exception.
     * @return the y position of the Actor
     */
    public int y()
    {
        validateBound();
        return pos.y();
    }

    /**
     * Returns a copy of the Coord representing the (x, y) position of the
     * Actor. Since it is a copy, changes made to the result of this call are
     * not reflected by the Actor. If changes are desired, move or setPos should
     * be used instead. Note that if the Actor is not bound to a world, this
     * method raises an exception.
     * @return
     */
    public Coord pos()
    {
        validateBound();
        return new Coord(pos);
    }

    /**
     * Translates the position on the world by (dx, dy). No check is made to see
     * if the new position (x + dx, y + dy) is within the world bounds, or if it
     * violates other conditions like passiblity. However, such additions in
     * overriding methods would not violate the contract of this method, and in
     * fact are encouraged. Note that if the Actor is not bound to a World, an
     * exception is raised.
     * @param dx the amount by which the position will change in x
     * @param dy the amount by which the position will change in y
     */
    public void move(int dx, int dy)
    {
        validateBound();
        if(held())
            throw new IllegalStateException("Actor not free to move");
        world.removeFromGrid(this);
        pos.translate(dx, dy);
        world.addToGrid(this);

    }

    /**
     * Moves the position on the world to (x, y). No check is made to see if the
     * new position (x + dx, y + dy) is within the world bounds, or if it
     * violates other conditions like passiblity. Internally this method uses
     * move, so if these changes are desired, overriding move is the correct way
     * to accomplish this. Note that if the Actor is not bound to a World, an
     * exception is raised.
     * @param x the new x position of the actor
     * @param y the new y positino of the actor
     */
    public final void setPos(int x, int y)
    {
        move(x - pos.x(), y - pos.y());
    }

    public final void setPos(Coord pos)
    {
        setPos(pos.x(), pos.y());
    }

    /**
     * Returns the World the Actor is currently bound to, or null if the Actor
     * is not bound to a World.
     * @return the World the Actor is currently bound to
     */
    public World world()
    {
        return world;
    }

    /**
     * Returns true if the Actor is currently bound to a World
     * @return true if the Actor is currently bound to a World
     */
    public boolean bound()
    {
        return world != null;
    }

    /**
     * Returns true if the Actor is currently bound to the specified World
     * @param world the World which the Actor might be bound to
     * @return true if the Actor is currently bound to the specified World
     */
    public boolean bound(World world)
    {
        return this.world == world;
    }

    void setWorld(World world)
    {
        if(held() && holder.world != world)
            throw new IllegalStateException("Actor held on world already");
        this.world = world;
        for(Actor held : holds)
            held.setWorld(world);

    }

    private void validateBound()
    {
        if(!bound())
            throw new IllegalStateException("Actor not bound to world");
    }

    public void attach(Actor actor)
    {
        if(held())
            throw new IllegalStateException("Actor already held");
        if(bound())
            world.removeActor(this);
        if(actor.bound())
        {
            setWorld(actor.world);
            world.registerActor(this);
        }
        holder = actor;
        actor.holds.add(this);
        pos = actor.pos;
        setHeldPos();
    }

    public void detach()
    {
        if(!held())
            throw new IllegalStateException("Actor not held");
        holder.holds.remove(this);
        pos = new Coord(holder.pos);
        setHeldPos();
        holder = null;
        if(bound())
            setPos(pos);
    }

    private void setHeldPos()
    {
        for(Actor held : holds)
        {
            held.pos = pos;
            held.setHeldPos();
        }
    }
    
    public Actor holder()
    {
        return holder;
    }

    public Iterable<Actor> holds()
    {
        return holds;
    }

    public boolean held()
    {
        return holder != null;
    }

    public boolean held(Actor actor)
    {
        return holder == actor;
    }

    public void expire()
    {
        expired = true;
        for(Actor held : holds)
            held.expire();
    }

    public boolean expired()
    {
        return expired;
    }
}
