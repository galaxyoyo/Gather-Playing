package fr.galaxyoyo.gatherplaying;

import java.util.HashMap;
import java.util.function.Supplier;

public class DefaultHashMap<K, V> extends HashMap<K, V>
{
	private final Supplier<V> defaultValue;

	public DefaultHashMap(V defaultValue)
	{
		this(() -> defaultValue);
	}

	public DefaultHashMap(Supplier<V> defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public V get(Object key)
	{
		if (!containsKey(key))
			put((K) key, defaultValue.get());
		return super.get(key);
	}
}
