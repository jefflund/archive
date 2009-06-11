package jade.util;

import java.awt.Color;
import java.io.Serializable;

/**
 * Represents a char with an associated color. ColoredChars are immutable once
 * created.
 */
public class ColoredChar implements Serializable
{
	private char ch;
	private Color color;

	/**
	 * Creates a new coloredchar with the specified char and color.
	 * @param ch
	 * @param color
	 */
	public ColoredChar(char ch, Color color)
	{
		this.ch = ch;
		this.color = color;
	}

	/**
	 * Returns the character represented by this ColoredChar.
	 * @return the character represented by this ColoredChar.
	 */
	public char ch()
	{
		return ch;
	}

	/**
	 * Returns the color represented by this ColoredChar.
	 * @return the color represented by this ColoredChar.
	 */
	public Color color()
	{
		return color;
	}

	public String toString()
	{
		return Character.toString(ch);
	}
}
