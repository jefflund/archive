package jade.core;

import jade.util.type.ColoredChar;
import jade.util.type.Coord;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;

/**
 * Adds tile support to a console
 */
public class GConsole extends Console
{
	private Map<ColoredChar, Image> images;
	private Map<Coord, List<ColoredChar>> tilesFromLook;
	private Semaphore tilesLock;
	private Map<Coord, List<ColoredChar>> tilesFromLookSaved;
	private int tileSetSize;

	/**
	 * Constructs a default GConsole. The tiles are set to 32x32 and the console
	 * is initially 20x20
	 */
	public GConsole()
	{
		this(32, 20, 20, 32);
	}

	/**
	 * Constructs a GConsole with the given size. The tileSize represents the size
	 * of each tile on the screen, while tileSetSize is the size of the tiles in
	 * the tiles sets used.
	 */
	public GConsole(int tileSize, int width, int height, int tileSetSize)
	{
		super(tileSize, tileSize, width, height);
		images = new TreeMap<ColoredChar, Image>();
		tilesFromLook = new TreeMap<Coord, List<ColoredChar>>();
		tilesLock = new Semaphore(1);
		tilesFromLookSaved = new TreeMap<Coord, List<ColoredChar>>();
		this.tileSetSize = tileSetSize;
	}

	private void aquireBuffer()
	{
		try
		{
			tilesLock.acquire();
		}
		catch(InterruptedException exception)
		{
			exception.printStackTrace();
		}
	}

	private void releaseBuffer()
	{
		tilesLock.release();
	}

	/**
	 * Returns a default GConsole embedded in a JFrame
	 */
	public static GConsole getFramedConsole(String frameTitle)
	{
		GConsole console = new GConsole();
		frameConsole(console, frameTitle);
		return console;
	}

	/**
	 * Returns a custom GConsole embedded in a JFrame
	 */
	public static GConsole getFramedConsole(String frameTitle, int tileSize,
			int width, int height, int tileSetSize)
	{
		GConsole console = new GConsole(tileSize, width, height, tileSetSize);
		frameConsole(console, frameTitle);
		return console;
	}

	public void buffCamera(Camera camera)
	{
		Coord center = cameras.get(camera);
		int offX = center.x() - camera.x();
		int offY = center.y() - camera.y();
		aquireBuffer();
		for(Coord coord : camera.getFoV())
			tilesFromLook.put(coord.getTranslated(offX, offY), camera.world()
					.lookAll(coord));
		releaseBuffer();
	}

	public void clearBuffer()
	{
		super.clearBuffer();
		aquireBuffer();
		tilesFromLook.clear();
		releaseBuffer();
	}

	public void saveBuffer()
	{
		super.saveBuffer();
		aquireBuffer();
		tilesFromLookSaved.clear();
		tilesFromLookSaved.putAll(tilesFromLook);
		releaseBuffer();
	}

	public void recallBuffer()
	{
		super.recallBuffer();
		aquireBuffer();
		tilesFromLook.clear();
		tilesFromLook.putAll(tilesFromLookSaved);
		releaseBuffer();
	}

	public void clearLine(int y)
	{
		super.clearLine(y);
		Set<Coord> inline = new HashSet<Coord>();
		for(Coord coord : tilesFromLook.keySet())
			if(coord.y() == y)
				inline.add(coord);
		for(Coord coord : inline)
			tilesFromLook.remove(coord);
		aquireBuffer();
	}

	/**
	 * Registers a tile. When ever buffCamera is called, the given ColoredChar
	 * will be replaced with the tile specified here.
	 */
	public boolean registerImage(String tileSet, int x, int y, char ch,
			Color color)
	{
		try
		{
			BufferedImage tiles = ImageIO.read(new File(tileSet));
			BufferedImage tile = tiles.getSubimage(x * tileSetSize, y * tileSetSize,
					tileSetSize, tileSetSize);
			images.put(new ColoredChar(ch, color), tile);
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}

	protected void paintComponent(Graphics page)
	{
		super.paintComponent(page);
		aquireBuffer();
		for(Coord coord : tilesFromLook.keySet())
		{
			List<ColoredChar> look = tilesFromLook.get(coord);
			for(ColoredChar ch : look)
				drawTile(ch, coord, page);
		}
		releaseBuffer();
	}

	private void drawTile(ColoredChar ch, Coord coord, Graphics page)
	{
		if(images.containsKey(ch))
		{
			page.drawImage(images.get(ch), coord.x() * tileWidth, coord.y()
					* tileHeight, tileWidth, tileHeight, null);
		}
		else
		{
			page.setColor(ch.color());
			page.drawString(ch.toString(), coord.x() * tileWidth, (coord.y() + 1)
					* tileHeight);
		}
	}
}
