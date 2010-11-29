package jade.ui;

import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A simple roguelike console with functionality that can mimic a terminal.
 * Extends a JPanel so it can be embedded in any container.
 */
public class Console extends JPanel implements KeyListener, Serializable
{
    public static final int DEFAULT_TILE_SIZE = 12;
    public static final int DEFAULT_COLS = 80;
    public static final int DEFAULT_ROWS = 24;

    private Map<Coord, ColoredChar> screenBuffer;
    private Map<Coord, ColoredChar> savedScreenBuffer;
    private Queue<Character> inputBuffer;
    private int tileWidth;
    private int tileHeight;

    /**
     * Creates a new Console with all the values defaulted. This Console will
     * initially have 80x24 tiles, with a tile size of 12.
     */
    public Console()
    {
        this(DEFAULT_COLS, DEFAULT_ROWS, DEFAULT_TILE_SIZE);
    }

    /**
     * Creates a new Console with the given specifications. The tile size will
     * be the default.
     * @param cols the number of columns initially shown
     * @param rows the number of rows initially shown
     */
    public Console(int cols, int rows)
    {
        this(cols, rows, DEFAULT_TILE_SIZE);
    }

    /**
     * Creates a new Console with the given specifications. The tile height is
     * the same as tile size, while tile width will be tilesize * 2 / 3.
     * @param tileSize the size of each tile
     * @param cols the number of columns initially shown
     * @param rows the number of rows initially shown
     */
    public Console(int cols, int rows, int tileSize)
    {
        this(cols, rows, tileSize * 2 / 3, tileSize);
    }

    /**
     * Creates a new Console with the given specifications.
     * @param tileWidth the width of each tile
     * @param tileHeight the height of each tile
     * @param cols the number of columns the console intially shows
     * @param rows the number of rows the console initially shows
     */
    public Console(int cols, int rows, int tileWidth, int tileHeight)
    {
        this.tileHeight = tileHeight;
        this.tileWidth = tileWidth;
        screenBuffer = new HashMap<Coord, ColoredChar>();
        savedScreenBuffer = new HashMap<Coord, ColoredChar>();
        inputBuffer = new LinkedList<Character>();
        addKeyListener(this);
        setPreferredSize(new Dimension(cols * tileWidth, rows * tileHeight));
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, tileHeight));
        setBackground(Color.black);
        setFocusable(true);
    }

    /**
     * Creates and returns a new Console and places it inside a JFrame. This
     * console will initially have 80x24 tiles, with a tile size of 12.
     * @param frameTitle the title of the JFrame
     * @return a new Console
     */
    public static Console getFramedConsole(String frameTitle)
    {
        Console console = new Console();
        frameConsole(console, frameTitle);
        return console;
    }

    /**
     * Creates and returns a new Console and places it inside a JFrame. The tile
     * size will be the default.
     * @param frameTitle the title of the JFrame
     * @param cols the number of columns the console intially shows
     * @param rows the number of rows the console initially shows
     * @return a new Console
     */
    public static Console getFramedConsole(String frameTitle, int cols, int rows)
    {
        Console console = new Console(cols, rows);
        frameConsole(console, frameTitle);
        return console;
    }

    /**
     * Creates and returns a new Console and places it inside a JFrame. The tile
     * height is the same as tile size, while tile width will be tilesize * 2 /
     * 3.
     * @param frameTitle the title of the JFrame
     * @param tileSize the size of each tile
     * @param cols the number of columns initially shown
     * @param rows the number of rows initially shown
     * @return a new Console
     */
    public static Console getFramedConsole(String frameTitle, int cols,
            int rows, int tileSize)
    {
        Console console = new Console(cols, rows, tileSize);
        frameConsole(console, frameTitle);
        return console;
    }

    /**
     * Creates and returns a new Console and places it inside a JFrame.
     * @param frameTitle the title of the JFrame
     * @param tileWidth the width of each tile
     * @param tileHeight the height of each tile
     * @param cols the number of columns the console intially shows
     * @param rows the number of rows the console initially shows
     * @return a new Console
     */
    public static Console getFramedConsole(String frameTitle, int cols,
            int rows, int tileWidth, int tileHeight)
    {
        Console console = new Console(cols, rows, tileWidth, tileHeight);
        frameConsole(console, frameTitle);
        return console;
    }

    private static void frameConsole(Console console, String frameTitle)
    {
        JFrame frame = new JFrame(frameTitle);
        frame.add(console);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Buffers a ColoredChar at the specified location.
     * @param coord the location (in tiles) of the ColoredChar on screen
     * @param ch the ColoredChar to be buffered
     */
    public void bufferChar(ColoredChar ch, Coord coord)
    {
        synchronized(screenBuffer)
        {
            screenBuffer.put(coord, ch);
        }
    }

    /**
     * Buffers a ColoredChar at the specified location.
     * @param ch the character value of the ColoredChar
     * @param color the color of the ColoredChar
     * @param coord the location (in tiles) of the ColoredChar on screen
     */
    public final void bufferChar(char ch, Color color, Coord coord)
    {
        bufferChar(new ColoredChar(ch, color), coord);
    }

    /**
     * Buffers a ColoredChar at the specified location.
     * @param ch the ColoredChar to be buffered
     * @param x the x position (in tiles) of the ColoredChar on screen
     * @param t the t position (in tiles) of the ColoredChar on screen
     */
    public final void bufferChar(ColoredChar ch, int x, int y)
    {
        bufferChar(ch, new Coord(x, y));
    }

    /**
     * Buffers a ColoredChar at the specified location.
     * @param ch the character value of the ColoredChar
     * @param color the color of the ColoredChar
     * @param x the x position (in tiles) of the ColoredChar on screen
     * @param t the t position (in tiles) of the ColoredChar on screen
     */
    public final void bufferChar(char ch, Color color, int x, int y)
    {
        bufferChar(new ColoredChar(ch, color), new Coord(x, y));
    }

    /**
     * Gets the character in the buffer at the given coordinate. Returns null if
     * there is nothing in the buffer.
     * @param coord the coordinate to be examined
     * @return the character in the buffer at the given Coord
     */
    public ColoredChar charAt(Coord coord)
    {
        return screenBuffer.containsKey(coord) ? screenBuffer.get(coord) : null;
    }

    /**
     * Gets the character in the buffer at the given coordinate. Returns null if
     * there is nothing in the buffer.
     * @param x the x value of the coordinate to be examined
     * @param y the y value of the coordinate to be examined
     * @return the character in the buffer at the given coordinate
     */
    public final ColoredChar charAt(int x, int y)
    {
        return charAt(new Coord(x, y));
    }

    /**
     * Saves the current state of the buffer for later recall.
     */
    public void saveBuffer()
    {
        savedScreenBuffer.clear();
        synchronized(screenBuffer)
        {
            savedScreenBuffer.putAll(screenBuffer);
        }
    }

    /**
     * Loads the last saved state into the current buffer.
     */
    public void recallBuffer()
    {
        synchronized(screenBuffer)
        {
            screenBuffer.clear();
            screenBuffer.putAll(savedScreenBuffer);
        }
    }

    /**
     * Clears the screen buffer
     */
    public void clearBuffer()
    {
        synchronized(screenBuffer)
        {
            screenBuffer.clear();
        }
    }

    /**
     * Updates the screen to reflect the current state of the buffer. This is
     * simply an alias for repaint().
     */
    public void updateScreen()
    {
        repaint();
    }

    /**
     * Buffers an entire string in the specified color, at the specified
     * coordinate. Any time '\n' is encountered, the rest of the string will be
     * buffered one line down, alligned with the line above it. No checks about
     * the dimensions of the screen are made, so it is possible that long
     * strings extend past the view of the user.
     * @param str the string to buffered
     * @param color the color of the text
     * @param coord the starting location of the text on screen
     */
    public final void bufferString(String str, Color color, Coord coord)
    {
        bufferString(str, color, coord.x(), coord.y());
    }

    /**
     * Buffers an entire string in the specified color, at the specified
     * coordinate. Any time '\n' is encountered, the rest of the string will be
     * buffered one line down, alligned with the line above it. No checks about
     * the dimensions of the screen are made, so it is possible that long
     * strings extend past the view of the user.
     * @param str the string to buffered
     * @param color the color of the text
     * @param x the x value of the starting location of the text on screen
     * @param y the y value of the starting location of the text on screen
     */
    public void bufferString(String str, Color color, int x, int y)
    {
        int startX = x;
        for(char ch : str.toCharArray())
        {
            switch(ch)
            {
            case '\n':
                x = startX;
                y++;
                break;
            case '\t':
                x += 2;
                break;
            default:
                bufferChar(ch, color, x++, y);
                
            }
        }
    }

    /**
     * Clears a given line from the buffer.
     * @param y the index of the line to be cleared
     */
    public void clearLine(int y)
    {
        Iterator<Coord> iter = screenBuffer.keySet().iterator();
        while(iter.hasNext())
        {
            if(iter.next().y() == y)
                iter.remove();
        }
    }

    /**
     * Clears the given line and buffers the string on that line.
     * @param y the line to be cleared
     * @param str the string to be buffered on line y
     * @param color the color of the string to be buffered
     */
    public void bufferLine(int y, String str, Color color)
    {
        clearLine(y);
        bufferString(str, color, 0, y);
    }

    @Override
    protected void paintComponent(Graphics page)
    {
        super.paintComponent(page);
        synchronized(screenBuffer)
        {
            for(Coord coord : screenBuffer.keySet())
            {
                ColoredChar ch = screenBuffer.get(coord);
                page.setColor(ch.color());
                page.drawString(ch.toString(),
                                tileWidth * coord.x(),
                                tileHeight * (coord.y() + 1));
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent event)
    {
        char key = event.getKeyChar();
        synchronized(inputBuffer)
        {
            inputBuffer.add(key);
            inputBuffer.notify();
        }
    }

    /**
     * Returns the next available key press, blocking until there is one.
     * @return the next availabe key
     */
    public char getKey()
    {
        synchronized(inputBuffer)
        {
            try
            {
                inputBuffer.wait();
                return inputBuffer.remove();
            }
            catch(InterruptedException e)
            {
                return 0;
            }
        }
    }

    /**
     * Returns the next availble key press if there is one, otherwise returns
     * '\0' without blocking.
     */
    public char tryGetKey()
    {
        synchronized(inputBuffer)
        {
            if(!inputBuffer.isEmpty())
                return inputBuffer.remove();
            else
                return '\0';
        }
    }

    /**
     * An alias for System.exit(0).
     */
    public void exit()
    {
        System.exit(0);
    }

    @Override
    public void keyReleased(KeyEvent event)
    {
        // do nothing
    }

    @Override
    public void keyTyped(KeyEvent event)
    {
        // do nothing
    }
}
