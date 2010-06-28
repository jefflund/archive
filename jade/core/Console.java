package jade.core;

import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import jade.util.type.Rect;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
public class Console extends JPanel implements KeyListener, Serializable
{
	public static final int DEFAULT_TILE = 12;
	public static final int DEFAULT_WIDTH = 80;
	public static final int DEFAULT_HEIGHT = 24;

	protected int tileHeight;
	protected int tileWidth;
	private Queue<Character> inputBuffer;
	private Semaphore inputReady;
	private Map<Coord, ColoredChar> buffer;
	private Semaphore bufferReady;
	private Map<Coord, ColoredChar> saved;
	protected Map<Camera, Coord> cameras;
	private Map<Character, char[]> macros;

	/**
	 * Creates a new default Console
	 */
	public Console()
	{
		this(DEFAULT_TILE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Creates a custom Console
	 */
	public Console(int tileSize, int width, int height)
	{
		this(tileSize, tileSize * 2 / 3, width, height);
	}

	/**
	 * Constructs a new console
	 */
	protected Console(int tileHeight, int tileWidth, int width, int height)
	{
		this.tileHeight = tileHeight;
		this.tileWidth = tileWidth;
		inputBuffer = new LinkedList<Character>();
		inputReady = new Semaphore(0);
		addKeyListener(this);
		buffer = new TreeMap<Coord, ColoredChar>();
		bufferReady = new Semaphore(1);
		saved = new TreeMap<Coord, ColoredChar>();
		setPreferredSize(new Dimension(width * tileWidth, height * tileHeight));
		setFont(new Font(Font.MONOSPACED, Font.PLAIN, tileHeight));
		setBackground(Color.black);
		setFocusable(true);
		cameras = new HashMap<Camera, Coord>();
		macros = new HashMap<Character, char[]>();
	}

	private void aquireBuffer()
	{
		try
		{
			bufferReady.acquire();
		}
		catch(InterruptedException exception)
		{
			exception.printStackTrace();
		}
	}

	private void releaseBuffer()
	{
		bufferReady.release();
	}

	/**
	 * Returns a default Console in a JFrame
	 */
	public static Console getFramedConsole(String frameTitle)
	{
		Console console = new Console();
		frameConsole(console, frameTitle);
		return console;
	}

	/**
	 * Returns a custom Console in a JFrame
	 */
	public static Console getFramedConsole(String frameTitle, int tileSize,
			int width, int height)
	{
		Console console = new Console(tileSize, width, height);
		frameConsole(console, frameTitle);
		return console;
	}

	/**
	 * Puts a console inside a JFrame
	 */
	protected static void frameConsole(Console console, String frameTitle)
	{
		JFrame frame = new JFrame(frameTitle);
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
		aquireBuffer();
		buffer.put(coord, ch);
		releaseBuffer();
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
		char key = getKey();
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
		aquireBuffer();
		ColoredChar result = buffer.get(coord);
		releaseBuffer();
		return result;
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
	 * Gets the Messenger's messages and buffers them on the given line
	 */
	public void buffMessages(int y, Messenger messenger)
	{
		buffString(0, y, messenger.getMessages(), Color.white);
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
		aquireBuffer();
		saved.clear();
		saved.putAll(buffer);
		releaseBuffer();
	}

	/**
	 * Reverts buffer to the previously saved one
	 */
	public void recallBuffer()
	{
		aquireBuffer();
		buffer.clear();
		buffer.putAll(saved);
		releaseBuffer();
	}

	/**
	 * Clears every thing in the buffer
	 */
	public void clearBuffer()
	{
		aquireBuffer();
		buffer.clear();
		releaseBuffer();
	}

	/**
	 * Clears the buffer in the given bounds
	 */
	public void clearBuffer(Rect bounds)
	{
		aquireBuffer();
		for(int x = bounds.xMin(); x <= bounds.xMax(); x++)
			for(int y = bounds.yMin(); y <= bounds.yMax(); y++)
				buffer.remove(new Coord(x, y));
		releaseBuffer();
	}

	/**
	 * Clears a line from the buffer
	 */
	public void clearLine(int y)
	{
		aquireBuffer();
		Set<Coord> inline = new HashSet<Coord>();
		for(Coord coord : buffer.keySet())
			if(coord.y() == y)
				inline.add(coord);
		for(Coord coord : inline)
			buffer.remove(coord);
		releaseBuffer();
	}

	/** 
	 * Invoked when a key is pressed. Either puts the pressed key in the input
	 * buffer, or puts a sequence of key presses into the buffer if they key
	 * has an associated macro. 
	 */
	public void keyPressed(KeyEvent event)
	{
		char key = event.getKeyChar();
		if(!Character.isDefined(key))
			return;
		if(macros.containsKey(key))
		{
			for(char macro : macros.get(key))
			{
				inputBuffer.add(macro);
				inputReady.release();
			}
		}
		else
		{
			inputBuffer.add(event.getKeyChar());
			inputReady.release();
		}
	}

	/**
	 * Returns the next key press. GetKey blocks for input.
	 */
	public char getKey()
	{
		try
		{
			inputReady.acquire();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		return inputBuffer.remove();
	}

	/**
	 * Returns the next key press if there is one, otherwise returns '\0' without
	 * blocking.
	 */
	public char tryGetKey()
	{
		if(inputReady.tryAcquire())
			return inputBuffer.remove();
		else
			return '\0';
	}

	/**
	 * Clears all the key events wainting to be used. Often useful after
	 * tryGetKey() so that new inputs can overide old key presses.
	 */
	public void clearKeyBuffer()
	{
		inputReady.drainPermits();
		inputBuffer.clear();
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
		aquireBuffer();
		super.paintComponent(page);
		for(Coord coord : buffer.keySet())
		{
			ColoredChar ch = buffer.get(coord);
			page.setColor(ch.color());
			page.drawString(ch.toString(), coord.x() * tileWidth,
					(coord.y() + 1) * tileHeight);
		}
		releaseBuffer();
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
		inputBuffer.clear();
		inputBuffer.addAll(console.inputBuffer);
		inputReady.drainPermits();
		inputReady.release(console.inputReady.availablePermits());
		aquireBuffer();
		buffer.clear();
		buffer.putAll(console.buffer);
		releaseBuffer();
		saved.clear();
		saved.putAll(console.saved);
		setPreferredSize(new Dimension(console.getWidth(), console.getHeight()));
		cameras.clear();
		cameras.putAll(console.cameras);
		macros.clear();
		macros.putAll(console.macros);
	}

	public interface Camera
	{
		public Collection<Coord> getFoV();

		public World world();

		public int x();

		public int y();
	}

	//ctrl key constants
	public static final char CTRLA = 1;
	public static final char CTRLB = 2;
	public static final char CTRLC = 3;
	public static final char CTRLD = 4;
	public static final char CTRLE = 5;
	public static final char CTRLF = 6;
	public static final char CTRLG = 7;
	public static final char CTRLH = 8;
	public static final char CTRLI = 9;
	public static final char CTRLJ = 10;
	public static final char CTRLK = 11;
	public static final char CTRLL = 12;
	public static final char CTRLM = 13;
	public static final char CTRLN = 14;
	public static final char CTRLO = 15;
	public static final char CTRLP = 16;
	public static final char CTRLQ = 17;
	public static final char CTRLR = 18;
	public static final char CTRLS = 19;
	public static final char CTRLT = 20;
	public static final char CTRLU = 21;
	public static final char CTRLV = 22;
	public static final char CTRLW = 23;
	public static final char CTRLX = 24;
	public static final char CTRLY = 25;
	public static final char CTRLZ = 26;
	public static final char ESC = 27;

	public void keyReleased(KeyEvent e)
	{
		// do nothing
	}

	public void keyTyped(KeyEvent e)
	{
		// do nothing
	}
}
