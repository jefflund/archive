package jade.gen.feature;

import jade.gen.Gen;

/**
 * A Gen which requires chaining, to be used exclusively for generation of
 * features on an already existing level.
 */
public abstract class FeatureGen extends Gen
{
    /**
     * Initializes the FeatureGen.
     * @param chained the required chained Gen
     */
    public FeatureGen(Gen chained)
    {
        super(chained);
        if(chained == null)
            throw new IllegalArgumentException("FeatureGen requires chaining");
    }
}
