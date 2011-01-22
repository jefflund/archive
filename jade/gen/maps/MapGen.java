package jade.gen.maps;

import jade.gen.Gen;

/**
 * A class of Gen that disallows chaining. It is meant to be the first step in
 * the map generation process and as such should provided a basic map on which
 * features can be placed.
 */
public abstract class MapGen extends Gen
{
    /**
     * Constructs a new instance of MapGen.
     */
    public MapGen()
    {
        super();
    }
}
