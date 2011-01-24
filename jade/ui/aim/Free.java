package jade.ui.aim;

import jade.ui.Terminal;
import jade.ui.Terminal.Camera;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Direction;
import java.util.Collection;

/**
 * A targeting system that is free to choose any tile in the field of view of
 * the Camera.
 */
public class Free extends Aim
{
    private static final ColoredChar targetCh = new ColoredChar('*');
    
    @Override
    public Coord getAim(Terminal term, Camera camera)
    {
        term.saveBuffer();
        Coord target = new Coord(camera.x(), camera.y());
        Collection<Coord> fov = camera.getFoV();
        char key = '\0';
        while(key != 't')
        {
            term.recallBuffer();
            term.bufferRelative(camera, target, targetCh);
            term.updateScreen();

            key = term.getKey();
            if(key == Terminal.ESC)
            {
                term.recallBuffer();
                term.updateScreen();
                return null;
            }

            Direction dir = Direction.viKeyDir(key);
            if(dir != null)
            {
                target.translate(dir);
                if(!fov.contains(target))
                    target.translate(dir.opposite());
            }
        }

        term.recallBuffer();
        term.updateScreen();
        return target;
    }
}
