package jade.core;

import jade.util.ColoredChar;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class TileConsole extends Console
{
	private Map<ColoredChar, Image> images;

	public TileConsole()
	{
		this(32, 80, 24);
	}

	public TileConsole(int tileSize, int width, int height)
	{
		super(tileSize, tileSize, width, height);
		images = new TreeMap<ColoredChar, Image>();
	}

	public static TileConsole getFramedConsole(String frameTitle)
	{
		TileConsole console = new TileConsole();
		frameConsole(console, frameTitle);
		return console;
	}

	public static TileConsole getFramedConsole(String frameTitle, int tileSize,
			int width, int height)
	{
		TileConsole console = new TileConsole(tileSize, width, height);
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

	public boolean registerImage(String tileSet, int x, int y, char ch, Color color)
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
		super.paintComponent(new JadeGraphics(page));
	}

	private class JadeGraphics extends Graphics
	{
		private Graphics page;

		public JadeGraphics(Graphics page)
		{
			this.page = page;
		}

		@Override
		public void drawString(String str, int x, int y)
		{
			if(str.length() == 1)
			{
				ColoredChar ch = new ColoredChar(str.charAt(0), getColor());
				if(images.containsKey(ch))
				{
					page.drawImage(images.get(ch), x, y - tileHeight, tileWidth,
							tileHeight, Color.white, null);
					return;
				}
			}
			page.drawString(str, x, y);
		}

		@Override
		public void clearRect(int x, int y, int width, int height)
		{
			page.clearRect(x, y, width, height);
		}

		@Override
		public void clipRect(int x, int y, int width, int height)
		{
			page.clearRect(x, y, width, height);
		}

		@Override
		public void copyArea(int x, int y, int width, int height, int dx, int dy)
		{
			page.copyArea(x, y, width, height, dx, dy);
		}

		@Override
		public Graphics create()
		{
			return page.create();
		}

		@Override
		public void dispose()
		{
			page.dispose();
		}

		@Override
		public void drawArc(int x, int y, int width, int height, int startAngle,
				int arcAngle)
		{
			page.drawArc(x, y, width, height, startAngle, arcAngle);
		}

		@Override
		public boolean drawImage(Image img, int x, int y, ImageObserver observer)
		{
			return page.drawImage(img, x, y, observer);
		}

		@Override
		public boolean drawImage(Image img, int x, int y, Color bgcolor,
				ImageObserver observer)
		{
			return page.drawImage(img, x, y, bgcolor, observer);
		}

		@Override
		public boolean drawImage(Image img, int x, int y, int width, int height,
				ImageObserver observer)
		{
			return page.drawImage(img, x, y, width, height, observer);
		}

		@Override
		public boolean drawImage(Image img, int x, int y, int width, int height,
				Color bgcolor, ImageObserver observer)
		{
			return page.drawImage(img, x, y, width, height, bgcolor, observer);
		}

		@Override
		public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
				int sx1, int sy1, int sx2, int sy2, ImageObserver observer)
		{
			return page.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
					observer);
		}

		@Override
		public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
				int sx1, int sy1, int sx2, int sy2, Color bgcolor,
				ImageObserver observer)
		{
			return page.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
					bgcolor, observer);
		}

		@Override
		public void drawLine(int x1, int y1, int x2, int y2)
		{
			page.drawLine(x1, y1, x2, y2);
		}

		@Override
		public void drawOval(int x, int y, int width, int height)
		{
			page.drawOval(x, y, width, height);
		}

		@Override
		public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints)
		{
			page.drawPolygon(xPoints, yPoints, nPoints);
		}

		@Override
		public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints)
		{
			page.drawPolyline(xPoints, yPoints, nPoints);
		}

		@Override
		public void drawRoundRect(int x, int y, int width, int height,
				int arcWidth, int arcHeight)
		{
			page.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
		}

		@Override
		public void drawString(AttributedCharacterIterator iterator, int x, int y)
		{
			page.drawString(iterator, x, y);
		}

		@Override
		public void fillArc(int x, int y, int width, int height, int startAngle,
				int arcAngle)
		{
			page.fillArc(x, y, width, height, startAngle, arcAngle);
		}

		@Override
		public void fillOval(int x, int y, int width, int height)
		{
			page.fillOval(x, y, width, height);
		}

		@Override
		public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints)
		{
			page.fillPolygon(xPoints, yPoints, nPoints);
		}

		@Override
		public void fillRect(int x, int y, int width, int height)
		{
			page.fillRect(x, y, width, height);
		}

		@Override
		public void fillRoundRect(int x, int y, int width, int height,
				int arcWidth, int arcHeight)
		{
			page.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
		}

		@Override
		public Shape getClip()
		{
			return page.getClip();
		}

		@Override
		public Rectangle getClipBounds()
		{
			return getClipBounds();
		}

		@Override
		public Color getColor()
		{
			return page.getColor();
		}

		@Override
		public Font getFont()
		{
			return page.getFont();
		}

		@Override
		public FontMetrics getFontMetrics(Font f)
		{
			return page.getFontMetrics(f);
		}

		@Override
		public void setClip(Shape clip)
		{
			page.setClip(clip);
		}

		@Override
		public void setClip(int x, int y, int width, int height)
		{
			page.setClip(x, y, width, height);
		}

		@Override
		public void setColor(Color c)
		{
			page.setColor(c);
		}

		@Override
		public void setFont(Font font)
		{
			page.setFont(font);
		}

		@Override
		public void setPaintMode()
		{
			page.setPaintMode();
		}

		@Override
		public void setXORMode(Color c1)
		{
			page.setXORMode(c1);
		}

		@Override
		public void translate(int x, int y)
		{
			page.translate(x, y);
		}
	}
}
