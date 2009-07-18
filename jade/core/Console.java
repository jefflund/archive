package jade.core;

import jade.fov.Camera;
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
import java.util.Map;
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
	public final int tileHeight;
	public final int tileWidth;
	private transient Thread mainThread;
	private InputListener listener;
	private Map<Coord, ColoredChar> buffer;
	private Map<Coord, ColoredChar> saved;

	/**
	 * Constructs a new default console with a default 80 x 24 size 12 tiles;
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
		this(tileSize, tileSize * 2 / 3, width, height);
	}

	protected Console(int tileHeight, int tileWidth, int width, int height)
	{
		this.tileHeight = tileHeight;
		this.tileWidth = tileWidth;
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

	protected static void frameConsole(Console console, String frameTitle)
	{
		JFrame frame = new JFrame(frameTitle);
		frame.add(console);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * Places a specified character in the buffer at the given location.
	 * @param coord the location of the character
	 * @param ch the character to be buffered
	 */
	public void buffChar(Coord coord, ColoredChar ch)
	{
		buffer.put(coord, ch);
	}

	/**
	 * Places a specified character in the buffer at the given location.
	 * @param x the x-coordinate of the character
	 * @param y the y-coordinate of the character
	 * @param ch the character to be buffered
	 */
	public final void buffChar(int x, int y, ColoredChar ch)
	{
		buffChar(new Coord(x, y), ch);
	}

	/**
	 * Places a specified character in the buffer at the given location.
	 * @param x the x-coordinate of the character
	 * @param y the y-coordinate of the character
	 * @param ch the character to be buffered
	 * @param color the color of the character to be buffered
	 */
	public final void buffChar(int x, int y, char ch, Color color)
	{
		buffChar(new Coord(x, y), new ColoredChar(ch, color));
	}

	/**
	 * Places a specified character in the buffer at the given location.
	 * @param coord the location of the character
	 * @param ch the character to be buffered
	 * @param color the color of the character to be buffered
	 */
	public final void buffChar(Coord coord, char ch, Color color)
	{
		buffChar(coord, new ColoredChar(ch, color));
	}

	/**
	 * Buffers a single char relative to the camera position on the screen. The
	 * camera will be centered at (x, y) on the screen. The ColoredChar will be
	 * the forground tile to use, while the background tile will be the tile that
	 * the camera sees at the coordinate. This is useful for drawing special effects
	 * as it will replace what is actually in the foreground with the specified
	 * ColoredChar.
	 * @param camera the camera to be used.
	 * @param x the x-coordinate of the camera on screen
	 * @param y the y-coordinate of the camera on screen
	 * @param coord the map coordinate to be drawn.
	 * @param ch the char to be drawn at coord
	 * @param color the color of the char to be drawn at coord
	 */
	public void buffCamera(Camera camera, int x, int y, Coord coord, char ch,
			Color color)
	{
		buffChar(coord.getTranslated(x - camera.x(), y - camera.y()), ch, color);
	}

	/**
	 * Draws everything the camera can see with the camera centered at (x, y)
	 * @param camera the camera
	 * @param x the x-coordinate of the camera on screen
	 * @param y the y-coordinate of the camera on screen
	 */
	public void buffCamera(Camera camera, int x, int y)
	{
		int offX = x - camera.x();
		int offY = y - camera.y();
		for(Coord coord : camera.getFoV())
			buffChar(coord.getTranslated(offX, offY), camera.world().look(coord));
	}

	/**
	 * This method blocks for keyboard input, the buffers the character to the
	 * given location and refreshes the screen.
	 * @param coord the location to print the key pressed
	 * @param color the color of the character to print
	 * @return the key pressed as a char
	 */
	public char echoChar(Coord coord, Color color)
	{
		char key = getKey();
		buffChar(coord, key, color);
		refreshScreen();
		return key;
	}

	/**
	 * This method blocks for keyboard input, the buffers the character to the
	 * given location and refreshes the screen.
	 * @param x the x-coordinate to print the key pressed
	 * @param y the y-coordinate to print the key pressed
	 * @param color the color of the character to print
	 * @return the key pressed as a char
	 */
	public final char echoChar(int x, int y, Color color)
	{
		return echoChar(new Coord(x, y), color);
	}

	/**
	 * Places a string in the buffer at the given location. This method does not
	 * wrap the text.
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
	 * @param coord the location of the string
	 * @param str the string to be buffered
	 * @param color the color of the string
	 */
	public final void buffString(Coord coord, String str, Color color)
	{
		buffString(coord.x(), coord.y(), str, color);
	}

	/**
	 * This method is simular to echoChar, only that it repeats until the
	 * terminator char is pressed. The location that each char is placed is one to
	 * the right.
	 * @param x the x-coordinate of the first char to be buffered.
	 * @param y the y-coordinate of the first char to be buffered.
	 * @param color the color of the string to be output
	 * @param terminator the char of the key that ends the string
	 * @return the string that was entered.
	 */
	public String echoString(int x, int y, Color color, char terminator)
	{
		String str = "";
		char key = getKey();
		while(key != terminator)
		{
			buffChar(x++, y, key, color);
			refreshScreen();
			str += key;
			key = getKey();
		}
		return str;
	}

	/**
	 * This method is simular to echoChar, only that it repeats until the
	 * terminator char is pressed. The location that each char is placed is one to
	 * the right.
	 * @param coord the coordinate of the first char to be buffered.
	 * @param color the color of the string to be output
	 * @param terminator the char of the key that ends the string
	 * @return the string that was entered.
	 */
	public String echoString(Coord coord, Color color, char terminator)
	{
		return echoString(coord.x(), coord.y(), color, terminator);
	}

	/**
	 * Returns the character at the specified location.
	 * @param x the x-coordinate to be checked
	 * @param y the y-coordinate to be checked
	 * @return the character at the specified location.
	 */
	public final ColoredChar charAt(int x, int y)
	{
		return charAt(new Coord(x, y));
	}

	/**
	 * Returns the character at the specified location.
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

	@Override
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
		// Near as I can tell, these exceptions get thrown due to concurrency with
		// the AWT-EventQueue threads. Basically, AWT is still painting when the
		// buffer is already in use again.
		catch(ConcurrentModificationException dontWorry)
		{
		}
		catch(NullPointerException dontWorry)
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

		@Override
		@SuppressWarnings("deprecation")
		public void keyPressed(KeyEvent event)
		{
			ready = true;
			input = event.getKeyChar();
			mainThread.resume();
		}
	}
}
