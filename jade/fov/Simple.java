package jade.fov;

import jade.core.World;
import jade.util.type.Coord;
import java.util.HashSet;
import java.util.Set;

/**
 * This field of vision algorithm attempts to replicate the field of vision fron
 * the original rogue. That is to say, if you are in a room, the whole room will
 * be lit (except in our case this is limited to specified range). Otherwise you
 * see only the tiles immediatly adjacent to the viewer. This algorithm should
 * only be applied on worlds in which the rooms are rectangular with corridorrs
 * of width 1.
 */
public class Simple implements FoV
{
    @Override
    public Set<Coord> getFov(World world, Coord orig, int range)
    {
        Set<Coord> fov = new HashSet<Coord>();
        for(int dx = -1; dx <= 1; dx++)
            for(int dy = -1; dy <= 1; dy++)
                fov.add(orig.getTranslated(dx, dy));
        return fov;
    }
}
