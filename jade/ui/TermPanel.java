package jade.ui;

import jade.util.datatype.ColoredChar;
import jade.util.datatype.Coordinate;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Implements a {@code Terminal} on a {@code JPanel}, which can then be embedded into any container
 * able to use a {@code JPanel}.
 */
public class TermPanel extends Terminal
{
    private Screen screen;

    /**
     * Constructs a new {@code TermPanel} with the given dimensions. Note that the rows and columns
     * can be changed by resizing the underlying JPanel, but font size is fixed.
     * @param columns the default number of columns to display
     * @param rows the default number of rows to display
     * @param fontSize the size of each tile
     */
    public TermPanel(int columns, int rows, int fontSize)
    {
        screen = new Screen(columns, rows, fontSize);
    }

    /**
     * Constructs a new {@code TermPanel} with the default dimensions. There will be 80 columns, 24
     * rows, and a font size of 12.
     */
    public TermPanel()
    {
        this(80, 24, 12);
    }

    /**
     * Constructs and returns a new {@code TermPanel} with default dimensions, which is placed
     * inside a {@code JFrame}. The {@code TermPanel} will initially have 80 columns, 24 rows, and a
     * font size of 12.
     * @param title the title of the {@code JFrame}
     * @return a new {@code TermPanel} with default dimensions.
     */
    public static TermPanel getFramedTerminal(String title)
    {
        TermPanel term = new TermPanel();
        JFrame frame = new JFrame(title);
        frame.add(term.panel());
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        return term;
    }

    /**
     * Returns the underlying {@code JPanel} display of the {@code TermPanel}. This {@code JPanel}
     * can then be embedded in any other container like a normal {@code JPanel}.
     * @return the underlying {@code JPanel} display of the {@code TermPanel}
     */
    public JPanel panel()
    {
        return screen;
    }

    @Override
    public char getKey() throws InterruptedException
    {
        return screen.consumeKeyPress();
    }

    @Override
    protected void drawScreen(Map<Coordinate, ColoredChar> buffer)
    {
        screen.setBuffer(buffer);
        screen.repaint();
    }

    private class Screen extends JPanel implements KeyListener
    {
        private static final long serialVersionUID = 7219226976524388778L;

        private int tileWidth;
        private int tileHeight;
        private BlockingQueue<Character> inputBuffer;
        private Map<Coordinate, ColoredChar> screenBuffer;

        public Screen(int columns, int rows, int fontSize)
        {
            inputBuffer = new LinkedBlockingQueue<Character>();
            screenBuffer = new HashMap<Coordinate, ColoredChar>();

            addKeyListener(this);
            tileWidth = fontSize * 3 / 4;
            tileHeight = fontSize;
            setPreferredSize(new Dimension(columns * tileWidth, rows * tileHeight));
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
            setBackground(Color.black);
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics page)
        {
            super.paintComponent(page);
            synchronized(screenBuffer)
            {
                for(Coordinate coord : screenBuffer.keySet())
                {
                    ColoredChar ch = screenBuffer.get(coord);
                    int x = tileWidth * coord.x();
                    int y = tileHeight * (coord.y() + 1);

                    page.setColor(ch.color());
                    page.drawString(ch.toString(), x, y);
                }
            }
        }

        public void setBuffer(Map<Coordinate, ColoredChar> buffer)
        {
            synchronized(screenBuffer)
            {
                screenBuffer.clear();
                screenBuffer.putAll(buffer);
            }
        }

        @Override
        public void keyPressed(KeyEvent event)
        {
            inputBuffer.offer(event.getKeyChar());
        }

        public char consumeKeyPress() throws InterruptedException
        {
            return inputBuffer.take();
        }

        @Override
        public void keyReleased(KeyEvent e)
        {}

        @Override
        public void keyTyped(KeyEvent e)
        {}
    }
}
