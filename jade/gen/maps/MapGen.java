package jade.gen.maps;

import jade.gen.Gen;

/**
 * A specialization of Gen that disallows chaining. These algorithms generate
 * the basic maps, with no extra features. They are to be used as the first gen
 * step in the level generation step (which is why chaining is not supported).
 */
public abstract class MapGen extends Gen
{
    public MapGen()
    {
        super(null);
    }
}
