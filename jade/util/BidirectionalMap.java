package jade.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A bidirectial map is a map that supports look of values via keys, as well as
 * keys via value. Most maps allow different keys to map to the same value. The
 * bidirectional map adds the property of injection, or in otherwords, each
 * unique key must map to a unique value. This allows values to be used in
 * looking up keys.
 * @param <K> the type of the keys in this multimap
 * @param <V> the type of the values in this multimap
 */
public class BidirectionalMap<K, V> implements Serializable
{
	private final Map<K, Pair> keys;
	private final Map<V, Pair> values;

	/**
	 * Creates a new bidirectionalmap
	 */
	public BidirectionalMap()
	{
		keys = new HashMap<K, Pair>();
		values = new HashMap<V, Pair>();
	}

	/**
	 * Associates a key with a value. If the key was previous associated with a
	 * value, that value will be removed. If the value was previous associated
	 * with a key, that key will be removed.
	 * @param key the key to associate with the value
	 * @param value the value to associate with the key
	 */
	public void put(K key, V value)
	{
		removeKey(key);
		removeValue(value);
		final Pair pair = new Pair(key, value);
		keys.put(key, pair);
		values.put(value, pair);
	}

	/**
	 * Removes the key and associated value from the mapping.
	 * @param key the key to remove
	 */
	public void removeKey(K key)
	{
		freePair(keys.get(key));
	}

	/**
	 * Removes the value and associated key from the mapping.
	 * @param value the value to remove
	 */
	public void removeValue(V value)
	{
		freePair(values.get(value));
	}

	private void freePair(Pair pair)
	{
		if (pair != null)
		{
			keys.values().remove(pair);
			values.values().remove(pair);
		}
	}

	/**
	 * Returns the value assciated with they key, or null if there is no mapping.
	 * @param key the key to query
	 * @return the value assciated with they key
	 */
	public V getValue(K key)
	{
		final Pair pair = keys.get(key);
		if (pair != null)
			return pair.value;
		return null;
	}

	/**
	 * Returns the key assciated with they value, or null if there is no mapping.
	 * @param value the value to query
	 * @return the key assciated with they value
	 */
	public K getKey(V value)
	{
		final Pair pair = values.get(value);
		if (pair != null)
			return pair.key;
		return null;
	}

	/**
	 * Returns the number of mappings in the map
	 * @return the number of mappings in the map
	 */
	public int size()
	{
		return keys.size();
	}

	/**
	 * Returns true if there are no mappings in the map
	 * @return true if there are no mappings in the map
	 */
	public boolean isEmpty()
	{
		return size() == 0;
	}

	/**
	 * Returns true if there is a mapping for the key
	 * @param key the key to be tested
	 * @return true if there is a mapping for the key
	 */
	public boolean containsKey(Object key)
	{
		return keys.containsKey(key);
	}

	/**
	 * Returns true if there is a mapping to the value
	 * @param value the value to be tested
	 * @return true if there is a mapping to the value
	 */
	public boolean containsValue(Object value)
	{
		return values.containsKey(value);
	}

	/**
	 * Returns the set of all keys currently in the map
	 * @return the set of all keys currently in the map
	 */
	public Set<K> keySet()
	{
		return keys.keySet();
	}

	/**
	 * Returns the set of all values currently in the map
	 * @return the set of all values currently in the map
	 */
	public Set<V> values()
	{
		return values.keySet();
	}

	/**
	 * Removes all mappings from the map
	 */
	public void clear()
	{
		keys.clear();
		values.clear();
	}

	private class Pair
	{
		private final K key;
		private final V value;

		public Pair(K key, V value)
		{
			this.key = key;
			this.value = value;
		}
	}
}
