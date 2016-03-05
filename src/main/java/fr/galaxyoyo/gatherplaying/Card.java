package fr.galaxyoyo.gatherplaying;

import com.google.gson.Gson;
import fr.galaxyoyo.gatherplaying.client.Config;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.Date;

public class Card implements Comparable<Card>
{
	public ObservableMap<String, String> muId = FXCollections.observableHashMap();
	public String cardId;
	public String mciNumber;
	public transient Set set;
	int[] variations;
	public ObservableMap<String, String> name = FXCollections.observableHashMap();
	public ObservableMap<String, String> ability = FXCollections.observableHashMap();
	ObservableMap<String, String> flavor = FXCollections.observableHashMap();
	public CardType type;
	public SubType[] subtypes = new SubType[0];
	boolean legendary, world, snow, ongoing;
	public boolean basic;
	public Rarity rarity;
	public String power, toughness;
	public int loyalty;
	public ManaColor[] colors;
	ManaColor[] colorIdentity;
	public ManaColor[] manaCost;
	public double cmc;
	int vanguardHand, vanguardLife;
	boolean reserved;
	Date releaseDate;
	public Layout layout;
	String border;
	String artist, imageName, watermark;
	public double cost, foilCost;

	public boolean isLegal(Rules rules) { return rules == Rules.FREEFORM || rules.isLegal(this) || isRestricted(rules); }

	public boolean isRestricted(Rules rules) { return rules.isRestricted(this); }

	public String getPreferredMuID()
	{
		String enMuId = muId.get("en");
		String muId = this.muId.get(Config.getLocaleCode());
		return muId == null ? enMuId : muId;
	}

	public StringBinding getTranslatedName(boolean force)
	{
		return Bindings.createStringBinding(() -> {
			String name = this.name.get(Config.getLocaleCode());
			if (force)
				return name != null ? name : "";
			return name != null ? name : this.name.get("en");
		});
	}

	public StringBinding getTranslatedName() { return getTranslatedName(false); }

	@Override
	public int hashCode() { return name.get("en").hashCode() << 16 | (type == null ? 0 : type.hashCode()); }

	@Override
	public String toString() { return new Gson().toJson(this); }

	public String getAbility()
	{
		String enAbility = ability.get("en");
		if (enAbility == null)
			return null;
		String ability = this.ability.get(Config.getLocaleCode());
		return ability == null || ability.equals("N/A") || ability.trim().isEmpty() ? enAbility : ability;
	}

	public String getFlavor()
	{
		String enFlavor = flavor.get("en");
		if (enFlavor == null)
			return null;
		String flavor = this.flavor.get(Config.getLocaleCode());
		return flavor == null || flavor.equals("N/A") || flavor.trim().isEmpty() ? enFlavor : flavor;
	}

	@Override
	public int compareTo(Card o)
	{
		if (o == this)
			return 0;
		if (set == null)
			return 1;
		if (o.set == null)
			return -1;
		int ret = -set.compareTo(o.set);
		if (ret == 0)
			ret = String.CASE_INSENSITIVE_ORDER.compare(name.get("en"), o.name.get("en"));
		if (ret == 0)
			ret = String.CASE_INSENSITIVE_ORDER.compare(o.muId.get("en"), o.muId.get("en"));
		return ret;
	}
}