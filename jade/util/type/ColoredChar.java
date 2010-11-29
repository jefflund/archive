package jade.util.type;

import java.awt.Color;
import java.io.Serializable;

/**
 * A character with a color. This class is immutible once instantiated.
 * ColoredChar is the type used as the face for all actors and tiles in the jade
 * system, even if the display does not actually render characters.
 */
public class ColoredChar implements Serializable
{
    private final char ch;
    private final Color color;

    /**
     * Creates a new instance of ColoredChar with the given values. Once
     * instantiated, these values are immutable.
     * @param ch the character of the new ColoredChar
     * @param color the color of the new ColoredChar
     */
    public ColoredChar(char ch, Color color)
    {
        this.ch = ch;
        this.color = color;
    }

    /**
     * Getter for the char of the ColoredChar
     * @return the char of the ColoredChar
     */
    public char ch()
    {
        return ch;
    }

    /**
     * Getter for the color of the ColoredChar
     * @return the color of the ColoredChar
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
