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
import jade.util.Dice;
import jade.util.Direction;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
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
        // console emulation is easy in Jade
        // check out the Terminal API for more info
        Terminal term = TermPanel.getFramedTerm("Jade Rogue Demo");
        term.bufferString(6, 6, "Welcome to Jade Rogue!!!!");
        term.getKey();

        // world generation is really easy too
        // check out the gen package for other generation algorithms
        World world = new Level();
        Gen gen = new BSP();
        gen.generate(world);

        // Players are a type of Actor
        // check out the core package for more on this
        Player player = new Player(term);
        world.addActor(player);
        term.addCamera(player, new Coord(6, 6));

        // Add a few zombies that move randomly
        for(int i = 0; i < 20; i++)
            world.addActor(new Zombie());

        // most game loops will look like this
        while(!player.expired())
        {
            // update the screen
            // can also do things like bufferChar, but this saves us some code
            term.clearBuffer();
            term.bufferCamera(player);
            term.updateScreen();

            // perform game logic
            // tick should handle moving and updating all Actors
            // look in the core package for more
            world.tick();
        }

        term.exit();
    }

    // Our player is an Actor, but note that Actors can represent pretty much
    // anything - spells, items, effects, timers, traps, monsters, etc.
    private static class Player extends Actor implements Camera
    {
        // field of view generation - see getFoV
        private FoV fov;
        // Terminal so the player's keyPresses can be used
        private Terminal term;
        // parameter to field of view
        private static final int viewRange = 5;
        // for zombies
        private int brainLeft;
        private boolean infected;

        public Player(Terminal term)
        {
            super(new ColoredChar('@'));
            fov = new Raycast(false);
            this.term = term;
            brainLeft = 100;
            infected = false;
        }

        @Override
        public void act()
        {
            // be a zombie if no brains are left...
            if(brainLeft < 0)
            {
                move(Dice.global.nextDir());// move brainlessly
                sleep();
                char key = term.tryGetKey();
                if(key == 'q' || key == Terminal.ESC)
                    expire();
                return;
            }

            // act is where the Actor gets its logic
            // this does mean just players and monsters
            // but spells, items, timers, etc
            char key = term.getKey();
            switch(key)
            {
            case Terminal.ESC:
            case 'q':
                expire();// mark the actor for removal
                break;
            default:
                Direction dir = Direction.keyDir(key);
                if(dir != null)
                    move(dir);// see setPos below
            }

            // zombie stuff
            if(infected)
            {
                if(brainLeft == 50)
                    setFace(new ColoredChar('@', Color.gray));
                if(brainLeft == 40)
                    setFace(new ColoredChar('@', Color.magenta));
                if(brainLeft == 30)
                    setFace(new ColoredChar('@', Color.yellow));
                if(brainLeft == 20)
                    setFace(new ColoredChar('@', Color.orange));
                if(brainLeft == 10)
                    setFace(new ColoredChar('@', Color.red));
                if(brainLeft == 0)
                {
                    setFace(new ColoredChar('Z', Color.red));
                    term.bufferString(0, 2 * viewRange, "YOU'RE A ZOMBIE!");
                    term.updateScreen();
                    term.getKey();
                }
                brainLeft--;
            }
        }

        private void sleep()
        {
            try
            {
                Thread.sleep(250);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public Set<Coord> getFoV()
        {
            // field of view generation is super easy!!
            // check out the fov package for more info
            return fov.getFoV(world(), pos(), viewRange);
        }

        public void setPos(int x, int y)
        {
            // override setPos to make sure that Player can't go through walls
            // not that move() uses setPos, so this covers move!
            if(world().passable(x, y))
                super.setPos(x, y);
        }

        // called by Zombies when it runs into the Zombie
        public void infect()
        {
            infected = true;
            brainLeft -= 10;
        }
    }

    // We make this interesting by adding a new Actor type...
    private static class Zombie extends Actor
    {
        public Zombie()
        {
            super(new ColoredChar('Z', Color.yellow));
        }

        @Override
        public void act()
        {
            // held actors cannot move on their own, see below
            if(held())
                return;

            // here we use the global instance of Dice, the Jade random
            // number generator. Note that every method that uses an instance
            // of Dice can either take one you give it, or just use the global
            // instance.
            move(Dice.global.nextDir());

            // zombies eat brains...also take note of the getActorAt
            Player player = world().getActorAt(Player.class, pos());
            if(player != null)
            {
                player.infect();
                // the zombie now goes where the player goes, and is no longer
                // able to move on its own, which is why the first line of this
                // method is there
                attach(player);
            }
        }

        @Override
        public void setPos(int x, int y)
        {
            // this code is the same as in Player and similar code should
            // probably be put in some sort of superclass in your game...
            if(world().passable(x, y))
                super.setPos(x, y);
        }
    }

    // Maps should all extend World
    // two methods that should be overridden are tick and lookAll
    // more on this below
    private static class Level extends World
    {
        public Level()
        {
            super(80, 24);
        }

        @Override
        public void tick()
        {
            for(Player actor : getActors(Player.class))
                actor.act();
            for(Zombie zombie : getActors(Zombie.class))
                zombie.act();
            removeExpired();
        }

        @Override
        public List<ColoredChar> lookAll(int x, int y)
        {
            List<ColoredChar> look = new ArrayList<ColoredChar>();
            look.add(tileAt(x, y));
            for(Zombie zombie : getActorsAt(Zombie.class, x, y))
                look.add(zombie.face());
            for(Player player : getActorsAt(Player.class, x, y))
                look.add(player.face());
            return look;
        }
    }
}
