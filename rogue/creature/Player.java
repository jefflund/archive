package rogue.creature;

import jade.ui.Terminal;
import jade.util.datatype.ColoredChar;
import jade.util.datatype.Direction;

public class Player extends Creature
{
    private Terminal term;

    public Player(Terminal term)
    {
        super(ColoredChar.create('@'));
        this.term = term;
    }

    @Override
    public void act()
    {
        try
        {
            char key;
            key = term.getKey();
            switch(key)
            {
                case 'q':
                    expire();
                    break;
                default:
                    Direction dir = Direction.keyToDir(key);
                    if(dir != null)
                        move(dir);
                    break;
            }
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
