package jade;

import jade.core.Actor;
import jade.core.World;
import jade.gen.Gen;
import jade.gen.maps.Cellular;
import jade.ui.Console;
import jade.util.Tools;
import jade.util.type.ColoredChar;
import jade.util.type.Direction;
import java.awt.Color;

/**
 * Simple roguelike that demonstrates a few features of the jade roguelike
 * library.
 */
public class Demo
{
    public static void main(String[] args)
    {
        Console console = Console.getFramedConsole("Jade Rogue");
        World world = new World(Console.DEFAULT_COLS, Console.DEFAULT_ROWS);
        world.appendDrawOrder(A1.class);
        world.appendDrawOrder(A2.class);
        world.appendActOrder(A1.class);
        world.appendActOrder(A2.class);
        Gen.factory.get(Cellular.class).generate(world);
        Actor player = new A1(console);
        world.addActor(player);
        world.addActor(new A2());

        while(!player.expired())
        {
            for(int x = 0; x < world.width(); x++)
                for(int y = 0; y < world.height(); y++)
                    console.bufferChar(world.look(x, y), x, y);
            console.updateScreen();
            world.tick();
        }
        console.exit();
    }

    static class A1 extends Actor
    {
        private Console console;
        
        public A1(Console console)
        {
            super(new ColoredChar('@', Color.white));
            this.console = console;
        }
        
        @Override
        public void act()
        {
            char key = console.getKey();
            Direction dir = Tools.keyToDir(key);
            if(dir != null)
                move(dir.x(), dir.y());
            else if(key == 'q')
                expire();
        }
    }

    static class A2 extends Actor
    {
        public A2()
        {
            super(new ColoredChar('D', Color.white));
        }
        
        @Override
        public void act()
        {
        }
    }
}
