package jade.util;

import java.awt.Color;

public class ColoredChar
{
	private char ch;
	private Color color;

	public ColoredChar(char ch, Color color)
	{
		this.ch = ch;
		this.color = color;
	}

	public char ch()
	{
		return ch;
	}

	public Color color()
	{
		return color;
	}

	public String toString()
	{
		return Character.toString(ch);
	}
}
