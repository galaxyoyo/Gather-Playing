package fr.galaxyoyo.gatherplaying;

import java.util.function.Supplier;

import java.util.Comparator;
import java.util.TreeMap;

public class DefaultTreeMap<K, V> extends TreeMap<K, V>
{
	private final Supplier<V> defaultValue;

	public DefaultTreeMap(Comparator<? super K> comp, Supplier<V> defaultValue)
	{
		super(comp);
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