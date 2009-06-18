package jade.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BidirectionalMap<K, V> implements Serializable
{
	private Map<K, V> k2v;
	private Map<V, K> v2k;
	
	public BidirectionalMap()
  {
		k2v = new HashMap<K, V>();
		v2k = new HashMap<V, K>();
  }
}
