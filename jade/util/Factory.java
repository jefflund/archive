package jade.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates a unique instance of each descending type of a particular super
 * type. These instances are instantiated lazily, but are cached for all future
 * calls to get using the same parameter. Note that all types passed in for
 * generation should have a default constructor or else an error is raised.
 * @param <T> the super type of the instances to be generated
 */
public class Factory<T>
{
    private Map<Class<? extends T>, T> singletons = init();

    /**
     * Returns a default instance of the given type, which descends for T.
     * Instantiation is done lazily, but the results are then cached. In other
     * words, no work is done by the Factory until the first time you make a
     * call to this method, but once you have made a call, all subsequent
     * classes with the exact same parameter will return exactly the same unique
     * instance.
     * @param type the type descending from T
     * @return a unique instance of the given type
     */
    public T get(Class<? extends T> type)
    {
        try
        {
            if(!singletons.containsKey(type))
                singletons.put(type, type.newInstance());
            return singletons.get(type);
        }
        catch(InstantiationException e)
        {
            e.printStackTrace();
            return null;
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private Map<Class<? extends T>, T> init()
    {
        return new HashMap<Class<? extends T>, T>();
    }
}
