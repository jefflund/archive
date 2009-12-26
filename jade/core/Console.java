package jade.core;

import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Rect;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Extends a JPanel to emulated a Console
 */
public class Console extends JPanel implements Serializable
{
	private int tileHeight;
	private int tileWidth;
	private InputListener listener;
	private Map<Coord, ColoredChar> buffer;
	private Map<Coord, ColoredChar> saved;
	private Map<Camera, Coord> cameras;
	private Map<Character, char[]> macros;

	/**
	 * Creates a new default Console
	 */
	public Console()
	{
		this(12, 80, 24);
	}

	/**
	 * Creates a custom Console
	 */
	public Console(int tileSize, int width, int height)
	{
		this(tileSize, tileSize * 2 / 3, width, height);
	}

	protected Console(int tileHeight, int tileWidth, int width, int height)
	{
		this.tileHeight = tileHeight;
		this.tileWidth = tileWidth;
		listener = new InputListener();
		addKeyListener(listener);
		buffer = new TreeMap<Coord, ColoredChar>();
		saved = new TreeMap<Coord, ColoredChar>();
		setPreferredSize(new Dimension(width * tileWidth, height * tileHeight));
		setFont(new Font(Font.MONOSPACED, Font.PLAIN, tileHeight));
		setBackground(Color.black);
		setFocusable(true);
		cameras = new HashMap<Camera, Coord>();
		macros = new HashMap<Character, char[]>();
	}

	/**
	 * Returns a default Console in a JFrame
	 */
	public static Console getFramedConsole(String frameTitle)
	{
		final Console console = new Console();
		frameConsole(console, frameTitle);
		return console;
	}

	/**
	 * Returns a custom Console in a JFrame
	 */
	public static Console getFramedConsole(String frameTitle, int tileSize,
			int width, int height)
	{
		final Console console = new Console(tileSize, width, height);
		frameConsole(console, frameTitle);
		return console;
	}

	protected static void frameConsole(Console console, String frameTitle)
	{
		final JFrame frame = new JFrame(frameTitle);
		frame.add(console);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * Buffers a ColoredChar at the given location
	 */
	public void buffChar(Coord coord, ColoredChar ch)
	{
		buffer.put(coord, ch);
	}

	/**
	 * Buffers a ColoredChar at the given location
	 */
	public final void buffChar(int x, int y, ColoredChar ch)
	{
		buffChar(new Coord(x, y), ch);
	}

	/**
	 * Buffers a ColoredChar at the given location
	 */
	public final void buffChar(int x, int y, char ch, Color color)
	{
		buffChar(new Coord(x, y), new ColoredChar(ch, color));
	}

	/**
	 * Buffers a ColoredChar at the given location
	 */
	public final void buffChar(Coord coord, char ch, Color color)
	{
		buffChar(coord, new ColoredChar(ch, color));
	}

	/**
	 * Places the next key at the given location on the screen.
	 */
	public char echoChar(Coord coord, Color color)
	{
		final char key = getKey();
		buffChar(coord, key, color);
		refreshScreen();
		return key;
	}

	/**
	 * Places the next key at the given location on the screen.
	 */
	public final char echoChar(int x, int y, Color color)
	{
		return echoChar(new Coord(x, y), color);
	}

	/**
	 * Buffers a string at the given location on screen
	 */
	public void buffString(int x, int y, String str, Color color)
	{
		final int startX = x;
		for(final char ch : str.toCharArray())
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
				buffChar(x++, y, ch, color);
			}
		}
	}

	/**
	 * Clears the y'th line and buffers the string on that line
	 */
	public void buffLine(int line, String str, Color color)
	{
		clearLine(line);
		buffString(0, line, str, color);
	}

	/**
	 * Buffers a string at the given location on screen
	 */
	public final void buffString(Coord coord, String str, Color color)
	{
		buffString(coord.x(), coord.y(), str, color);
	}

	/**
	 * Places a string on screen as it is typed. Terminates when the terminator is
	 * pressed.
	 */
	public String echoString(int x, int y, Color color, char terminator)
	{
		String str = "";
		char key = getKey();
		while(key != terminator)
		{
			if((key >= 33 && key <= 126) || key == ' ')
			{
				buffChar(x++, y, key, color);
				refreshScreen();
				str += key;
			}
			else if(key == '\b')
			{
				buffChar(--x, y, ' ', Color.black);
				refreshScreen();
				str = str.substring(0, str.length() - 1);
			}
			key = getKey();
		}
		return str;
	}

	/**
	 * Places a string on screen as it is typed. Terminates when the terminator is
	 * pressed.
	 */
	public final String echoString(Coord coord, Color color, char terminator)
	{
		return echoString(coord.x(), coord.y(), color, terminator);
	}

	/**
	 * Returns the ColoredChar in the buffer at the specified location
	 */
	public final ColoredChar charAt(int x, int y)
	{
		return charAt(new Coord(x, y));
	}

	/**
	 * Returns the ColoredChar in the buffer at the specified location
	 */
	public ColoredChar charAt(Coord coord)
	{
		return buffer.get(coord);
	}

	/**
	 * Adds a camera to the console centered at the given location on screen
	 */
	public void addCamera(Camera camera, Coord screenCenter)
	{
		cameras.put(camera, screenCenter);
	}

	/**
	 * Adds a camera to the console centered at the given location on screen
	 */
	public final void addCamera(Camera camera, int centerX, int centerY)
	{
		cameras.put(camera, new Coord(centerX, centerY));
	}

	/**
	 * Gets the Messenger's messages and buffers them at the given location
	 */
	public void buffMessages(int x, int y, Messenger messenger)
	{
		buffString(x, y, messenger.getMessages(), Color.white);
	}

	/**
	 * Removes a camera from the console
	 */
	public void removeCamera(Camera camera)
	{
		cameras.remove(camera);
	}

	/**
	 * Buffers a ColoredChar relative to the camera at the center previously
	 * specified in addCamera. If addCamera has not been called, this method will
	 * fail.
	 */
	public final void buffRelCamera(Camera camera, Coord pos, char ch, Color color)
	{
		buffRelCamera(camera, pos, new ColoredChar(ch, color));
	}

	/**
	 * Buffers a ColoredChar relative to the camera at the center previously
	 * specified in addCamera. If addCamera has not been called, this method will
	 * fail.
	 */
	public void buffRelCamera(Camera camera, Coord pos, ColoredChar ch)
	{
		Coord center = cameras.get(camera);
		int offX = center.x() - camera.x();
		int offY = center.y() - camera.y();
		buffChar(pos.getTranslated(offX, offY), ch);
	}

	/**
	 * Buffers everything the camera sees centered at the location specified in
	 * addCamera. If addCamera has not been called first, this will fail.
	 */
	public void buffCamera(Camera camera)
	{
		Coord center = cameras.get(camera);
		int offX = center.x() - camera.x();
		int offY = center.y() - camera.y();
		for(Coord coord : camera.getFoV())
			buffChar(coord.getTranslated(offX, offY), camera.world().look(coord));
	}

	/**
	 * Saves the current buffer
	 */
	public void saveBuffer()
	{
		saved.clear();
		saved.putAll(buffer);
	}

	/**
	 * Reverts buffer to the previously saved one
	 */
	public void recallBuffer()
	{
		buffer.clear();
		buffer.putAll(saved);
	}

	/**
	 * Clears every thing in the buffer
	 */
	public void clearBuffer()
	{
		buffer.clear();
	}

	/**
	 * Clears the buffer in the given bounds
	 */
	public void clearBuffer(Rect bounds)
	{
		for(int x = bounds.xMin(); x <= bounds.xMax(); x++)
			for(int y = bounds.yMin(); y <= bounds.yMax(); y++)
				buffer.remove(new Coord(x, y));
	}

	/**
	 * Clears a line from the buffer
	 */
	public void clearLine(int y)
	{
		Set<Coord> inline = new HashSet<Coord>();
		for(Coord coord : buffer.keySet())
			if(coord.y() == y)
				inline.add(coord);
		for(Coord coord : inline)
			buffer.remove(coord);
	}

	/**
	 * Returns the next key press. GetKey is blocks for input.
	 */
	public char getKey()
	{
		try
		{
			listener.inputReady.acquire();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		return listener.input.remove();
	}

	/**
	 * Simulates a key press.
	 */
	public void pressKey(char key)
	{
		long time = System.currentTimeMillis();
		KeyEvent event = new KeyEvent(this, KeyEvent.KEY_PRESSED, time, 0, key, key);
		listener.keyPressed(event);
	}

	/**
	 * Maps a key to a macro. When ever the key is pressed, the macro will be put
	 * into the input buffer rather than the pressed keys. Note that macros are
	 * not recursive.
	 */
	public void addMacro(char key, String macro)
	{
		macros.put(key, macro.toCharArray());
	}

	/**
	 * Removes any macros associated with the key.
	 */
	public void removeMacro(char key)
	{
		macros.remove(key);
	}

	@Override
	protected void paintComponent(Graphics page)
	{
		super.paintComponent(page);
		for(final Coord coord : buffer.keySet())
		{
			page.setColor(buffer.get(coord).color());
			page.drawString(buffer.get(coord).toString(), coord.x() * tileWidth,
					(coord.y() + 1) * tileHeight);
		}
	}

	/**
	 * Refreshes the screen to reflect the buffer
	 */
	public void refreshScreen()
	{
		repaint();
	}

	/**
	 * Calls System.exit(0);
	 */
	public void exit()
	{
		System.exit(0);
	}

	/**
	 * Copies the state of the console. This is useful if you need to open a
	 * serialized console.
	 */
	public void copyState(Console console)
	{
		tileHeight = console.tileHeight;
		tileWidth = console.tileWidth;
		listener.input.clear();
		listener.input.addAll(console.listener.input);
		listener.inputReady.drainPermits();
		listener.inputReady.release(console.listener.inputReady.availablePermits());
		buffer.clear();
		buffer.putAll(console.buffer);
		saved.clear();
		saved.putAll(console.saved);
		setPreferredSize(new Dimension(console.getWidth(), console.getHeight()));
		cameras.clear();
		cameras.putAll(console.cameras);
		macros.clear();
		macros.putAll(console.macros);
	}

	private class InputListener extends KeyAdapter implements Serializable
	{
		private Queue<Character> input;
		private Semaphore inputReady;

		public InputListener()
		{
			input = new LinkedList<Character>();
			inputReady = new Semaphore(0);
		}

		@Override
		public void keyPressed(KeyEvent event)
		{
			char key = event.getKeyChar();
			if(macros.containsKey(key))
			{
				for(char macro : macros.get(key))
				{
					input.add(macro);
					inputReady.release();
				}
			}
			else
			{
				input.add(event.getKeyChar());
				inputReady.release();
			}
		}
	}

	public interface Camera
	{
		public Collection<Coord> getFoV();

		public World world();

		public int x();

		public int y();
	}
}
