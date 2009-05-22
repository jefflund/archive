package jade.util;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Multimap<K, V>
{
	private TreeMap<K, Collection<V>> map;

	public Multimap()
	{
		map = new TreeMap<K, Collection<V>>();
	}

	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	public void clear()
	{
		map.clear();
	}

	public boolean containsKey(K key)
	{
		return map.containsKey(key);
	}

	public boolean containsValue(Object value)
	{
		for(Collection<V> mapping : map.values())
			if(mapping.contains(value))
				return true;
		return false;
	}

	public Collection<V> get(Object key)
	{
		return map.get(key);
	}

	public void put(K key, V value)
	{
		Collection<V> set = map.get(key);
		if(set == null)
			set = new TreeSet<V>();// might a HashSet be better?
		set.add(value);
		map.put(key, set);
	}

	public Collection<V> removeKey(K key)
	{
		return map.remove(key);
	}

	public Set<K> keys()
	{
		return map.keySet();
	}

	public Collection<V> values()
	{
		Collection<V> values = new TreeSet<V>();
		for(Collection<V> mapping : map.values())
			values.addAll(mapping);
		return values;
	}

	public int size()
	{
		int size = 0;
		for(Collection<V> mapping : map.values())
			size += mapping.size();
		return size;
	}

	public Multimap<K, V> headMap(K toKey, boolean inclusive)
	{
		Multimap<K, V> head = new Multimap<K, V>();
		head.map = new TreeMap<K, Collection<V>>(map.headMap(toKey, inclusive));
		return head;
	}

	public Multimap<K, V> tailMap(K fromKey, boolean inclusive)
	{
		Multimap<K, V> tail = new Multimap<K, V>();
		tail.map = new TreeMap<K, Collection<V>>(map.tailMap(fromKey, inclusive));
		return tail;
	}
}
