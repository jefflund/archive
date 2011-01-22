package jade.gen.maps;

import jade.core.World;
import jade.util.ColoredChar;
import jade.util.Dice;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * A map generator which creates an open world, with some random terrain
 * features.
 */
public class Terrain extends MapGen
{
    private int probImpass;
    private List<ColoredChar> pass;
    private List<ColoredChar> impass;

    public Terrain()
    {
        this(10);
    }

    public Terrain(int probImpass)
    {
        this(probImpass, defaultPass(), defaultImpass());
    }

    public Terrain(int probImpass, List<ColoredChar> pass,
            List<ColoredChar> impass)
    {
        this.probImpass = probImpass;
        this.pass = pass;
        this.impass = impass;
    }

    @Override
    protected void genStep(World world, Dice dice)
    {
        for(int x = 0; x < world.width(); x++)
            for(int y = 0; y < world.height(); y++)
            {
                if(dice.chance(probImpass))
                {
                    ColoredChar tile = dice.choose(impass);
                    world.setTile(x, y, tile, false);
                }
                else
                {
                    ColoredChar tile = dice.choose(pass);
                    world.setTile(x, y, tile, true);
                }
            }
    }

    private static List<ColoredChar> defaultPass()
    {
        ArrayList<ColoredChar> pass = new ArrayList<ColoredChar>();
        pass.add(new ColoredChar('.', Color.white));
        pass.add(new ColoredChar('.', Color.green));
        pass.add(new ColoredChar('.', Color.yellow));
        return pass;
    }

    private static List<ColoredChar> defaultImpass()
    {
        ArrayList<ColoredChar> impass = new ArrayList<ColoredChar>();
        impass.add(new ColoredChar('%', Color.green));
        impass.add(new ColoredChar('%', Color.yellow));
        return impass;
    }
}
