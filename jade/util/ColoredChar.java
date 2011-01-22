package jade.util;

import java.awt.Color;

/**
 *  An immutable character with an associated color
 */
public class ColoredChar
{
    private char ch;
    private Color color;

    /**
     * Constructs a new ColoredChar with the given properties
     * @param ch the character value of the ColoredChar
     * @param color the Color value of the ColoredChar
     */
    public ColoredChar(char ch, Color color)
    {
        this.ch = ch;
        this.color = color;
    }

    /**
     * Constructs a new white ColoredChar with the given character.
     * @param ch the character value of the ColoredChar
     */
    public ColoredChar(char ch)
    {
        this(ch, Color.white);
    }

    /**
     * Returns the character value of the ColoredChar.
     * @return the character value of the ColoredChar.
     */
    public char ch()
    {
        return ch;
    }

    /**
     * Returns the Color value of the ColoredChar.
     * @return the Color value of the ColoredChar.
     */
    public Color color()
    {
        return color;
    }
    
    @Override
    public String toString()
    {
        return Character.toString(ch);
    }
}
