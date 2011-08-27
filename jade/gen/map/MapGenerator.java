package jade.gen.map;

import jade.gen.Generator;

/**
 * Represents an algorithm for generating entire maps. It should be the first step in any generation
 * process and therefore cannot have any chained {@code} Generator attached to it.
 */
public abstract class MapGenerator extends Generator
{
    public MapGenerator()
    {
        super();
    }
}
