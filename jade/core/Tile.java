package jade.core;

import jade.util.ColoredChar;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a single tile on a jade World. They can be accessed
 * using the tile method of the jade world.
 */
public final class Tile
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
	 * Changes the tile's appearance and default passability.
	 * @param ch the tile's new character representation.
	 * @param color the tile's new color
	 * @param passable the tile's new passability.
	 */
	public void setTile(char ch, Color color, boolean passable)
	{
		tile = new ColoredChar(ch, color);
		this.passable = passable;
	}

	/**
	 * Returns the tile's character representation. Note that for most cases,
	 * calling look on the jade World is preferable since it could be overriden to
	 * consider actor occupants.
	 * @return the tile's character representation
	 */
	public ColoredChar look()
	{
		return tile;
	}

	/**
	 * Returns the tile's default passability. Note that for most cases, calling
	 * passable on the jade World is prefereable since it could be overriden to
	 * consider actor occupants.
	 * @return the tile's default passability
	 */
	public boolean passable()
	{
		return passable;
	}

	Set<Actor> actors()
	{
		return actors;
	}
}