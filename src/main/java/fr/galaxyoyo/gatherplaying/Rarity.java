package fr.galaxyoyo.gatherplaying;

import fr.galaxyoyo.gatherplaying.client.I18n;
import javafx.beans.binding.StringBinding;

public enum Rarity
{
	COMMON, UNCOMMON, RARE, MYTHIC, BASIC_LAND, TOKEN, SPECIAL;

	@Override
	public String toString() { return getTranslatedName().get(); }

	public StringBinding getTranslatedName() { return I18n.tr("rarity." + name().toLowerCase()); }
}