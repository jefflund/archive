package jade.core;

import jade.fov.Camera;
import jade.util.ColoredChar;
import jade.util.Coord;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;

/**
 * GConsole takes a regular Jade Console and adds the capability to use
 * graphical tiles instead of ColoredChar.
 */
public class GConsole extends Console
{
	private Map<Coord, CharPair> imageBuffer;
	private Map<Coord, CharPair> imageSaved;
	private Map<ColoredChar, Image> images;

	public GConsole()
	{
		super(32, 32, 24, 24);
		imageBuffer = new TreeMap<Coord, CharPair>();
		imageSaved = new TreeMap<Coord, CharPair>();
		images = new TreeMap<ColoredChar, Image>();
	}

	public static GConsole getFramedConsole(String frameTitle)
	{
		GConsole console = new GConsole();
		frameConsole(console, frameTitle);
		return console;
	}

	public void buffChar(Coord coord, ColoredChar ch)
	{
		super.buffChar(coord, ch);
		imageBuffer.remove(coord);
	}

	@Override
	public void buffCamera(Camera camera, int x, int y, Coord coord, char ch,
			Color color)
	{
		int offX = x - camera.x();
		int offY = y - camera.y();
		CharPair pair = new CharPair(camera.world().tile(coord).look(),
				new ColoredChar(ch, color));
		imageBuffer.put(coord.getTranslated(offX, offY), pair);
	}

	public void buffCamera(Camera camera, int x, int y)
	{
		int offX = x - camera.x();
		int offY = y - camera.y();
		World world = camera.world();
		for(Coord coord : camera.getFoV())
		{
			Coord off = coord.getTranslated(offX, offY);
			CharPair pair = new CharPair(world.tile(coord).look(), world.look(coord));
			imageBuffer.put(off, pair);
		}
	}

	@Override
	public void saveBuffer()
	{
		super.saveBuffer();
		imageSaved.clear();
		imageSaved.putAll(imageBuffer);
	}

	@Override
	public void recallBuffer()
	{
		super.recallBuffer();
		imageBuffer.clear();
		imageBuffer.putAll(imageSaved);
	}

	@Override
	public void clearBuffer()
	{
		super.clearBuffer();
		imageBuffer.clear();
	}

	public boolean registerImage(String tileSet, int x, int y, char ch,
			Color color)
	{
		try
		{
			BufferedImage tiles = ImageIO.read(new File(tileSet));
			BufferedImage tile = tiles.getSubimage(x * tileHeight, y * tileHeight,
					tileHeight, tileHeight);
			images.put(new ColoredChar(ch, color), tile);
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}

	@Override
	public void paintComponent(Graphics page)
	{
		super.paintComponent(page);
		try
		{
			for(Coord coord : imageBuffer.keySet())
			{
				CharPair pair = imageBuffer.get(coord);
				if(images.containsKey(pair.bgChar))
					page.drawImage(images.get(pair.bgChar), coord.x() * tileWidth, coord
							.y()
							* tileHeight, tileWidth, tileHeight, null);
				if(images.containsKey(pair.fgChar))
					page.drawImage(images.get(pair.fgChar), coord.x() * tileWidth, coord
							.y()
							* tileHeight, tileWidth, tileHeight, null);
				else
				{
					page.setColor(pair.fgChar.color());
					page.drawString(pair.fgChar.toString(), coord.x() * tileWidth, (coord
							.y() + 1)
							* tileHeight);
				}
			}
		}
		catch(ConcurrentModificationException dontWorry)
		{
		}
		catch(NullPointerException dontWorry)
		{
		}
	}

	private class CharPair
	{
		private ColoredChar bgChar;
		private ColoredChar fgChar;

		public CharPair(ColoredChar bgChar, ColoredChar fgChar)
		{
			this.bgChar = bgChar;
			this.fgChar = fgChar;
		}
	}
}
