package jade.ui.aim;

import jade.core.Actor;
import jade.ui.Terminal;
import jade.ui.Terminal.Camera;
import jade.util.ColoredChar;
import jade.util.Coord;
import java.util.List;

/**
 * Replaces all possible targets with a letter and allows the user to select
 * one. Invalid choices return null.
 */
public class Letter extends Aim
{
    /**
     * Initializes Letter with a given target type
     * @param cls the target type for this Aim
     */
    public Letter(Class<? extends Actor> cls)
    {
        super(cls);
        if(cls == null)
            throw new IllegalStateException("Letter must have target type");
    }

    @Override
    public Coord getAim(Terminal term, Camera camera)
    {
        term.saveBuffer();
        List<Coord> targets = getTargets(camera);

        // on screen, replace each target with letter corresponding to
        // index in target list (ie 1=a, 2=b...)
        for(int i = 0; i < targets.size(); i++)
        {
            ColoredChar ch = new ColoredChar((char)('a' + i));
            term.bufferRelative(camera, targets.get(i), ch);
        }
        term.updateScreen();

        int choice = term.getKey() - 'a'; // reverse int to char mapping
        term.recallBuffer();
        term.updateScreen();

        if(choice < 0 || choice >= targets.size())
            return null;
        else
            return targets.get(choice);
    }
}
