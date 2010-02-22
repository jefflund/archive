package jade.util.type;

import java.awt.Color;
import java.io.Serializable;

/**
 * A character with an associated color. They are immutable once instantiated.
 */
public class ColoredChar implements Serializable, Comparable<ColoredChar>
{
	private final char ch;
	private final Color color;

	/**
	 * Creates a new ColoredChar
	 */
	public ColoredChar(char ch, Color color)
	{
		this.ch = ch;
		this.color = color;
	}

	/**
	 * Gets the char portion of the ColoredChar
	 */
	public char ch()
	{
		return ch;
	}

	/**
	 * Gets the color portion of the ColoredChar
	 */
	public Color color()
	{
		return color;
	}

	/**
	 * Returns the same ColoredChar only darker
	 */
	public ColoredChar darker()
	{
		return new ColoredChar(ch, color.darker());
	}

	/**
	 * Returns the same ColoredChar with darker() called magnitude times
	 */
	public ColoredChar darker(int magnitude)
	{
		Color color = this.color;
		for(int i = 0; i < magnitude; i++)
			color = color.darker();
		return new ColoredChar(ch, color.darker());
	}

	/**
	 * Returns the same ColoredChar only brighter
	 */
	public ColoredChar brighter()
	{
		return new ColoredChar(ch, color.brighter());
	}

	@Override
	public String toString()
	{
		return Character.toString(ch);
	}

	@Override
	public int compareTo(ColoredChar other)
	{
		if(color.hashCode() == other.color.hashCode())
			return ch - other.ch;
		else
			return color.hashCode() - other.color.hashCode();
	}

	public boolean equals(ColoredChar obj)
	{
		return ch == obj.ch && color == obj.color;
	}
}
