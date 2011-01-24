package jade.ui.aim;

import java.util.Set;
import jade.core.Actor;
import jade.ui.Terminal;
import jade.ui.Terminal.Camera;
import jade.util.Coord;
import jade.util.Direction;

/**
 * Allows the user to select the closest valid target in a cardinal direction.
 */
public class Directional extends Aim
{

    public Directional(Class<? extends Actor> cls)
    {
        super(cls);
        if(cls == null)
            throw new IllegalStateException("Letter must have target type");
    }

    @Override
    public Coord getAim(Terminal term, Camera camera)
    {
        Direction dir = Direction.keyDir(term.getKey());
        if(dir == null || dir == Direction.ORIGIN)
            return null;
        Coord target = new Coord(camera.x(), camera.y());
        Set<Coord> fov = camera.getFoV();
        while(fov.contains(target))
        {
            target.translate(dir);
            if(camera.world().getActorAt(targetType(), target) != null)
                return target;
        }
        return null;
    }

}
