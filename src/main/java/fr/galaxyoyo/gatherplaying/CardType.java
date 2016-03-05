package fr.galaxyoyo.gatherplaying;

import fr.galaxyoyo.gatherplaying.client.I18n;
import javafx.beans.binding.StringBinding;

public enum CardType
{
	INSTANT, SORCERY, CREATURE, LAND, ARTIFACT, CREATURE_ARTIFACT, LAND_ARTIFACT, LAND_CREATURE, ENCHANTMENT, CREATURE_ENCHANTMENT, ENCHANTMENT_ARTIFACT, PLANESWALKER,
	CREATURE_PLANESWALKER, VANGUARD, TRIBAL, ENCHANTMENT_TRIBAL, INSTANT_TRIBAL, SORCERY_TRIBAL, ARTIFACT_TRIBAL, PLAYER, ENCHANTMENT_PLAYER, PLANE, SCHEME, PHENOMENON,
	CONSPIRACY, CREATURE_TOKEN, ARTIFACT_TOKEN, CREATURE_ARTIFACT_TOKEN, ENCHANTMENT_CREATURE_TOKEN, ENCHANTMENT_ARTIFACT_TOKEN, ENCHANTMENT_ARTIFACT_CREATURE_TOKEN, EMBLEM;

	public CardType with(CardType type)
	{
		try
		{
			return valueOf(name() + "_" + type.name());
		}
		catch (IllegalArgumentException ex)
		{
			return valueOf(type.name() + "_" + name());
		}
	}

	public boolean is(CardType type) { return this == type || name().contains(type.name()); }

	public boolean isPermanent() { return !is(INSTANT) && !is(SORCERY); }

	public StringBinding getTranslatedName() { return I18n.tr("type." + name().toLowerCase()); }

	@Override
	public String toString() { return getTranslatedName().get(); }
}