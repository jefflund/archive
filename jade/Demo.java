package jade;

import jade.core.World;
import jade.fov.FoV;
import jade.fov.Raycast;
import jade.gen.Gen;
import jade.gen.maps.BSP;
import jade.ui.TermPanel;
import jade.ui.Terminal;
import jade.util.Coord;
import jade.util.Dice;
import java.awt.Color;

public class Demo
{
    public static void main(String[] args)
    {
        Terminal term = TermPanel.getFramedTerm("Jade Rogue");
        World world = new World(80, 24);
        Gen gen = new BSP();
        FoV fov = new Raycast(false);
        do
        {
            gen.generate(world, Dice.global);
            for(int x = 0; x < 80; x++)
                for(int y = 0; y < 24; y++)
                    term.bufferChar(x, y, world.tileAt(x, y));

            Coord pos = world.getOpenTile();
            for(Coord vis : fov.getFoV(world, pos, 10))
                term.bufferChar(vis, world.tileAt(vis).ch(), Color.red);
            term.bufferChar(pos, '@', Color.red);
            term.update();
        }
        while(term.getKey() != 'q');
        term.exit();
    }
}
