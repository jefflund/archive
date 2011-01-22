package jade.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A pseudo-random number generator using the Mersenne Twister algorithm as
 * presented on Wikipedia.
 */
public class Dice
{
    /**
     * A global instance of Dice, for use when a unique instance of Dice is not
     * required.
     */
    public static final Dice global = new Dice();

    private static final int N = 624;

    private int[] twister;
    private int index;

    /**
     * Constructs an instance of Dice, with the given seed.
     * @param seed the seed to the pseudo-random number generator
     */
    public Dice(int seed)
    {
        twister = new int[N];
        index = 0;
        reseed(seed);
    }

    public Dice()
    {
        this((int)System.currentTimeMillis());
    }

    /**
     * Reseeds the pseudo-random number generator.
     * @param seed the seed to the pseudo-random number generator
     */
    public void reseed(int seed)
    {
        twister[0] = seed;
        for(int i = 1; i < N; i++)
            twister[i] = 0x6c078965 * (twister[i - 1] ^ (twister[i - 1] << 30))
            + i;
    }

    /**
     * Reseeds the pseudo-random number generator with a seed taken from the
     * current state of the clock.
     */
    public final void reseed()
    {
        reseed((int)System.currentTimeMillis());
    }

    /**
     * Returns a pseudo-random integer.
     * @return a random integer
     */
    public int next()
    {
        if(index == 0)
            generate();

        int y = twister[index];
        y ^= y >> 11;
        y ^= (y << 7) & 0x9d2c5680;
        y ^= (y << 15) & 0xefc60000;
        y ^= y >> 18;

        index = (index + 1) % N;

        return y;
    }

    /**
     * Returns a pseudo-random integer strictly less than max
     * @param max the upper bound (exclusive) of the return value
     * @return a random integer less than max
     */
    public final int next(int max)
    {
        return next() % max;
    }

    /**
     * Returns a pseudo-random integer between min and max inclusive.
     * @param min the lower bound (inclusive) of the return value
     * @param max the upper bound (inclusive) of the return value
     * @return a random integer between min and max
     */
    public final int next(int min, int max)
    {
        int range = max - min;
        return next(range + 1) + min;
    }

    /**
     * Returns true with a probability of pval / total.
     * @param pval the probability of true
     * @param total the total amount of probability
     * @return true with probability pval / total
     */
    public boolean chance(int pval, int total)
    {
        return next(total) < pval;
    }

    /**
     * Returns true with a probability of pval / 100.
     * @param pval the probability of true
     * @return true with probability pval / 100
     */
    public final boolean chance(int probablity)
    {
        return chance(probablity, 100);
    }

    /**
     * Returns true with a probability of 1/2.
     * @return true with probability 1/2
     */
    public final boolean chance()
    {
        return chance(50);
    }

    /**
     * Returns the sum of an xdy dice roll. In other words, x y-sided dice are
     * rolled, and their sum is returned.
     * @param x the number of dice
     * @param y the number of sides on each dice
     * @return the result of an xdy dice roll
     */
    public final int rollXdY(int x, int y)
    {
        int sum = 0;
        for(int i = 0; i < x; i++)
            sum += next(1, y);
        return sum;
    }
    
    public <T> T choose(T[] array)
    {
        int index = next(array.length);
        return array[index];
    }
    
    public <T> T choose(List<T> list)
    {
        int index = next(list.size());
        return list.get(index);
    }
    
    public <T> T choose(Collection<T> collection)
    {
        int index = next(collection.size());
        Iterator<T> iter = collection.iterator();
        T result = iter.next();
        while(index > 0)
            iter.next();
        return result;
    }

    private void generate()
    {
        for(int i = 0; i < N; i++)
        {
            int y = twister[i] & 0x80000000;
            y += twister[(i + 1) % N] & 0x7fffffff;
            twister[i] = twister[(i + 397) % N] ^ (y >> 1);
            if(y % 2 == 1)
                twister[i] = twister[i] ^ 0x9908b0df;
        }
    }
}
