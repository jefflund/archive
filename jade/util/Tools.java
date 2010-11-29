package jade.util;

import jade.util.type.Direction;

/**
 * Tools contains varias static helper methods used through the jade library.
 */
public final class Tools
{
    private Tools()
    {
        throw new IllegalAccessError("Tools may not be instantiated");
    }

    /**
     * Returns the string as is if the suffix is present, or returns the string
     * concatinated with the suffix if it is not present.
     * @param str the string needing the suffix
     * @param suffix the suffix to be appended
     * @return the str with the suffix at the end
     */
    public static String ensureSuffix(String str, String suffix)
    {
        if(str.endsWith(suffix))
            return str;
        else
            return str + suffix;
    }

    /**
     * Returns the integer clamped to the range [min, max]. In other words, if n
     * < min, returns min; if n > max, returns max; otherwise returns n/
     * @param n the integer to be clamped
     * @param min the minimum return value
     * @param max the maximum return value
     * @return n clamped to the range [min, max]
     */
    public static int clamp(int n, int min, int max)
    {
        if(n < min)
            return min;
        else if(n > max)
            return max;
        else
            return n;
    }

    public static Direction keyToDir(char key)
    {
        switch(key)
        {
        case 'l':
            return Direction.E;
        case 'h':
            return Direction.W;
        case 'k':
            return Direction.N;
        case 'j':
            return Direction.S;
        case 'b':
            return Direction.SW;
        case 'n':
            return Direction.SE;
        case 'u':
            return Direction.NE;
        case 'y':
            return Direction.NW;
        case '.':
            return Direction.O;
        default:
            return null;
        }
    }
}
