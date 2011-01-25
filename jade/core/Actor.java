package jade.core;

import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Direction;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents anything that performs any action in a Jade World.
 */
public abstract class Actor extends Messenger
{
    private ColoredChar face;
    private World world;
    private Coord pos;
    private boolean expired;
    private Actor holder;
    private Set<Actor> holds;

    /**
     * Constructs a new, unbound Actor with the given face.
     * @param face the face of the new Actor
     */
    public Actor(ColoredChar face)
    {
        this.face = face;
        world = null;
        pos = new Coord();
        expired = false;
        holder = null;
        holds = new HashSet<Actor>();
    }

    /**
     * Carries out the action of the Actor. Often is usually called by the World
     * in its tick method.
     */
    public abstract void act();

    /**
     * Returns the face of the Actor
     * @return the face of the Actor
     */
    public ColoredChar face()
    {
        return face;
    }

    /**
     * Changes the face of this Actor to a new one. Note that a null face is
     * allowed, and implies that the Actor should never be drawn on screen, or
     * have its face returned in World look methods.
     * @param face the new face of the Actor
     */
    public void setFace(ColoredChar face)
    {
        this.face = face;
    }

    /**
     * Returns the world on which the Actor resides, or null if the actor is not
     * bound to a World.
     * @return the world on which the Actor currently resides
     */
    public World world()
    {
        return world;
    }

    /**
     * Returns true if the Actor is bound to a World.
     * @return true if the Actor is bound to a World
     */
    public boolean bound()
    {
        return world != null;
    }

    /**
     * Returns true if the Actor is bound to the given World.
     * @param world the world being tested
     * @return true if the Actor is bound to the given World
     */
    public boolean bound(World world)
    {
        return this.world == world;
    }

    /**
     * Returns the x value of the location of the Actor on the World. An error
     * is raised if the Actor is not bound to a World.
     * @return the x value of the location of the Actor on the World
     */
    public int x()
    {
        assertBound();
        return pos.x();
    }

    /**
     * Returns the y value of the location of the Actor on the World. An error
     * is raised if the Actor is not bound to a World.
     * @return the y value of the location of the Actor on the World
     */
    public int y()
    {
        assertBound();
        return pos.y();
    }

    /**
     * Returns a copy of the location of the Actor on the World. An error is
     * raised if the Actor is not bound to a World. Note that the result is a
     * copy, and changes to this Coord will not be reflected in the state of the
     * Actor.
     * @return the location of the Actor on the World
     */
    public Coord pos()
    {
        assertBound();
        return new Coord(pos);
    }

    /**
     * Sets the position of the Actor on the World to (x, y). An error is raised
     * if the Actor is not bound to a World or if it is held by another Actor.
     * @param x the x value of the new location of the Actor on the World
     * @param y the y value of the new location of the Actor on the World
     */
    public void setPos(int x, int y)
    {
        assertBound();
        assertNotHeld();
        world.unoccupyGrid(this);
        pos.move(x, y);
        world.occupyGrid(this);
    }

    /**
     * Sets the position of the Actor on the World to the value of the given
     * Coord. An error is raised if the Actor is not bound to a World or if it
     * is held by another Actor.
     * @param coord the new position of the Actor on the World
     */
    public final void setPos(Coord coord)
    {
        setPos(coord.x(), coord.y());
    }

    /**
     * Sets the position of the Actor on the World to (x + dx, y + dy). An error
     * is raised if the Actor is not bound to a World or if it is held by
     * another Actor.
     * @param dx the change x
     * @param dy the change y
     */
    public final void move(int dx, int dy)
    {
        setPos(x() + dx, y() + dy);
    }

    /**
     * Sets the position of the Actor on the World to (x + dx, y + dy) where
     * delta defines (dx, dy). An error is raised if the Actor is not bound to a
     * World or if it is held by another Actor.
     * @param delta the change in position
     */
    public final void move(Coord delta)
    {
        move(delta.x(), delta.y());
    }

    /**
     * Sets the position of the Actor to be one step in the given direction. An
     * error is raised if the Actor is not bound to a World or if it is held by
     * another Actor.
     * @param dir the direction of change
     */
    public final void move(Direction dir)
    {
        move(dir.dx(), dir.dy());
    }

    /**
     * Expires this Actor and every Actor held by this one, marking them for
     * removal from its World.
     */
    public void expire()
    {
        expired = true;
        for(Actor held : holds)
            held.expire();
    }

    /**
     * Returns true if the Actor is expired, which means it is marked for
     * removal from its World.
     * @return true if the Actor is expired
     */
    public boolean expired()
    {
        return expired;
    }

    /**
     * Attaches this Actor to the given Actor. The position of this Actor then
     * becomes that of the new holder. This Actor is removed from its current
     * World and bount to the World of the new holder, although it will not be
     * found on the grid of the World. Any Actor held by this Actor will also be
     * moved with the new holder. An error is raised if the Actor is already
     * held.
     * @param actor the new holder
     */
    public void attach(Actor actor)
    {
        assertNotHeld();
        if(bound())
            world.removeActor(this);
        if(actor.bound())
        {
            setWorld(actor.world);
            world.registerActor(this);
        }
        holder = actor;
        actor.holds.add(this);
        pos = holder.pos;// holder position IS our own
        setHeldPos();
    }

    /**
     * Releases this Actor from its holder. The position of this Actor becomes
     * independent of its former holder. It is also recorded as occupying the
     * World grid. An error is raised by this method if the Actor was not held.
     */
    public void detach()
    {
        assertHeld();
        holder.holds.remove(this);
        pos = new Coord(holder.pos);
        setHeldPos();
        holder = null;
        if(bound())
            setPos(pos);
    }

    /**
     * Returns true if the Actor is currently held by another.
     * @return true if the Actor is currently held by another
     */
    public boolean held()
    {
        return holder != null;
    }

    /**
     * Returns true if the given Actor is the holder of this Actor
     * @param actor the Actor which is being queried as the holder
     * @return true if the given Actor is the holder of this Actor
     */
    public boolean held(Actor actor)
    {
        return holder == actor;
    }

    /**
     * Returns the current holder of this Actor, or null if there is none
     * @return the current holder of this Actor
     */
    public Actor holder()
    {
        return holder;
    }

    /**
     * Returns all Actors currently held by this one
     * @return all Actors currently held by this one
     */
    public Iterable<Actor> holds()
    {
        return holds;
    }

    void setWorld(World world)
    {
        this.world = world;
    }

    private void setHeldPos()
    {
        for(Actor held : holds)
        {
            held.pos = pos;
            held.setHeldPos();
        }
    }

    private void assertBound()
    {
        if(!bound())
            throw new IllegalStateException("Actor not bound!");
    }

    private void assertNotHeld()
    {
        if(held())
            throw new IllegalStateException("Actor held!");
    }

    private void assertHeld()
    {
        if(!held())
            throw new IllegalStateException("Actor not held!");
    }
}
