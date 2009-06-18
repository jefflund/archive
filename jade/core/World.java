package jade.core;

import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Dice;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * This class is a 2D grid based world in which jade actors live. Each tile in
 * the grid has an appearance and can hold actors. Through the tick method, the
 * world's behavior is controled. Most likely, the tick method will call the act
 * method on the appropriate actors.
 */
public abstract class World extends Messenger implements Serializable
{
	/**
	 * The width of the world
	 */
	public final int width;
	/**
	 * The height of the world
	 */
	public final int height;
	private Tile[][] grid;
	private HashSet<Actor> actorRegister;

	/**
	 * Constructs a new world with the specified size. This size is immutable once
	 * the world is created.
	 * @param width the width of the world
	 * @param height the height of the world
	 */
	public World(int width, int height)
	{
		this.width = width;
		this.height = height;
		grid = new Tile[width][height];
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				grid[x][y] = new Tile();
		actorRegister = new HashSet<Actor>();
	}

	/**
	 * Specifies the behavior of the world
	 */
	public abstract void tick();

	/**
	 * Adds an actor on the world at the specified location. The actors world will
	 * be set to this world. Also, any actors attached to the actor will also be
	 * added to this world.
	 * @param actor the actor to be added
	 * @param x the x-coordinate where the actor will be placed
	 * @param y the x-coordinate where the actor will be placed
	 */
	public void addActor(Actor actor, int x, int y)
	{
		assert (!actor.bound());
		assert (!actor.held());
		actor.setWorld(this);
		registerActor(actor);
		actor.setPos(x, y);
	}

	/**
	 * Adds an actor on the world at the specified location. The actors world will
	 * be set to this world. Also, any actors attached to the actor will also be
	 * added to this world.
	 * @param actor the actor to be added
	 * @param coord the coordinate where the actor will be placed
	 */
	public void addActor(Actor actor, Coord coord)
	{
		addActor(actor, coord.x(), coord.y());
	}

	/**
	 * Adds an actor to a random, open tile. A tile is open if there are no actors
	 * on the tile and the tile is passable.
	 * @param actor the actor to be added
	 * @param random the psuedorandom number generator to be used in selecting the
	 * open tile
	 */
	public void addActor(Actor actor, Dice random)
	{
		Coord pos = getOpenTile(random);
		addActor(actor, pos.x(), pos.y());
	}

	/**
	 * Returns one actor of the specified class from the given location, or null
	 * if none is found. The return type of this method will be the class passed
	 * into the method. If there are more than one actor of the specified class at
	 * the location, there is no guarantee as to which one will be returned.
	 * @param <T> extends Actor. Is the return type.
	 * @param x the x-coordinate to search at
	 * @param y the y-coordinate to search at
	 * @param cls determins T
	 * @return one actor of the specified class from the given location, or null
	 * if none is found.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Actor> T getActorAt(int x, int y, Class<T> cls)
	{
		for(Actor actor : grid[x][y].actors())
			if(cls.isInstance(actor))
				return (T)actor;
		return null;
	}

	/**
	 * Returns a collection with all the actors of the specified class at a
	 * location. The collection will be parameterized based on the given class.
	 * @param <T> extends Actor. Collection<T> will be the return type.
	 * @param x the x-coordinate to search at
	 * @param y the y-coordinate to search at
	 * @param cls determins T
	 * @return a collection with all the actors of the specified class at the
	 * given location
	 */
	@SuppressWarnings("unchecked")
	public <T extends Actor> Collection<T> getActorsAt(int x, int y, Class<T> cls)
	{
		Collection<T> result = new HashSet<T>();
		for(Actor actor : grid[x][y].actors())
			if(cls.isInstance(actor))
				result.add((T)actor);
		return result;
	}

	/**
	 * Returns a collection of all actors of the specified class currently on the
	 * world. The collection will be parameterized on the given class.
	 * @param <T> extends Actor. Collection<T> will be the return type
	 * @param cls determins T
	 * @return a collection of all actors of the specified class currently on the
	 * world.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Actor> Collection<T> getActors(Class<T> cls)
	{
		Collection<T> result = new HashSet<T>();
		for(Actor actor : actorRegister)
			if(cls.isInstance(actor))
				result.add((T)actor);
		return result;
	}

	/**
	 * Removes an actor, all with all actors that are attached to it from the
	 * world. The actor must belong to this world.
	 * @param actor the actor to remove
	 */
	public void removeActor(Actor actor)
	{
		assert (actor.boundTo(this));
		if(actor.held())
			actor.detachFrom();
		removeFromGrid(actor);
		unregisterActor(actor);
		actor.setWorld(null);
	}

	/**
	 * Removes all expired actors from the world. In most cases, this method
	 * should be called somewhere in the tick method so that expired actors are
	 * removed in a timely manner.
	 */
	public void removeExpired()
	{
		Collection<Actor> expired = new HashSet<Actor>();
		for(Actor actor : actorRegister)
			if(actor.isExpired())
				expired.add(actor);
		for(Actor actor : expired)
			removeActor(actor);
	}

	/**
	 * Returns the tile at the specifed location.
	 * @param x the x-coordinate of the tile to be returned
	 * @param y the y-coordinate of the tile to be returned
	 * @return the tile at the specifed location.
	 */
	public Tile tile(int x, int y)
	{
		return grid[x][y];
	}

	/**
	 * Returns the tile at the specifed location.
	 * @param coord the coordinate of the tile to be returned
	 * @return the tile at the specifed location.
	 */
	public Tile tile(Coord coord)
	{
		return tile(coord.x(), coord.y());
	}

	/**
	 * Returns a random open tile on the world. A tile is open if it is passable
	 * and there are no actors on it.
	 * @param random the psuedorandom number generator to be used in finding the
	 * open tile
	 * @return a random open tile on the world
	 */
	public Coord getOpenTile(Dice random)
	{
		return getOpenTile(random, 0, 0, width - 1, height - 1);
	}

	/**
	 * Returns a random open tile on the world from within the specified bounds. A
	 * tile is open if it is passable and there are no actors on it.
	 * @param random the psuedorandom number generator to be used in finding the
	 * open tile
	 * @param upperleft the upper left boundry of the area from which to choose
	 * the random tile
	 * @param lowerright the lower right boundry of the area from which to choose
	 * the random tile
	 * @return a random open tile on the world
	 */
	public Coord getOpenTile(Dice random, Coord upperleft, Coord lowerright)
	{
		return getOpenTile(random, upperleft.x(), upperleft.y(), lowerright.x(),
		    lowerright.y());
	}

	/**
	 * Returns a random open tile on the world from within the specified bounds. A
	 * tile is open if it is passable and there are no actors on it.
	 * @param random the psuedorandom number generator to be used in finding the
	 * open tile
	 * @param x1 the left boundry of the area from which to choose the random tile
	 * @param y1 the upper boundry of the area from which to choose the random tile
	 * @param x2 the right boundry of the area from which to choose the random tile
	 * @param y2 the lower boundry of the area from which to choose the random tile
	 * @return a random open tile on the world
	 */
	public Coord getOpenTile(Dice random, int x1, int y1, int x2, int y2)
	{
		int x, y;
		do
		{
			x = random.nextInt(x1, x2);
			y = random.nextInt(y1, y2);
		}
		while(!passable(x, y) || getActorsAt(x, y, Actor.class).size() > 0);
		return new Coord(x, y);
	}

	/**
	 * Returns the appearance of the specified tile. By default this method
	 * returns the appearance of the tile itself, but could be overriden to return
	 * the look method of the actors on the tile.
	 * @param x the x-coordinate of the tile
	 * @param y the y-coordinate of the tile
	 * @return the appearance of the specified tile
	 */
	public ColoredChar look(int x, int y)
	{
		return grid[x][y].look();
	}

	/**
	 * Returns the appearance of the specified tile. By default this method
	 * returns the appearance of the tile itself, but could be overriden to return
	 * the look method of the actors on the tile.
	 * @param coord the coordinate location of the tile
	 * @return the appearance of the specified tile
	 */
	public ColoredChar look(Coord coord)
	{
		return look(coord.x(), coord.y());
	}

	/**
	 * Returns true if the specified tile is passable, false otherwise. By
	 * default, this method only examins the passable property of the tile, but
	 * could be overriden to consider the presence of actors.
	 * @param x the x-coordinate of the tile
	 * @param y the y-coordinate of the tile
	 * @return true if the specified tile is passable, false otherwise
	 */
	public boolean passable(int x, int y)
	{
		return grid[x][y].passable();
	}

	/**
	 * Returns true if the specified tile is passable, false otherwise. By
	 * default, this method only examins the passable property of the tile, but
	 * could be overriden to consider the presence of actors.
	 * @param coord the coordinate location of the tile
	 * @return true if the specified tile is passable, false otherwise
	 */
	public boolean passable(Coord coord)
	{
		return passable(coord.x(), coord.y());
	}

	void addToGrid(Actor actor)
	{
		assert (actor.boundTo(this));
		grid[actor.x()][actor.y()].actors().add(actor);
	}

	void removeFromGrid(Actor actor)
	{
		assert (actor.boundTo(this));
		grid[actor.x()][actor.y()].actors().remove(actor);
	}

	void registerActor(Actor actor)
	{
		actorRegister.add(actor);
		for(Actor held : actor.holds())
			registerActor(held);
	}

	void unregisterActor(Actor actor)
	{
		actorRegister.remove(actor);
		for(Actor held : actor.holds())
			unregisterActor(held);
	}
}