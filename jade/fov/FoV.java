package jade.fov;

import jade.core.World;
import jade.util.Factory;
import jade.util.type.Coord;
import java.util.Set;

/**
 * Defines the interface for field of view algorithms. These classes generate a
 * field of view from a given position on a world. This field of view is
 * represented as a set of coordinates that are viewable from that coordinate.
 * Note that in order to use the factory, all implementing classes should have a
 * default constructor.
 */
public interface FoV
{
    /**
     * Calculates the field of vision on a world from the specified origin. The
     * field of vision will be limited in how far from the origin it can get by
     * the specified range. Generally, this bound should form a square of length
     * 2 * range, but other shapes, such as a circular field with radius range
     * are also acceptable.
     * @param world the world on which to generate the field of vision
     * @param orig the location of the origin of the field of vision
     * @param range defines the size of the bounds of the field of vision
     * @return a set of Coord that are in the field of vision
     */
    public abstract Set<Coord> getFov(World world, Coord orig, int range);

    /**
     * Factory for generating unique instances of FoV
     */
    public static Factory<FoV> factory = new Factory<FoV>();
}
