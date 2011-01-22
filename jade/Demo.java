package jade;

import jade.core.Actor;
import jade.core.World;
import jade.fov.FoV;
import jade.fov.Raycast;
import jade.gen.Gen;
import jade.gen.maps.BSP;
import jade.ui.TermPanel;
import jade.ui.Terminal;
import jade.ui.Terminal.Camera;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Direction;
import java.util.Set;

public class Demo
{
    private static class Player extends Actor implements Camera
    {
        private FoV fov;

        public Player()
        {
            super(new ColoredChar('@'));
            fov = new Raycast(false);
        }

        @Override
        public Set<Coord> getFoV()
        {
            return fov.getFoV(world(), pos(), 5);
        }
    }

    public static void main(String[] args)
    {
        Terminal term = TermPanel.getFramedTerm("Jade Rogue");
        World world = new World(80, 24);
        Gen gen = new BSP();
        gen.generate(world);
        Player player = new Player();
        world.addActor(player);
        term.addCamera(player, new Coord(6, 6));

        char key;
        do
        {
            term.clearBuffer();
            term.bufferCamera(player);
            term.bufferRelative(player, player.pos(), player.face());
            term.update();

            key = term.getKey();
            Direction d = Direction.viKeyDir(key);
            if(d != null)
                player.move(d);
        }
        while(key != 'q');

        term.exit();
    }
}
