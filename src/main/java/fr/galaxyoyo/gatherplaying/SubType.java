package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.galaxyoyo.gatherplaying.client.I18n;
import javafx.beans.binding.StringBinding;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class SubType implements Comparable<SubType>
{
	private static final Map<String, SubType> subtypes = Maps.newHashMap();
	private static SubType[] values = new SubType[0];
	public final String name;
	private HashSet<CardType> applicables = Sets.newHashSet();

	private SubType(String name) { this.name = name; }

	public static SubType valueOf(String name)
	{
		if ("TheBiggest,Baddest,Nastiest,Scariestof".contains(name))
			return null;
		if (subtypes.containsKey(name))
			return subtypes.get(name);
		SubType subtype = new SubType(name);
		subtypes.put(name, subtype);
		return subtype;
	}

	public static SubType[] values()
	{
		if (values.length != subtypes.size())
		{
			values = new SubType[subtypes.size()];
			values = subtypes.values().toArray(values);
			Arrays.sort(values);
		}
		return values;
	}

	public boolean canApplicate(Collection<CardType> types)
	{
		for (CardType type : types)
		{
			if (canApplicate(type))
				return true;
		}

		return false;
	}

	private boolean canApplicate(CardType type) { return applicables.contains(type); }

	public void setCanApplicate(CardType type) { applicables.add(type); }

	@Override
	public int compareTo(@NotNull SubType o) { return String.CASE_INSENSITIVE_ORDER.compare(toString(), o.toString()); }

	@Override
	public String toString() { return getTranslatedName().get(); }

	public StringBinding getTranslatedName() { return I18n.tr("subtype." + name.toLowerCase().replace("'", "")); }
}