package jade.ui;

import jade.util.ColoredChar;
import jade.util.Coord;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Implements a Terminal through a JPanel, which can be embedded in any
 * container.
 */
public class TermPanel extends Terminal
{
    /**
     * The default tile size
     */
    public static final int DEFAULT_TILE = 12;
    /**
     * The default number of columns, which corresponds to the size of old
     * school terminals.
     */
    public static final int DEFAULT_COLS = 80;
    /**
     * The default number of rows, which corresponds to the size of old school
     * terminals.
     */
    public static final int DEFAULT_ROWS = 24;

    private Screen screen;

    /**
     * Constructs a new TermPanel, with the default tile size of 8x12 and a
     * default screen size of 80x24.
     */
    public TermPanel()
    {
        screen = new Screen(DEFAULT_TILE, DEFAULT_COLS, DEFAULT_ROWS);
    }

    /**
     * Constructs a new TermPanel, with custom dimension.
     * @param tileSize the size of each tile
     * @param cols the number of columns initially shown
     * @param rows the number of rows initially shown
     */
    public TermPanel(int tileSize, int cols, int rows)
    {
        screen = new Screen(tileSize, cols, rows);
    }

    /**
     * Returns a new TermPanel with the default dimensions, placed inside a
     * JFrame, which has the given frame title.
     * @param frameTitle the title of the JFrame
     * @return a new TermPanel, with the default dimensions
     */
    public static TermPanel getFramedTerm(String frameTitle)
    {
        return getFramedTerm(DEFAULT_TILE, DEFAULT_COLS, DEFAULT_ROWS,
                frameTitle);
    }

    /**
     * Returns a new TermPanel with custom dimensions, placed inside a JFrame,
     * which has been given frame title.
     * @param tileSize the size of each tile
     * @param cols the number of columns initially shown
     * @param rows the number of rows initially shown
     * @param frameTitle the title of the JFrame
     * @return a new TermPanel, with the custome dimensions
     */
    public static TermPanel getFramedTerm(int tileSize, int cols, int rows,
            String frameTitle)
    {
        TermPanel term = new TermPanel(tileSize, cols, rows);
        JFrame frame = new JFrame(frameTitle);
        frame.add(term.screen);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        return term;
    }

    /**
     * Returns the JPanel which is the Terminal screen.
     * @return the JPanel which is the screen
     */
    public JPanel getScreen()
    {
        return screen;
    }

    @Override
    public char getKey()
    {
        synchronized(screen.inputBuffer)
        {
            try
            {
                screen.inputBuffer.wait();
                return screen.inputBuffer.remove();
            }
            catch(InterruptedException e)
            {
                return 0;
            }
        }
    }

    @Override
    public void updateScreen()
    {
        screen.repaint();
        try
        {
            synchronized(screen)
            {
                screen.wait();
            }
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private class Screen extends JPanel implements KeyListener
    {
        public Queue<Character> inputBuffer;

        private int tileWidth;
        private int tileHeight;

        public Screen(int tileSize, int cols, int rows)
        {
            tileHeight = tileSize;
            tileWidth = tileHeight * 3 / 4;
            inputBuffer = new LinkedList<Character>();
            addKeyListener(this);
            setPreferredSize(new Dimension(cols * tileWidth, rows * tileHeight));
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, tileHeight));
            setBackground(Color.black);
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics page)
        {
            super.paintComponent(page);
            for(Coord coord : screenBuffer.keySet())
            {
                ColoredChar ch = screenBuffer.get(coord);
                page.setColor(ch.color());
                page.drawString(ch.toString(), tileWidth * coord.x(),
                        tileHeight * (coord.y() + 1));
            }
            synchronized(this)
            {
                notify();
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

        @Override
        public void keyReleased(KeyEvent event)
        {
            // noop
        }

        @Override
        public void keyTyped(KeyEvent event)
        {
            // noop
        }
    }
}
