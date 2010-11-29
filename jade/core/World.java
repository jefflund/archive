package jade.core;

import jade.util.Dice;
import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The World is the home of Actors in the Jade system. It stores the current
 * state of each actor, and the map in which the actors reside. The world also
 * defines the order in which actors act, and the way any given tile is drawn.
 */
public class World extends Messenger
{
    private final int width;
    private final int height;
    private final Tile[][] grid;
    private Set<Actor> actors;
    private List<Class<? extends Actor>> drawOrder;
    private List<Class<? extends Actor>> actOrder;

    /**
     * Constructs a new empty World of size width x height.
     * @param width the width of the new World
     * @param height the height of the new World
     */
    public World(int width, int height)
    {
        this.width = width;
        this.height = height;
        grid = new Tile[width][height];
        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++)
                grid[x][y] = new Tile();
        actors = new HashSet<Actor>();
        setDrawOrder(new LinkedList<Class<? extends Actor>>());
        setActOrder(new LinkedList<Class<? extends Actor>>());
    }

    /**
     * Getter for the width of the World
     * @return the width of the World
     */
    public int width()
    {
        return width;
    }

    /**
     * Getter for the height of the World
     * @return the height of the World
     */
    public int height()
    {
        return height;
    }
    
    public void setActOrder(List<Class<? extends Actor>> actOrder)
    {
        this.actOrder = actOrder;
    }
    
    public List<Class<? extends Actor>> getActOrder()
    {
        return actOrder;
    }
    
    public void appendActOrder(Class<? extends Actor> cls)
    {
        actOrder.add(cls);
    }
    
    
    public void tick()
    {
        for(Class<? extends Actor> cls : actOrder)
            for(Actor actor : getActors(cls))
                actor.act();
        removeExpired();
    }
    /**
     * Returns a list of all visible faces at a given location, ordered by draw
     * priority, which is defined in drawOrder. The last character in this list
     * is always the face of the tile at (x, y).
     * @param x the x location of the tile
     * @param y the y locatio of the tile
     * @return a list of all visible faces at (x, y)
     */
    public List<ColoredChar> lookAll(int x, int y)
    {
        List<ColoredChar> look = new LinkedList<ColoredChar>();
        for(Class<? extends Actor> cls : drawOrder)
            for(Actor actor : getActorsAt(x, y, cls))
                look.add(actor.look());
        look.add(tile(x, y));
        return look;
    }

    /**
     * Returns a list of all visible faces at a given location, ordered by draw
     * priority, which is defined in drawOrder. The last character in this list
     * is always the face of the tile at (x, y).
     * @param x the x location of the tile
     * @param y the y locatio of the tile
     * @return a list of all visible faces at (x, y)
     */
    public final List<ColoredChar> lookAll(Coord coord)
    {
        return lookAll(coord.x(), coord.y());
    }

    /**
     * Returns what is to be drawn for the tile at the given coordinate. This
     * depends on the draw order and the actor occupants of at the location. If
     * the tile is occupied, and the one or more actors are to be draw as given
     * by the draw order, the face returned will be that of the actor with the
     * highest draw priority. Otherwise, the face returned will be that of the
     * tile itself.
     * @param x the x value of the coordinate
     * @param y the y value of the coordinate
     * @return the face to be draw at the given coordinate
     */
    public ColoredChar look(int x, int y)
    {
        return lookAll(x, y).get(0);
    }

    /**
     * Returns what is to be drawn for the tile at the given coordinate. This
     * depends on the draw order and the actor occupants of at the location. If
     * the tile is occupied, and the one or more actors are to be draw as given
     * by the draw order, the face returned will be that of the actor with the
     * highest draw priority. Otherwise, the face returned will be that of the
     * tile itself.
     * @param coord the coordinate of the tile being considered
     * @return the face to be draw at the given coordinate
     */
    public final ColoredChar look(Coord coord)
    {
        return look(coord.x(), coord.y());
    }

    /**
     * Sets the draw order of the world. By default this list is empty so that
     * look will only return the face of the tile. Setting the draw order
     * defines the priority for drawing.
     * @param drawOrder the new draw order
     */
    public void setDrawOrder(List<Class<? extends Actor>> drawOrder)
    {
        this.drawOrder = drawOrder;
    }

    /**
     * Returns the draw order for the world. This list is the exact instance
     * used by the world so any changes to the result of this getter will be
     * reflected in the draw order used by lookAll.
     * @return the draw order for the world
     */
    public List<Class<? extends Actor>> getDrawOrder()
    {
        return drawOrder;
    }

    /**
     * Addes the given type to the end of the draw order for the World.
     * Equivalent to getDrawOrder().add(cls).
     * @param cls the type to be appended to the draw order
     */
    public void appendDrawOrder(Class<? extends Actor> cls)
    {
        drawOrder.add(cls);
    }

    /**
     * Returns the face of the tile at the given coordinate.
     * @param x the x value of the location of the tile considered
     * @param y the y value of the location of the tile considered
     * @return the face of the tile at the given coordinate
     */
    public ColoredChar tile(int x, int y)
    {
        return grid[x][y].face;
    }

    /**
     * Returns the face of the tile at the given coordinate.
     * @param coord the cordinatie location of the tile considered
     * @return the face of the tile at the given coordinate
     */
    public final ColoredChar tile(Coord coord)
    {
        return tile(coord.x(), coord.y());
    }

    /**
     * Returns true if the tile at the given coordinate is passable. This only
     * reflects the tile and not the occupants of the tile.
     * @param x the x value of the coordinate location of the tile
     * @param y the y value of the coordinate location of the tile
     * @return true if the tile at the given coordinate is passable
     */
    public boolean passable(int x, int y)
    {
        return grid[x][y].passable;
    }

    /**
     * Returns true if the tile at the given coordinate is passable. This only
     * reflects the tile and not the occupants of the tile.
     * @param coord the coordinate location of the tile
     * @return true if the tile at the given coordinate is passable
     */
    public final boolean passable(Coord coord)
    {
        return passable(coord.x(), coord.y());
    }

    /**
     * Returns the coordinate of a random open tile. The tile is found by
     * randomly choosing x,y coordinates and seeing if that location is
     * passable. After 100 attempts, if no open location is found, then an
     * iterative approach looks for the first open tile, which will fail only if
     * there is no open tile on the map, in which case null will be returned.
     * @param dice the rng which will generate random x,y values
     * @return a random open tile
     */
    public final Coord getOpenTile(Dice dice)
    {
        return getOpenTile(dice, 0, 0, width - 1, height - 1);
    }

    /**
     * Returns the coordinate of a random open tile with in the given bounds.
     * The tile is found by randomly choosing x,y coordinates in the given bound
     * and seeing if that location is passable. Note that no check is made
     * whether or not this (x,y) value is even on the map, so be sure to passing
     * in valid bounds or errors may result. After 100 attempts, if no open
     * location is found, then an iterative approach looks for the first open
     * tile, which will fail only if there is no open tile on the map, in which
     * case null will be returned.
     * @param dice the rng which will generate random x,y values
     * @param x1 the mimimum value of x
     * @param y1 the mimimum value of y
     * @param x2 the maximum value of x
     * @param y2 the maximum value of y
     * @return a random open tile
     */
    public Coord getOpenTile(Dice dice, int x1, int y1, int x2, int y2)
    {
        int count = 0;
        while(count < 100)
        {
            int x = dice.nextInt(x1, x2);
            int y = dice.nextInt(y1, y2);
            if(passable(x, y))
                return new Coord(x, y);
            count++;
        }
        for(int x = x1; x < x2; x++)
            for(int y = y1; y < y2; y++)
                if(passable(x, y))
                    return new Coord(x, y);
        return null;
    }

    /**
     * Returns the coordinate of a random open tile. The tile is found by
     * randomly choosing x,y coordinates using the default instance of Dice and
     * seeing if that location is passable. After 100 attempts, if no open
     * location is found, then an iterative approach looks for the first open
     * tile, which will fail only if there is no open tile on the map, in which
     * case null will be returned.
     * @return a random open tile
     */
    public final Coord getOpenTile()
    {
        return getOpenTile(Dice.global);
    }

    /**
     * Returns the coordinate of a random open tile with in the given bounds.
     * The tile is found by randomly choosing x,y coordinates in the given bound
     * and seeing if that location is passable using the global instance of
     * Dice. Note that no check is made whether or not this (x,y) value is even
     * on the map, so be sure to passing in valid bounds or errors may result.
     * After 100 attempts, if no open location is found, then an iterative
     * approach looks for the first open tile, which will fail only if there is
     * no open tile on the map, in which case null will be returned.
     * @param x1 the mimimum value of x
     * @param y1 the mimimum value of y
     * @param x2 the maximum value of x
     * @param y2 the maximum value of y
     * @return a random open tile
     */
    public final Coord getOpenTile(int x1, int y1, int x2, int y2)
    {
        return getOpenTile(Dice.global, x1, y1, x2, y2);
    }

    /**
     * Sets the attributes of the tile at the given location.
     * @param face the new face of the tile
     * @param passable the new passibility of the tile
     * @param x the x value of the coordinate of the modified tile
     * @param y the y value of the coordinate of the modified tile
     */
    public void setTile(ColoredChar face, boolean passable, int x, int y)
    {
        grid[x][y].set(face, passable);
    }

    /**
     * Sets the attributes of the tile at the given location.
     * @param face the new face of the tile
     * @param passable the new passibility of the tile
     * @param coord the coordinate of the modified tile
     */
    public final void setTile(ColoredChar face, boolean passable, Coord coord)
    {
        setTile(face, passable, coord.x(), coord.y());
    }

    private class Tile
    {
        public ColoredChar face;
        public boolean passable;
        public Set<Actor> actors;

        public Tile()
        {
            set(new ColoredChar('.', Color.white), true);
            actors = new HashSet<Actor>();
        }

        public void set(ColoredChar face, boolean passable)
        {
            this.face = face;
            this.passable = passable;
        }
    }

    /**
     * Gets a single actor of a specified type that is occuping the cell at the
     * location (x, y). If no actor of the type is present, null is returned. If
     * there are multiple actors of the given type are present, one of them will
     * be returned arbitrarily.
     * @param <T> the type, extending Actor, of the actor to be returned
     * @param x the x location of the actor returned
     * @param y the y location of the actor returned
     * @param cls the class that will define T
     * @return an actor of type T from the cell (x, y)
     */
    public <T extends Actor> T getActorAt(int x, int y, Class<T> cls)
    {
        return getActor(grid[x][y].actors, cls);
    }

    /**
     * Gets a single actor of a specified type from anywhere on the world.. If
     * no actor of the type is present, null is returned. If there are multiple
     * actors of the given type are present, one of them will be returned
     * arbitrarily.
     * @param <T> the type, extending Actor, of the actor to be returned
     * @param cls the class that will define T
     * @return an actor of type T
     */
    public <T extends Actor> T getActor(Class<T> cls)
    {
        return getActor(actors, cls);
    }

    @SuppressWarnings("unchecked")
    private <T extends Actor> T getActor(Set<Actor> actors, Class<T> cls)
    {
        for(Actor actor : actors)
            if(cls.isInstance(actor))
                return (T)actor;
        return null;
    }

    /**
     * Returns all the actors of the specified type that are present at the
     * location (x, y).
     * @param <T> the type, extending Actor, of the actors to be returned
     * @param x the x value of the location of the actors returned
     * @param y the y value of the location of the actors returned
     * @param cls the class that will define T
     * @return the set of actors of type T present at (x, y)
     */
    public <T extends Actor> Set<T> getActorsAt(int x, int y, Class<T> cls)
    {
        return getActors(grid[x][y].actors, cls);
    }

    /**
     * Returns all the actors of the specified type that are present in the
     * world.
     * @param <T> the type, extending Actor, of the actors to be returned
     * @param cls the class that will define T
     * @return the set of actors of type T present on the world
     */
    public <T extends Actor> Set<T> getActors(Class<T> cls)
    {
        return getActors(actors, cls);
    }

    @SuppressWarnings("unchecked")
    private <T extends Actor> Set<T> getActors(Set<Actor> actors, Class<T> cls)
    {
        Set<T> filtered = new HashSet<T>();
        for(Actor actor : actors)
            if(cls.isInstance(actor))
                filtered.add((T)actor);
        return filtered;
    }

    /**
     * Adds the actor and places it at the location (x, y). This will modify
     * actor so that its world is this one, and its position is (x, y), as well
     * as modifying the world so getActor can return the actor.
     * @param actor the actor to be added
     * @param x the x value of the new location of the actor
     * @param y the y value of the new location of the actor
     */
    public void addActor(Actor actor, int x, int y)
    {
        if(actor.bound() || actor.held())
            throw new IllegalStateException("Actor not free to be added");
        actor.setWorld(this);
        registerActor(actor);
        actor.setPos(x, y);
    }

    /**
     * Adds the actor and places it at the specified location. This will modify
     * actor so that its world is this one, and its position is the given Coord,
     * as well as modifying the world so getActor can return the actor.
     * @param actor the actor to be added
     * @param pos the new location of the actor on the world
     */
    public final void addActor(Actor actor, Coord pos)
    {
        addActor(actor, pos.x(), pos.y());
    }

    /**
     * Adds the actor and places it a randomly chosen location choosen using
     * getOpenTile with the specified dice. This will modify actor so that its
     * world is this one, and its position is the given Coord, as well as
     * modifying the world so getActor can return the actor.
     * @param actor the actor to be added
     * @param dice the random number generator used to get the position
     */
    public final void addActor(Actor actor, Dice dice)
    {
        addActor(actor, getOpenTile(dice));
    }

    /**
     * Adds the actor and places it a randomly chosen location choosen using
     * getOpenTile with the global instance of Dice. This will modify actor so
     * that its world is this one, and its position is the given Coord, as well
     * as modifying the world so getActor can return the actor.
     * @param actor the actor to be added
     */
    public final void addActor(Actor actor)
    {
        addActor(actor, Dice.global);
    }

    /**
     * Removes the specified actor from the world. After calling this method,
     * the actor will no longer be bound to any world, and getActor will no
     * longer return this actor.
     * @param actor the actor to remove from the world
     */
    public void removeActor(Actor actor)
    {
        if(!actor.bound(this))
            throw new IllegalStateException("Actor not bound to world");
        if(actor.held())
            actor.detach();
        removeFromGrid(actor);
        unregisterActor(actor);
        actor.setWorld(null);
    }
    
    public void removeExpired()
    {
        Set<Actor> expired = new HashSet<Actor>();
        for(Actor actor : actors)
            if(actor.expired())
                expired.add(actor);
        for(Actor actor : expired)
            if(actor.bound(this))
                removeActor(actor);
    }

    void registerActor(Actor actor)
    {
        actors.add(actor);
    }

    void unregisterActor(Actor actor)
    {
        actors.remove(actor);
    }

    void removeFromGrid(Actor actor)
    {
        if(!actor.bound(this))
            throw new IllegalStateException("Actor not bound to world");
        grid[actor.x()][actor.y()].actors.remove(actor);
    }

    void addToGrid(Actor actor)
    {
        if(!actor.bound(this))
            throw new IllegalStateException("Actor not bound to world");
        grid[actor.x()][actor.y()].actors.add(actor);
    }
}
