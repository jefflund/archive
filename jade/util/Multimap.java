package jade.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A multimap is a map in which more than one value may be associated with and
 * returned for a given key. It is essentially a map where each key maps to a
 * collection. This multimap is implemented using a TreeMap so the keys must
 * implement the Comparable interface. The values of each mapping are stored in
 * a HashSet.
 * @param <K> the type of the keys in this multimap
 * @param <V> the type of the values in this multimap
 */
public class Multimap<K, V> implements Serializable
{
	private TreeMap<K, Collection<V>> map;

	/**
	 * Constructs a new empty multimap with the natural ordering of the keys.
	 */
	public Multimap()
	{
		map = new TreeMap<K, Collection<V>>();
	}

	/**
	 * Returns true if the multimap is empty.
	 * @return true if the multimap is empty.
	 */
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	/**
	 * Clears all mappings from the multimap
	 */
	public void clear()
	{
		map.clear();
	}

	/**
	 * Returns true if the key is mapped to at least one value in the multimap.
	 * @param key the key to be tested
	 * @return true if the key is mapped to at least one value in the multimap.
	 */
	public boolean containsKey(K key)
	{
		return map.containsKey(key);
	}

	/**
	 * Returns true if at least one key is mapped to the value in the multimap
	 * @param value the value to be tested
	 * @return true if at least one key is mapped to the value in the multimap
	 */
	public boolean containsValue(Object value)
	{
		for(final Collection<V> mapping : map.values())
			if(mapping.contains(value))
				return true;
		return false;
	}

	/**
	 * Returns a collection with the values the key is mapped to, or null if there
	 * is no mapping for the key. More formally, if this map contains a mapping
	 * from a key k to a value v such that key compares equal to k according to
	 * the map's ordering, then this method returns a collection which will
	 * contain v; otherwise it returns null.
	 * @param key the whose values will be returned
	 * @return a collection with the values the key is mapped to, or null if there
	 * is no mapping for the key
	 */
	public Collection<V> get(Object key)
	{
		return map.get(key);
	}

	/**
	 * Associates the value with a key, even if the key is already associated with
	 * other values.
	 * @param key the key which will be mapped to the value
	 * @param value the value to be associated with the key
	 */
	public void put(K key, V value)
	{
		Collection<V> set = map.get(key);
		if(set == null)
			set = new HashSet<V>();
		set.add(value);
		map.put(key, set);
	}

	/**
	 * Removes all values associated with the key. After this function is called,
	 * if get were called using this key, null would be returned. However, if the
	 * value is associated with another key, then that mapping will still be
	 * preserved.
	 * @param key
	 * @return a collection with all values previously associated with the key
	 */
	public Collection<V> removeKey(K key)
	{
		return map.remove(key);
	}

	/**
	 * Returns a set of all the keys currently mapped to at least one value in the
	 * multimap.
	 * @return a set of all the keys currently mapped to at least one value in the
	 * multimap.
	 */
	public Set<K> keys()
	{
		return map.keySet();
	}

	/**
	 * Returns a collection of all the values currently associated with at least
	 * one key in the multimap. If more than one key both map to the same value,
	 * then the value will only be included once in the collection.
	 * @return a collection of all the values currently associated with at least
	 * one key
	 */
	public Collection<V> values()
	{
		final Collection<V> values = new TreeSet<V>();
		for(final Collection<V> mapping : map.values())
			values.addAll(mapping);
		return values;
	}

	/**
	 * Returns the number of values which are associated with keys. If more than
	 * one key maps to the same value, the value will be counted for each key. If
	 * this is undesirable, values().size() will not count values more than once.
	 * @return the number of values which are associated with keys
	 */
	public int size()
	{
		int size = 0;
		for(final Collection<V> mapping : map.values())
			size += mapping.size();
		return size;
	}

	/**
	 * Returns a multimap with the values for every key that is less than the
	 * specified key. This can be either inclusive or exclusive.
	 * @param toKey the highest key allowed in the headmap
	 * @param inclusive if true, then the key will be allowed in the headmap
	 * @return multimap with the values for every key that is less than the key
	 */
	public Multimap<K, V> headMap(K toKey, boolean inclusive)
	{
		final Multimap<K, V> head = new Multimap<K, V>();
		head.map = new TreeMap<K, Collection<V>>(map.headMap(toKey, inclusive));
		return head;
	}

	/**
	 * Returns a multimap with the values for every key that is greater than the
	 * specified key. This can be either inclusive or exclusive.
	 * @param fromKey the lowest key allowed in the tailmap
	 * @param inclusive if true, then the key will be allowed in the tailmap
	 * @return multimap with the values for every key that is greater than the key
	 */
	public Multimap<K, V> tailMap(K fromKey, boolean inclusive)
	{
		final Multimap<K, V> tail = new Multimap<K, V>();
		tail.map = new TreeMap<K, Collection<V>>(map.tailMap(fromKey, inclusive));
		return tail;
	}
}
