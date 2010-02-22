package jade.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A multimap is a map in which more than one value may be associated a given
 * key. It is essentially a map where each key maps to a collection. This
 * multimap is implemented using a TreeMap so the keys must implement the
 * Comparable interface. The values of each mapping are stored in a HashSet.
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
	 */
	public boolean containsKey(K key)
	{
		return map.containsKey(key);
	}

	/**
	 * Returns true if at least one key is mapped to the value in the multimap
	 */
	public boolean containsValue(Object value)
	{
		for(Collection<V> mapping : map.values())
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
	 */
	public Collection<V> get(K key)
	{
		return map.get(key);
	}

	/**
	 * Associates the value with a key, even if the key is already associated with
	 * other values. Any previously associated values will be retained in the
	 * mapping.
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
	 */
	public Collection<V> removeKey(K key)
	{
		return map.remove(key);
	}

	/**
	 * Returns a set of all the keys currently mapped to at least one value in the
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
	 */
	public Collection<V> values()
	{
		final Collection<V> values = new TreeSet<V>();
		for(Collection<V> mapping : map.values())
			values.addAll(mapping);
		return values;
	}

	/**
	 * Returns the number of values which are associated with keys. If more than
	 * one key maps to the same value, the value will be counted for each key. If
	 * this is undesirable, values().size() will not count values more than once.
	 */
	public int size()
	{
		int size = 0;
		for(Collection<V> mapping : map.values())
			size += mapping.size();
		return size;
	}

	/**
	 * Returns a multimap with the values for every key that is less than the
	 * specified key. This can be either inclusive or exclusive.
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
	 */
	public Multimap<K, V> tailMap(K fromKey, boolean inclusive)
	{
		final Multimap<K, V> tail = new Multimap<K, V>();
		tail.map = new TreeMap<K, Collection<V>>(map.tailMap(fromKey, inclusive));
		return tail;
	}

	/**
	 * Returns the lowest key currently in the map
	 */
	public K firstKey()
	{
		return map.firstKey();
	}

	/**
	 * Returns the highest key currently in the map
	 */
	public K lastKey()
	{
		return map.lastKey();
	}
}
