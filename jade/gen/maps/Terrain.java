package jade.gen.maps;

import jade.core.World;
import jade.util.Dice;
import jade.util.type.ColoredChar;
import java.awt.Color;

/**
 * A simple generator that places tiles at random according to a predefined
 * distribution. This is useful for generating many different terrain types,
 * although this terrain will not be particularly interesting.
 */
public class Terrain extends MapGen
{
    private int pClosed = 10;
    private int[] pClosedFace = {33, 33, 34};
    private ColoredChar[] closedFaces = {new ColoredChar('%', Color.yellow),
            new ColoredChar('%', Color.orange),
            new ColoredChar('%', Color.green)};
    private int[] pOpenFace = {50, 25, 25};
    private ColoredChar[] openFaces = {new ColoredChar('.', Color.white),
            new ColoredChar('.', Color.yellow),
            new ColoredChar('.', Color.green)};

    @Override
    public void genStep(World world, Dice dice)
    {
        for(int x = 1; x < world.width() - 1; x++)
            for(int y = 1; y < world.height() - 1; y++)
            {
                if(dice.nextChance(pClosed))
                {
                    ColoredChar face = nextFace(pClosedFace, closedFaces, dice);
                    world.setTile(face, false, x, y);
                }
                else
                {
                    ColoredChar face = nextFace(pOpenFace, openFaces, dice);
                    world.setTile(face, true, x, y);
                }
            }
    }

    private ColoredChar nextFace(int[] dist, ColoredChar[] choices, Dice dice)
    {
        int sum = 0;
        for(int i = 0; i < dist.length; i++)
            sum += dist[i];
        int choice = dice.nextInt(sum);

        int total = 0;
        for(int i = 0; i < dist.length; i++)
        {
            total += dist[i];
            if(choice < total)
                return choices[i];
        }
        return null;
    }
}
