package jade.ui.aim;

import jade.core.Actor;
import jade.ui.Terminal;
import jade.ui.Terminal.Camera;
import jade.util.Coord;

/**
 * Automatically selects the location of the closest allowed target, excluding
 * the location of the Camera itself.
 */
public class Closest extends Aim
{
    /**
     * Initializes Closest with a given target type
     * @param cls the target type for this Aim
     */
    public Closest(Class<? extends Actor> cls)
    {
        super(cls);
        if(cls == null)
            throw new IllegalStateException("Letter must have target type");
    }

    @Override
    public Coord getAim(Terminal term, Camera camera)
    {
        double bestDist = Double.POSITIVE_INFINITY;
        Coord bestTarget = null;
        Coord pos = new Coord(camera.x(), camera.y());
        for(Coord target : getTargets(camera))
        {
            double dist = pos.distCart(target);
            if(dist < bestDist && dist > 0)
            {
                bestDist = dist;
                bestTarget = target;
            }
        }
        return bestTarget;
    }

}
