package jade.core;

import jade.util.Dice;
import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import jade.util.type.Rect;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The home of Jade Actors.
 */
public abstract class World extends Messenger implements Serializable
{
	public final int width;
	public final int height;
	private final Tile[][] grid;
	private final HashSet<Actor> actorRegister;

	/**
	 * Creates a new World.
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
	 * Default behavior for the world. Should call Actor.act() were appropriate.
	 */
	public abstract void tick();

	/**
	 * Adds an Actor and those it holds to the World at the given location.
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
	 * Adds an Actor and those it holds to the World at the given location.
	 */
	public final void addActor(Actor actor, Coord coord)
	{
		addActor(actor, coord.x(), coord.y());
	}

	/**
	 * Adds an Actor and those it holds to the World at a random open location.
	 */
	public final void addActor(Actor actor, Dice random)
	{
		final Coord pos = getOpenTile(random);
		addActor(actor, pos.x(), pos.y());
	}

	/**
	 * Retrieves an Actor of the specified class at the given location. If more
	 * than one are present, there is not guarantee as to which will be returned.
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Actor> T getActorAt(int x, int y, Class<T> cls)
	{
		for(final Actor actor : grid[x][y].actors())
			if(cls.isInstance(actor))
				return (T)actor;
		return null;
	}

	/**
	 * Retrieves an Actor of the specified class at the given location. If more
	 * than one are present, there is not guarantee as to which will be returned.
	 */
	public final <T extends Actor> T getActorAt(Coord coord, Class<T> cls)
	{
		return getActorAt(coord.x(), coord.y(), cls);
	}

	/**
	 * Returns all the Actors of the specified class at the given location.
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Actor> Collection<T> getActorsAt(int x, int y,
			Class<T> cls)
	{
		final Collection<T> result = new HashSet<T>();
		for(final Actor actor : grid[x][y].actors())
			if(cls.isInstance(actor))
				result.add((T)actor);
		return result;
	}

	/**
	 * Returns all the Actors of the specified class at the given location.
	 */
	public final <T extends Actor> Collection<T> getActorsAt(Coord coord,
			Class<T> cls)
	{
		return getActorsAt(coord.x(), coord.y(), cls);
	}

	/**
	 * Returns all Actors of the specified class currently in the World.
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Actor> Collection<T> getActors(Class<T> cls)
	{
		final Collection<T> result = new HashSet<T>();
		for(final Actor actor : actorRegister)
			if(cls.isInstance(actor))
				result.add((T)actor);
		return result;
	}

	/**
	 * Removes an Actor and those it holds from the World.
	 */
	public final void removeActor(Actor actor)
	{
		assert (actor.boundTo(this));
		if(actor.held())
			actor.detachFrom();
		removeFromGrid(actor);
		unregisterActor(actor);
		actor.setWorld(null);
	}

	/**
	 * Removes all expired actors from the World.
	 */
	public final void removeExpired()
	{
		final Collection<Actor> expired = new HashSet<Actor>();
		for(final Actor actor : actorRegister)
			if(actor.isExpired())
				expired.add(actor);
		for(final Actor actor : expired)
			if(actor.boundTo(this))
				removeActor(actor);
	}

	/**
	 * Returns the tile at the specified location.
	 */
	public final Tile tile(int x, int y)
	{
		return grid[x][y];
	}

	/**
	 * Returns the tile at the specified location.
	 */
	public final Tile tile(Coord coord)
	{
		return tile(coord.x(), coord.y());
	}

	/**
	 * Returns a random open tile, using passable()
	 */
	public final Coord getOpenTile(Dice random)
	{
		return getOpenTile(random, 0, 0, width - 1, height - 1);
	}

	/**
	 * Returns a random open tile from the given bounds.
	 */
	public final Coord getOpenTile(Dice random, Coord upperleft, Coord lowerright)
	{
		return getOpenTile(random, upperleft.x(), upperleft.y(), lowerright.x(),
				lowerright.y());
	}

	/**
	 * Returns a random open tile from the given bounds.
	 */
	public final Coord getOpenTile(Dice random, Rect rect)
	{
		return getOpenTile(random, rect.xMin(), rect.yMin(), rect.xMax(), rect
				.yMax());
	}

	/**
	 * Returns a random open tile from the given bounds.
	 */
	public final Coord getOpenTile(Dice random, int x1, int y1, int x2, int y2)
	{
		int x, y;
		int count = 0;
		do
		{
			x = random.nextInt(x1, x2);
			y = random.nextInt(y1, y2);
			count++;
			if(count == 1000)
				for(int b = 0; b < height; b++)
				{
					for(int a = 0; a < width; a++)
						System.out.print(look(a, b).ch());
					System.out.println();
				}
		}
		while(!passable(x, y) || getActorsAt(x, y, Actor.class).size() > 0);
		return new Coord(x, y);
	}

	/**
	 * Returns the top face visible from the tile. Normally this will be the one
	 * drawn.
	 */
	public final ColoredChar look(int x, int y)
	{
		List<ColoredChar> look = lookAll(x, y);
		return look.get(look.size() - 1);
	}

	/**
	 * Returns the top face visible from the tile. Normally this will be the one
	 * drawn.
	 */
	public final ColoredChar look(Coord coord)
	{
		return look(coord.x(), coord.y());
	}

	/**
	 * Returns everything visible from a given location. The top item is the last
	 * item in the list, with the actual tile being the first item in the list.
	 */
	public final List<ColoredChar> lookAll(Coord coord)
	{
		return lookAll(coord.x(), coord.y());
	}

	/**
	 * Returns everything visible from a given location. The top item is the last
	 * item in the list, with the actual tile being the first item in the list. By
	 * default it only examines the tile, but should be overriden to take into
	 * account the tile's occupants and the proper draw order of the occupants.
	 */
	public List<ColoredChar> lookAll(int x, int y)
	{
		List<ColoredChar> look = new ArrayList<ColoredChar>();
		look.add(grid[x][y].look());
		return look;
	}

	/**
	 * Returns true if the tile at the given location is passable. Should be
	 * overridden to reflect the tile's occupants.
	 */
	public boolean passable(int x, int y)
	{
		return grid[x][y].passable();
	}

	/**
	 * Returns true if the tile at the given location is passable. Should be
	 * overridden to reflect the tile's occupants.
	 */
	public final boolean passable(Coord coord)
	{
		return passable(coord.x(), coord.y());
	}

	final void addToGrid(Actor actor)
	{
		assert (actor.boundTo(this));
		grid[actor.x()][actor.y()].actors().add(actor);
	}

	final void removeFromGrid(Actor actor)
	{
		assert (actor.boundTo(this));
		grid[actor.x()][actor.y()].actors().remove(actor);
	}

	final void registerActor(Actor actor)
	{
		actorRegister.add(actor);
		for(final Actor held : actor.holds())
			registerActor(held);
	}

	final void unregisterActor(Actor actor)
	{
		actorRegister.remove(actor);
		for(final Actor held : actor.holds())
			unregisterActor(held);
	}

	public final class Tile implements Serializable
	{
		private ColoredChar tile;
		private boolean passable;
		private final HashSet<Actor> actors;

		Tile()
		{
			setTile('.', Color.white, true);
			actors = new HashSet<Actor>();
		}

		/**
		 * Sets the default tile's appearance and passability.
		 */
		public void setTile(char ch, Color color, boolean passable)
		{
			tile = new ColoredChar(ch, color);
			this.passable = passable;
		}

		/**
		 * Gets the face of the tile. Unlike World.look(), this method examines only
		 * the tile, and not its occupants.
		 */
		public final ColoredChar look()
		{
			return tile;
		}

		/**
		 * Gets the default passablilty of the tile. Unlike World.passable(), this
		 * method examines only the tile, and not its occupants.
		 */
		public final boolean passable()
		{
			return passable;
		}

		Set<Actor> actors()
		{
			return actors;
		}
	}
}