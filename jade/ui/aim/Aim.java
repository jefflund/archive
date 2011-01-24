package jade.ui.aim;

import jade.core.Actor;
import jade.ui.Terminal;
import jade.ui.Terminal.Camera;
import jade.util.ColoredChar;
import jade.util.Coord;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for targeting systems. The targeting uses a Termial to provide IO
 * and a Camera to determine possible targets. The terminal buffer should be
 * unmodified upon return from a call to getAim, although the saved buffer may
 * be modified. Note that some Aim systems may require a non-null target type,
 * and others may not allow one.
 */
public abstract class Aim
{
    protected static final ColoredChar pointer = new ColoredChar('*');

    private final Class<? extends Actor> targetType;

    /**
     * Initializes an Aim with no target type.
     */
    public Aim()
    {
        this(null);
    }

    /**
     * Initializes an Aim with a specific target type. This instance of Aim
     * should only be able to work with tiles that are occupied by an Actor of
     * the specified type.
     * @param targetType the target type of the aim
     */
    public Aim(Class<? extends Actor> targetType)
    {
        this.targetType = targetType;
    }

    /**
     * Returns the location of the target tile, relative to the World of the
     * Camera. All IO should be done relative to this Camera. If there is a
     * non-null target type, this location must be occupied by an Actor of this
     * type. The buffer of the Terminal and the screen should be in the same
     * state at the end of this method as it was at the begining, although the
     * saved buffer has no such guarantee. This is normally accomplished by
     * using saveBuffer and recallBuffer on the Terminal. If at any time the ESC
     * key is pressed, the targeting system should return the screen to its
     * origional state and return null.
     * @param term the Terminal which provides IO for targetings
     * @param camera the camera relative to which targeting takes place
     * @return the location of the target tile
     */
    public abstract Coord getAim(Terminal term, Camera camera);

    /**
     * Returns the target type for this Aim, or null if there is none.
     * @return the target type for this Aim
     */
    public final Class<? extends Actor> targetType()
    {
        return targetType;
    }

    /**
     * Returns all possible tiles that could be returned by getAim, given the
     * target type and a specific instance of Camera.
     * @param camera the camera passed to getAim
     * @return all possible tiles that could be returned by getAim
     */
    protected final List<Coord> getTargets(Camera camera)
    {
        if(targetType == null)
            return new ArrayList<Coord>(camera.getFoV());

        List<Coord> targets = new ArrayList<Coord>();
        for(Coord coord : camera.getFoV())
        {
            if(camera.world().getActorAt(targetType, coord) != null)
                targets.add(coord);
        }
        return targets;
    }
}
