package jade.ui.aim;

import jade.ui.Terminal;
import jade.ui.Terminal.Camera;
import jade.util.Coord;
import jade.util.Direction;
import java.util.Collection;

/**
 * A targeting system that is free to choose any tile in the field of view of
 * the Camera.
 */
public class Free extends Aim
{
    @Override
    public Coord getAim(Terminal term, Camera camera)
    {
        term.saveBuffer();
        Coord target = new Coord(camera.x(), camera.y());
        Collection<Coord> fov = camera.getFoV();
        char key = '\0';
        while(key != 't')
        {
            // show where current target is
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

            // update pointer as needed
            Direction dir = Direction.viKeyDir(key);
            if(dir != null)
            {
                target.translate(dir);
                if(!fov.contains(target))// can't target what you can't see
                    target.translate(dir.opposite());
            }
        }

        term.recallBuffer();
        term.updateScreen();
        return target;
    }
}
