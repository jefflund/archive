package jade.gen.feature;

import jade.gen.Gen;

/**
 * A specialization of Gen that not only allows chaining, but requires it. If a
 * null valued Gen is given to the constructor for a chain, a
 * NullPointerException will be raised. This class is intended to be used to add
 * special features onto an already generated map.
 */
public abstract class FeatureGen extends Gen
{
    public FeatureGen(Gen chained)
    {
        super(chained);
        if(chained == null)
            throw new NullPointerException("FeatureGen chain cannot be null");
    }
}
