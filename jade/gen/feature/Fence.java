package jade.gen.feature;

import jade.core.World;
import jade.gen.Generator;
import jade.util.Dice;
import jade.util.datatype.ColoredChar;

public class Fence extends FeatureGenerator
{
    private static final ColoredChar face = ColoredChar.create('#');

    public Fence(Generator chained)
    {
        super(chained);
    }

    @Override
    protected void generateStep(World world, Dice dice)
    {
        for(int x = 0; x < world.width(); x++)
        {
            world.setTile(face, false, x, 0);
            world.setTile(face, false, x, world.height() - 1);
        }
        for(int y = 1; y < world.height() - 1; y++)
        {
            world.setTile(face, false, 0, y);
            world.setTile(face, false, world.width() - 1, y);
        }
    }
}
