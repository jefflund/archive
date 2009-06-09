package jade.core;

import jade.util.ColoredChar;
import jade.util.Coord;
import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

public abstract class World extends Messenger
{
	public final int width;
	public final int height;
	private Tile[][] grid;
	private HashSet<Actor> actorRegister;

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

	public abstract void tick();

	public void addActor(Actor actor, int x, int y)
	{
		assert (!actor.bound());
		assert (!actor.held());
		actor.setWorld(this);
		registerActor(actor);
		actor.setPos(x, y);
	}

	public void addActor(Actor actor, Random random)
	{
		Coord pos = getOpenTile(random);
		addActor(actor, pos.x(), pos.y());
	}

  @SuppressWarnings("unchecked")
  public <T extends Actor> T getActorAt2(int x, int y, Class<T> cls)
	{
  	for(Actor actor : grid[x][y].actors)
			if(cls.isInstance(actor))
				return (T)actor;
		return null;
	}
  
	@SuppressWarnings("unchecked")
  public <T extends Actor> Collection<T> getActors2(Class<T> cls)
	{
		Collection<T> result = new HashSet<T>();
		for(Actor actor : actorRegister)
			if(cls.isInstance(actor))
				result.add((T)actor);
		return result;
	}

	@SuppressWarnings("unchecked")
  public <T extends Actor> Collection<T> getActorsAt2(int x, int y, Class<T> cls)
	{
		Collection<T> result = new HashSet<T>();
		for(Actor actor : grid[x][y].actors)
			if(cls.isInstance(actor))
				result.add((T)actor);
		return result;
	}

	public void removeActor(Actor actor)
	{
		assert (actor.boundTo(this));
		if(actor.held())
			actor.detachFrom();
		removeFromGrid(actor);
		unregisterActor(actor);
		actor.setWorld(null);
	}

	public void removeExpired()
	{
		Collection<Actor> expired = new HashSet<Actor>();
		for(Actor actor : actorRegister)
			if(actor.isExpired())
				expired.add(actor);
		for(Actor actor : expired)
			removeActor(actor);
	}

	public Tile tile(int x, int y)
	{
		return grid[x][y];
	}

	public Coord getOpenTile(Random random)
	{
		int x, y;
		do
		{
			x = random.nextInt(width);
			y = random.nextInt(height);
		}
		while(!passable(x, y) || getActorsAt2(x, y, Actor.class).size() > 0);
		return new Coord(x, y);
	}

	public ColoredChar look(int x, int y)
	{
		return grid[x][y].tile;
	}
	
	public ColoredChar look(Coord coord)
	{
		return look(coord.x(), coord.y());
	}

	public boolean passable(int x, int y)
	{
		return grid[x][y].passable;
	}
	
	public boolean passable(Coord coord)
	{
		return passable(coord.x(), coord.y());
	}

	void addToGrid(Actor actor)
	{
		assert (actor.boundTo(this));
		grid[actor.x()][actor.y()].actors.add(actor);
	}

	void removeFromGrid(Actor actor)
	{
		assert (actor.boundTo(this));
		grid[actor.x()][actor.y()].actors.remove(actor);
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

	public class Tile
	{
		private ColoredChar tile;
		private boolean passable;
		private HashSet<Actor> actors;

		private Tile()
		{
			setTile('.', Color.white, true);
			actors = new HashSet<Actor>();
		}

		public void setTile(char ch, Color color, boolean passable)
		{
			tile = new ColoredChar(ch, color);
			this.passable = passable;
		}
	}
}