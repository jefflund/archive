package jade.core;

import jade.util.ColoredChar;
import jade.util.Coord;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This class allows the JPanel to have console functionality simular to what
 * curses does in c. The api differs from curses but is fairly simple to use.
 * Any changes to the screen are written to a buffer which then must be painted
 * on the screen by use of the refreshScreen method. A second buffer is also
 * provided to allow for saving and restoring of screen. A getKey method is also
 * provided, which blocks until the next key press like getch in curses.
 */
public class Console extends JPanel implements Serializable
{
	private final int tileHeight;
	private final int tileWidth;
	private transient Thread mainThread;
	private InputListener listener;
	private TreeMap<Coord, ColoredChar> buffer;
	private TreeMap<Coord, ColoredChar> saved;

	/**
	 * Constructs a new default console with a default size of 80 x 24 tiles;
	 */
	public Console()
	{
		this(12, 80, 24);
	}

	/**
	 * Constructs a new console with a specific starting size.
	 * @param tileSize the size of the tiles. The default is 12.
	 * @param width the number of columns. The default is 80.
	 * @param height the number of rows. The default is 24.
	 */
	public Console(int tileSize, int width, int height)
	{
		tileHeight = tileSize;
		tileWidth = tileHeight * 2 / 3;
		onDeserialize();
		listener = new InputListener();
		addKeyListener(listener);
		buffer = new TreeMap<Coord, ColoredChar>();
		saved = new TreeMap<Coord, ColoredChar>();
		setPreferredSize(new Dimension(width * tileWidth, height * tileHeight));
		setFont(new Font(Font.MONOSPACED, Font.PLAIN, tileHeight));
		setBackground(Color.black);
		setFocusable(true);
	}

	/**
	 * Returns a new default console which has been placed within a JFrame.
	 * 
	 * @param frameTitle the title of the JFrame
	 * @return a new instance of Console
	 */
	public static Console getFramedConsole(String frameTitle)
	{
		Console console = new Console();
		frameConsole(console, frameTitle);
		return console;
	}

	/**
	 * Returns a new console of the specifed size which has been placed within a
	 * JFrame.
	 * 
	 * @param frameTitle the title of the JFrame
	 * @param tileSize the size of the console tiles. The default is 12.
	 * @param width the number of console columns. The default is 80.
	 * @param height the console number of rows. The default is 24.
	 * @return a new instance of Console
	 */
	public static Console getFramedConsole(String frameTitle, int tileSize,
	    int width, int height)
	{
		Console console = new Console(tileSize, width, height);
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
	 * Places a specified character in the buffer at the given location.
	 * 
	 * @param coord the location of the character
	 * @param ch the character to be buffered
	 */
	public void buffChar(Coord coord, ColoredChar ch)
	{
		buffer.put(coord, ch);
	}

	/**
	 * Places a specified character in the buffer at the given location.
	 * 
	 * @param x the x-coordinate of the character
	 * @param y the y-coordinate of the character
	 * @param ch the character to be buffered
	 */
	public void buffChar(int x, int y, ColoredChar ch)
	{
		buffChar(new Coord(x, y), ch);
	}

	/**
	 * Places a specified character in the buffer at the given location.
	 * 
	 * @param x the x-coordinate of the character
	 * @param y the y-coordinate of the character
	 * @param ch the character to be buffered
	 * @param color the color of the character to be buffered
	 */
	public void buffChar(int x, int y, char ch, Color color)
	{
		buffChar(new Coord(x, y), new ColoredChar(ch, color));
	}

	/**
	 * Places a specified character in the buffer at the given location.
	 * 
	 * @param coord the location of the character
	 * @param ch the character to be buffered
	 * @param color the color of the character to be buffered
	 */
	public void buffChar(Coord coord, char ch, Color color)
	{
		buffChar(coord, new ColoredChar(ch, color));
	}

	/**
	 * Places a string in the buffer at the given location. This method does not
	 * wrap the text.
	 * 
	 * @param x the x-coordinate of the string
	 * @param y the y-coordinate of the string
	 * @param str the string to be buffered
	 * @param color the color of the string
	 */
	public void buffString(int x, int y, String str, Color color)
	{
		for(char ch : str.toCharArray())
			buffChar(x++, y, ch, color);
	}

	/**
	 * Places a string in the buffer at the given location. This method does not
	 * wrap the text.
	 * 
	 * @param coord the location of the string
	 * @param str the string to be buffered
	 * @param color the color of the string
	 */
	public void buffString(Coord coord, String str, Color color)
	{
		buffString(coord.x(), coord.y(), str, color);
	}

	/**
	 * Returns the character at the specified location.
	 * 
	 * @param x the x-coordinate to be checked
	 * @param y the y-coordinate to be checked
	 * @return the character at the specified location.
	 */
	public ColoredChar charAt(int x, int y)
	{
		return charAt(new Coord(x, y));
	}

	/**
	 * Returns the character at the specified location.
	 * 
	 * @param coord the location to be checked
	 * @return the character at the specified location.
	 */
	public ColoredChar charAt(Coord coord)
	{
		return buffer.get(coord);
	}

	/**
	 * Saves the current buffer to the secondary buffer.
	 */
	public void saveBuffer()
	{
		saved.clear();
		saved.putAll(buffer);
	}

	/**
	 * Overwrites the current buffer with the secondary buffer.
	 */
	public void recallBuffer()
	{
		buffer.clear();
		buffer.putAll(saved);
	}

	/**
	 * Clears the current buffer.
	 */
	public void clearBuffer()
	{
		buffer.clear();
	}

	/**
	 * Returns the character of the next key press. This function will block until
	 * a key is pressed, simular to getch in c curses.
	 * 
	 * @return the character of the next key press.
	 */
	@SuppressWarnings("deprecation")
	public char getKey()
	{
		if(!listener.ready)
			mainThread.suspend();
		listener.ready = false;
		return listener.input;
	}

	public void paintComponent(Graphics page)
	{
		try
		{
			super.paintComponent(page);
			for(Coord coord : buffer.keySet())
			{
				page.setColor(buffer.get(coord).color());
				page.drawString(buffer.get(coord).toString(), coord.x() * tileWidth,
				    (coord.y() + 1) * tileHeight);
			}
		}
		catch(ConcurrentModificationException dontWorry)
		{
		}
	}

	/**
	 * Paints the current buffer to the screen.
	 */
	public void refreshScreen()
	{
		repaint();
	}

	/**
	 * In order for getKey to work, console must know about the main thread.
	 * However, since Threads are transient, this method must be called upon
	 * deserialization to avoid NullPointerExceptions when using getKey.
	 */
	public void onDeserialize()
	{
		mainThread = Thread.currentThread();
	}

	private class InputListener extends KeyAdapter implements Serializable
	{
		private char input;
		private boolean ready;

		public InputListener()
		{
			ready = false;
		}

		@SuppressWarnings("deprecation")
		public void keyPressed(KeyEvent event)
		{
			ready = true;
			input = event.getKeyChar();
			mainThread.resume();
		}
	}
}
