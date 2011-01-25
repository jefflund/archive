package jade;

import jade.core.Actor;
import jade.core.World;
import jade.fov.FoV;
import jade.fov.Raycast;
import jade.gen.Gen;
import jade.gen.maps.Cellular;
import jade.ui.TermPanel;
import jade.ui.Terminal;
import jade.ui.Terminal.Camera;
import jade.util.ColoredChar;
import jade.util.Coord;
import jade.util.Direction;
import java.util.Set;

/**
 * A simple demo of some Jade Rogue features.
 */
public class Demo
{
    /**
     * Runs the demo.
     * @param args command line args - nothing is done with them
     */
    public static void main(String[] args)
    {
        Terminal term = TermPanel.getFramedTerm("Jade Rogue");

        World world = new Level();
        Gen gen = new Cellular();

        do
        {
            gen.generate(world);// world generation is pretty easy too!
            for(int x = 0; x < world.width(); x++)
                for(int y = 0; y < world.height(); y++)
                    term.bufferChar(x, y, world.look(x, y));
            term.updateScreen();
        }
        while(term.getKey() != 'q');

        Player player = new Player(term);
        world.addActor(player);// now the player is in the World
        term.addCamera(player, new Coord(6, 6));

        // most game loops will look like this
        while(!player.expired())
        {
            // update the screen
            term.clearBuffer();
            term.bufferCamera(player);
            term.updateScreen();

            // perform game logic
            world.tick();
        }

        term.exit();
    }

    private static class Player extends Actor implements Camera
    {
        private FoV fov;
        private Terminal term;
        private static final int view = 5;

        public Player(Terminal term)
        {
            super(new ColoredChar('@'));
            fov = new Raycast(false);
            this.term = term;
        }

        @Override
        public void act()
        {
            // act is where the Actor gets its logic
            // this does mean just players and monsters
            // but spells, items, timers, etc
            char key = term.getKey();
            switch(key)
            {
            case 'q':
                expire();// mark the actor for removal
                break;
            default:
                Direction dir = Direction.keyDir(key);
                if(dir != null)
                    move(dir);
            }
        }

        @Override
        public Set<Coord> getFoV()
        {
            // field of view generation is super easy!!
            return fov.getFoV(world(), pos(), view);
        }
    }

    private static class Level extends World
    {
        public Level()
        {
            super(80, 24);
        }

        @Override
        public void tick()
        {
            // the basic tick method has this structure
            // just call variaents of getActor(s) and have them act
            for(Player actor : getActors(Player.class))
                actor.act();
            // this removes everyone who was marked for removal
            removeExpired();
        }
    }
}
