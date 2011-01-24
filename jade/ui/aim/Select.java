package jade.ui.aim;

import jade.core.Actor;
import jade.ui.Terminal;
import jade.ui.Terminal.Camera;
import jade.util.Coord;
import jade.util.Direction;
import java.util.List;

public class Select extends Aim
{
    public Select(Class<? extends Actor> cls)
    {
        super(cls);
        if(cls == null)
            throw new IllegalStateException("Letter must have target type");
    }

    @Override
    public Coord getAim(Terminal term, Camera camera)
    {

        List<Coord> targets = getTargets(camera);
        if(targets.isEmpty())
            return null;
        term.saveBuffer();
        char key = '\0';
        Coord target = targets.get(0);
        while(key != 't')
        {
            term.recallBuffer();
            term.bufferRelative(camera, target, pointer);
            term.updateScreen();
            key = term.getKey();
            if(key == Terminal.ESC)
            {
                term.recallBuffer();
                term.updateScreen();
                return null;
            }
            Direction dir = Direction.keyDir(key);
            if(dir != null)
            {
                Coord newTarget = null;
                for(Coord coord : targets)
                {
                    if(coord.equals(target)
                            || (dir.dx() < 0 && coord.x() > target.x())
                            || (dir.dx() > 0 && coord.x() < target.x())
                            || (dir.dy() < 0 && coord.y() > target.y())
                            || (dir.dy() > 0 && coord.y() < target.y()))
                        continue;
                    if(newTarget == null
                            || (target.distCart(coord) < target
                                    .distCart(newTarget)))
                        newTarget = coord;
                }
                if(newTarget != null)
                    target = newTarget;
            }
        }
        term.recallBuffer();
        term.updateScreen();
        return target;
    }
}
