package jade.ui;

import jade.util.ColoredChar;
import jade.util.Coord;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class that represents a terminal. Features a screen buffer, the
 * ability to save and recall a single screen buffer, and the ability to capture
 * key presses.
 */
public abstract class Terminal
{
    protected final Map<Coord, ColoredChar> screenBuffer;
    protected final Map<Coord, ColoredChar> savedBuffer;

    /**
     * Constructs a terminal with an empty buffer.
     */
    public Terminal()
    {
        screenBuffer = new HashMap<Coord, ColoredChar>();
        savedBuffer = new HashMap<Coord, ColoredChar>();
    }

    /**
     * Buffers a ColoredChar at the given position on the screen.
     * @param pos the position on screen
     * @param ch the ColoredChar to be buffered
     */
    public void bufferChar(Coord pos, ColoredChar ch)
    {
        synchronized(screenBuffer)
        {
            screenBuffer.put(pos, ch);
        }
    }

    /**
     * Buffers a ColoredChar as specified by ch and color at (x, y).
     * @param x the x value of the position on screen
     * @param y the y value of the position on screen
     * @param ch the character value to be buffered
     * @param color the color of the character to be buffered
     */
    public final void bufferChar(int x, int y, char ch, Color color)
    {
        bufferChar(new Coord(x, y), new ColoredChar(ch, color));
    }

    /**
     * Buffers a ColoredChar as specified by ch and color at the given position.
     * @param pos the position on screen
     * @param ch the character value to be buffered
     * @param color the color of the character to be buffered
     */
    public final void bufferChar(Coord pos, char ch, Color color)
    {
        bufferChar(pos, new ColoredChar(ch, color));
    }

    /**
     * Buffers a ColoredChar at (x, y).
     * @param x the x value of the position on screen
     * @param y the y value of the position on screen
     * @param ch the ColoredChar to be buffered
     */
    public final void bufferChar(int x, int y, ColoredChar ch)
    {
        bufferChar(new Coord(x, y), ch);
    }

    /**
     * Returns the contents of the buffer at the given coordinate
     * @param coord the location being queried
     * @return the contents of the buffer at coord
     */
    public ColoredChar charAt(Coord coord)
    {
        return savedBuffer.get(coord);
    }

    /**
     * Returns the contents of the buffer at (x, y).
     * @param x the x value of the location being queried
     * @param y the y value of the location being queried
     * @return the contents of the buffer at (x, y)
     */
    public final ColoredChar charAt(int x, int y)
    {
        return charAt(new Coord(x, y));
    }

    /**
     * Clears the contents of the buffer.
     */
    public void clearBuffer()
    {
        screenBuffer.clear();
    }

    /**
     * Saves the contents of the buffer for recall.
     */
    public void saveBuffer()
    {
        savedBuffer.clear();
        savedBuffer.putAll(savedBuffer);
    }

    /**
     * Restores the last saved buffer.
     */
    public void recallBuffer()
    {
        screenBuffer.clear();
        screenBuffer.putAll(savedBuffer);
    }

    /**
     * Updates the screen to reflect the current state of the buffer. Without a
     * call to this method, there is no guarantee that the screen will be
     * updated to reflect the screen buffer.
     */
    public abstract void update();

    /**
     * Returns the next key press, blocking until there is one.
     * @return
     */
    public abstract char getKey();

    /**
     * An alias for System.exit(0).
     */
    public void exit()
    {
        System.exit(0);
    }
}
