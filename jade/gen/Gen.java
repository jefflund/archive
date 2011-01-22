package jade.gen;

import jade.core.World;
import jade.util.Dice;

/**
 * The base class for random map generators. These generators are generally
 * designed to allow chaining so that in decorator fashion, although decending
 * classes may either disallow or mandate chaining. The Gen will first ask its
 * chained Gen to perform its step, and the perform its own. In this way,
 * generators which place special features can be chained with generators with
 * provided basic levels.
 */
public abstract class Gen
{
    private Gen chained;

    /**
     * Intializes a new Gen.
     * @param chained the optional Gen (ie can be null)
     */
    public Gen(Gen chained)
    {
        this.chained = chained;
    }

    /**
     * Initializes a new Gen, with no chaining.
     */
    public Gen()
    {
        this(null);
    }

    /**
     * Randomly generates or modifies the world map using the provided Dice for
     * random number generation. This method should always generate the exact
     * same map given dice with identical states (ie reseed just before calling
     * this method).
     * @param world The World to be modified by the generator
     * @param dice the Dice used for random number generation
     */
    public void generate(World world, Dice dice)
    {
        if(chained != null)
            chained.generate(world, dice);
        genStep(world, dice);
    }

    /**
     * Randomly generates or modifies the world map using the global instance of
     * Dice for random number generation.
     * @param world The World to be modified by the generator
     */
    public final void generate(World world)
    {
        generate(world, Dice.global);
    }

    /**
     * Performs the modifications of this Gen on the world, using the given Dice
     * for random number generation. This step can be as simple as placing some
     * feature on a map, to actual level generation. This method should always
     * generate the exact same map given dice with identical states.
     * @param world the World to be modified by the generator
     * @param dice the Dice used for random number generation
     */
    protected abstract void genStep(World world, Dice dice);
}
