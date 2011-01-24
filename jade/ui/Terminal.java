package jade.ui;

import jade.core.World;
import jade.util.ColoredChar;
import jade.util.Coord;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An abstract class that represents a terminal. Features a screen buffer, the
 * ability to save and recall a single screen buffer, and the ability to capture
 * key presses.
 */
public abstract class Terminal
{
    protected final Map<Coord, ColoredChar> screenBuffer;
    protected final Map<Coord, ColoredChar> savedBuffer;
    protected final Map<Camera, Coord> cameras;

    /**
     * Constructs a terminal with an empty buffer.
     */
    public Terminal()
    {
        screenBuffer = new HashMap<Coord, ColoredChar>();
        savedBuffer = new HashMap<Coord, ColoredChar>();
        cameras = new HashMap<Camera, Coord>();
    }

    /**
     * Buffers a ColoredChar at the given position on the screen.
     * @param pos the position on screen
     * @param ch the ColoredChar to be buffered
     */
    public void bufferChar(Coord pos, ColoredChar ch)
    {
        screenBuffer.put(pos, ch);
    }

    /**
     * Buffers a ColoredChar as specified by ch and color at (x, y).
     * @param x the x value of the position on screen
     * @param y the y value of the position on screen
     * @param ch the character value to be buffered
     * @param color the color of the character to be buffered
     */
    public final void bufferChar(int x, int y, char ch, Color color)
    {
        bufferChar(new Coord(x, y), new ColoredChar(ch, color));
    }

    /**
     * Buffers a ColoredChar as specified by ch and color at the given position.
     * @param pos the position on screen
     * @param ch the character value to be buffered
     * @param color the color of the character to be buffered
     */
    public final void bufferChar(Coord pos, char ch, Color color)
    {
        bufferChar(pos, new ColoredChar(ch, color));
    }

    /**
     * Buffers a ColoredChar at (x, y).
     * @param x the x value of the position on screen
     * @param y the y value of the position on screen
     * @param ch the ColoredChar to be buffered
     */
    public final void bufferChar(int x, int y, ColoredChar ch)
    {
        bufferChar(new Coord(x, y), ch);
    }

    /**
     * Returns the contents of the buffer at the given coordinate
     * @param coord the location being queried
     * @return the contents of the buffer at coord
     */
    public ColoredChar charAt(Coord coord)
    {
        return savedBuffer.get(coord);
    }

    /**
     * Returns the contents of the buffer at (x, y).
     * @param x the x value of the location being queried
     * @param y the y value of the location being queried
     * @return the contents of the buffer at (x, y)
     */
    public final ColoredChar charAt(int x, int y)
    {
        return charAt(new Coord(x, y));
    }

    /**
     * Clears the contents of the buffer.
     */
    public void clearBuffer()
    {
        screenBuffer.clear();
    }

    /**
     * Saves the contents of the buffer for recall.
     */
    public void saveBuffer()
    {
        savedBuffer.clear();
        savedBuffer.putAll(screenBuffer);
    }

    /**
     * Restores the last saved buffer.
     */
    public void recallBuffer()
    {
        screenBuffer.clear();
        screenBuffer.putAll(savedBuffer);
    }

    /**
     * Updates the screen to reflect the current state of the buffer. Without a
     * call to this method, there is no guarantee that the screen will be
     * updated to reflect the screen buffer.
     */
    public abstract void updateScreen();

    /**
     * Returns the next key press, blocking until there is one.
     * @return
     */
    public abstract char getKey();

    /**
     * An alias for System.exit(0).
     */
    public void exit()
    {
        System.exit(0);
    }

    /**
     * Represents anything which can observe a World. Note that an Actor with an
     * added getFoV method can easily implement Camera. The Terminal then has
     * methods to draw what the camera sees in a particular location on screen.
     */
    public interface Camera
    {
        /**
         * Returns the x value of the center of the camera on the World
         * @return the x value of the center of the camera on the World
         */
        public int x();

        /**
         * Returns the y value of the center of the camera on the World
         * @return the y value of the center of the camera on the World
         */
        public int y();

        /**
         * Returns the World the camera is observing
         * @return the World the camera is observing
         */
        public World world();

        /**
         * Returns the set of locations on the World the Camera can see
         * @return the set of locations on the World the Camera can see
         */
        public Set<Coord> getFoV();
    }

    /**
     * Registers a camera with an associtated screen center. When ever the
     * Terminal is asked to draw the camera, it will place the center of the
     * field of vision for the Camera at this screen center.
     * @param camera the camera being registered
     * @param screenCenter the on screen center of the cameras field of view
     */
    public void addCamera(Camera camera, Coord screenCenter)
    {
        cameras.put(camera, screenCenter);
    }

    /**
     * Removes a Camera from the Camera register.
     * @param camera the Camera being removed
     */
    public void removeCamera(Camera camera)
    {
        cameras.remove(camera);
    }

    /**
     * Buffers everything the camera can see, with the center of the field of
     * view for the Camera being placed at the registered on screen center.
     * @param camera the camera which is being buffered
     */
    public void bufferCamera(Camera camera)
    {
        Coord center = cameras.get(camera);
        int offX = center.x() - camera.x();
        int offY = center.y() - camera.y();
        for(Coord coord : camera.getFoV())
            bufferChar(coord.translated(offX, offY), camera.world().look(coord));
    }

    /**
     * Buffers the contents of every registered camera on screen.
     */
    public void bufferCameras()
    {
        for(Camera camera : cameras.keySet())
            bufferCamera(camera);
    }

    /**
     * Buffers a ColoredChar relative to a Camera. The ColoredChar will appear
     * on screen where it would if the Camera where seeing it in the World at
     * the specified position.
     * @param camera the Camera relative to which ch is buffered
     * @param pos the position in the World of the Camera which ch is seen
     * @param ch the ColoredChar to buffered
     */
    public void bufferRelative(Camera camera, Coord pos, ColoredChar ch)
    {
        Coord center = cameras.get(camera);
        int offX = center.x() - camera.x();
        int offY = center.y() - camera.y();
        bufferChar(pos.translated(offX, offY), ch);
    }

    public static final char ESC = 27;
}
