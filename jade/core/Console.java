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

public class Console extends JPanel implements Serializable
{
	private static final int tileHeight = 12;
	private static final int tileWidth = tileHeight * 2 / 3;
	private transient Thread mainThread;
	private InputListener listener;
	private TreeMap<Coord, ColoredChar> buffer;
	private TreeMap<Coord, ColoredChar> saved;

	public Console()
	{
		setMainThread();
		listener = new InputListener();
		addKeyListener(listener);
		buffer = new TreeMap<Coord, ColoredChar>();
		saved = new TreeMap<Coord, ColoredChar>();
		setPreferredSize(new Dimension(80 * tileWidth, 24 * tileHeight));
		setFont(new Font(Font.MONOSPACED, Font.PLAIN, tileHeight));
		setBackground(Color.black);
		setFocusable(true);
	}

	public static Console getFramedConsole(String frameTitle)
	{
		JFrame frame = new JFrame(frameTitle);
		Console console = new Console();
		frame.add(console);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		return console;
	}

	public void buffChar(int x, int y, ColoredChar ch)
	{
		buffer.put(new Coord(x, y), ch);
	}
	
	public void buffString(int x, int y, String str, Color color)
	{
		for(char ch : str.toCharArray())
			buffChar(x++, y, new ColoredChar(ch, color));
	}

	public ColoredChar charAt(int x, int y)
	{
		return buffer.get(new Coord(x, y));
	}

	public void saveBuffer()
	{
		saved.clear();
		saved.putAll(buffer);
	}

	public void recallBuffer()
	{
		buffer.clear();
		buffer.putAll(saved);
	}

	public void clearBuffer()
	{
		buffer.clear();
	}

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

	public void setMainThread()
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
