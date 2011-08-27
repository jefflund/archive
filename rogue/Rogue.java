package rogue;

import jade.core.World;
import jade.ui.TermPanel;
import jade.ui.Terminal;
import jade.util.datatype.ColoredChar;
import java.awt.Color;
import rogue.creature.Monster;
import rogue.creature.Player;
import rogue.level.Level;

public class Rogue
{
    public static void main(String[] args) throws InterruptedException
    {
        Terminal term = TermPanel.getFramedTerminal("Jade Rogue");
        Player player = new Player(term);
        World world = new Level(80, 24, player);
        world.addActor(new Monster(ColoredChar.create('D', Color.red)));

        while(!player.expired())
        {
            term.clearBuffer();
            for(int x = 0; x < world.width(); x++)
                for(int y = 0; y < world.height(); y++)
                    term.bufferChar(x, y, world.look(x, y));
            term.refreshScreen();

            world.tick();
        }

        System.exit(0);
    }
}
