package jade.gen.feature;

import jade.core.World;
import jade.gen.Gen;
import jade.util.ColoredChar;
import jade.util.Dice;

/**
 * Places a fence around a world Map
 */
public class Fence extends FeatureGen
{
    private ColoredChar wallTile;

    /**
     * Initializes the Fence, using a '#' for the fence walls.
     * @param chained the required chained Gen
     */
    public Fence(Gen chained)
    {
        this(chained, new ColoredChar('#'));
    }

    /**
     * Initializes the Fense, using a provided character for fence walls.
     * @param chained the required chained Gen
     * @param wallTile the tile used for impassable walls
     */
    public Fence(Gen chained, ColoredChar wallTile)
    {
        super(chained);
        this.wallTile = wallTile;
    }

    @Override
    protected void genStep(World world, Dice dice)
    {
        for(int x = 0; x < world.width(); x++)
        {
            world.setTile(x, 0, wallTile, false);
            world.setTile(x, world.height() - 1, wallTile, false);
        }
        for(int y = 0; y < world.height(); y++)
        {
            world.setTile(0, y, wallTile, false);
            world.setTile(world.width() - 1, y, wallTile, false);
        }
    }
}
