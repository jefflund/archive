package jade.gen;

import jade.core.World;
import jade.util.Dice;
import jade.util.Factory;

/**
 * Base class for a random level generator. These generators are generally
 * designed to be allowed to be chained together in Decorator pattern fashion,
 * although some descending classes may or may not allow chaining. During
 * generation, the Gen will first asked its chained generator to perform its
 * step, after which it will perform its own step. In this way, various
 * instances of Gen which place special feature could be chained to other
 * algorithms that generates a basic levels in any number of interesting
 * combinations. Note that in order to use the factory, all derived classes
 * should have a default constructor. For this reason, all derived classes ought
 * to have setters for any generation parameters.
 */
public abstract class Gen
{
    private Gen chained;

    public Gen(Gen chained)
    {
        this.chained = chained;
    }

    /**
     * Randomly generates a level on the given Level using the provided dice.
     * This method should never reseed the dice. Provided that the state of the
     * Gen, the state of the World, and the state of the Dice are all the same
     * for each call, this method should generate the same level from each call.
     * @param world the world on which to generate a new level
     * @param dice the dice used to provide randomness
     */
    public void generate(World world, Dice dice)
    {
        if(chained != null)
            chained.generate(world, dice);
        genStep(world, dice);
    }

    /**
     * Randomly generates a level on the given Level using the global instance
     * of dice. This method should never reseed the dice. Provided that the
     * state of the Gen, the state of the World, and the state of the gobal Dice
     * is the same for each call, this method should generate the same level
     * from each call.
     * @param world the world on which to generate a new level
     */
    public final void generate(World world)
    {
        generate(world, Dice.global);
    }

    /**
     * Peforms the generation step specified by this Gen.
     * @param world
     * @param dice
     */
    protected abstract void genStep(World world, Dice dice);

    public static Factory<Gen> factory = new Factory<Gen>();
}