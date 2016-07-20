package fr.galaxyoyo.gatherplaying;

import com.google.gson.Gson;
import fr.galaxyoyo.gatherplaying.client.Config;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Card implements Comparable<Card>
{
	private ObservableMap<String, Integer> muId = FXCollections.observableHashMap();
	private String number;
	private String mciNumber;
	private transient Set set;
	private ObservableMap<String, String> name = FXCollections.observableHashMap();
	private ObservableMap<String, String> ability = FXCollections.observableHashMap();
	private CardType type;
	private SubType[] subtypes = new SubType[0];
	private boolean basic;
	private Rarity rarity;
	private String power;
	private String toughness;
	private int loyalty;
	private ManaColor[] colors;
	private ManaColor[] manaCost;
	private double cmc;
	private Layout layout;
	private double cost;
	private double foilCost;
	private int[] variations;
	private ObservableMap<String, String> flavor = FXCollections.observableHashMap();
	private boolean legendary;
	private boolean world;
	private boolean snow;
	private boolean ongoing;
	private ManaColor[] colorIdentity;
	private int vanguardHand;
	private int vanguardLife;
	private boolean reserved;
	private Date releaseDate;
	private String border;
	private String artist;
	private String imageName;
	private String watermark;
	private boolean preview = false;

	public boolean isLegal(Rules rules) { return rules == Rules.FREEFORM || rules.isLegal(this) || isRestricted(rules); }

	public boolean isRestricted(Rules rules) { return rules.isRestricted(this); }

	public int getPreferredMuID()
	{
		Integer enMuId = getMuId("en");
		Integer muId = this.getMuId(Config.getLocaleCode());
		return muId == null ? enMuId : muId;
	}

	public Integer getMuId(String locale)
	{
		return muId.get(locale);
	}

	public StringBinding getTranslatedName() { return getTranslatedName(false); }

	public StringBinding getTranslatedName(boolean force)
	{
		return Bindings.createStringBinding(() -> {
			String name = this.name.get(Config.getLocaleCode());
			if (force)
				return name != null ? name : "";
			return name != null ? name : this.name.get("en");
		});
	}

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
	public int compareTo(@NotNull Card o)
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
			ret = Integer.compare(o.getMuId("en"), o.getMuId("en"));
		return ret;
	}

	public ObservableMap<String, Integer> getMuId()
	{
		return muId;
	}

	public String getNumber()
	{
		return number;
	}

	public void setNumber(String number)
	{
		this.number = number;
	}

	public String getMciNumber()
	{
		return mciNumber;
	}

	public void setMciNumber(String mciNumber)
	{
		this.mciNumber = mciNumber;
	}

	public Set getSet()
	{
		return set;
	}

	public void setSet(Set set)
	{
		this.set = set;
	}

	public ObservableMap<String, String> getName()
	{
		return name;
	}

	public void setName(ObservableMap<String, String> name)
	{
		this.name = name;
	}

	public ObservableMap<String, String> getAbilityMap()
	{
		return ability;
	}

	public CardType getType()
	{
		return type;
	}

	public void setType(CardType type) { this.type = type; }

	public SubType[] getSubtypes()
	{
		return subtypes;
	}

	public void setSubtypes(SubType[] subtypes)
	{
		this.subtypes = subtypes;
	}

	public boolean isBasic()
	{
		return basic;
	}

	public void setBasic(boolean basic)
	{
		this.basic = basic;
	}

	public Rarity getRarity()
	{
		return rarity;
	}

	public void setRarity(Rarity rarity)
	{
		this.rarity = rarity;
	}

	public String getPower()
	{
		return power;
	}

	public void setPower(String power)
	{
		this.power = power;
	}

	public String getToughness()
	{
		return toughness;
	}

	public void setToughness(String toughness)
	{
		this.toughness = toughness;
	}

	public int getLoyalty()
	{
		return loyalty;
	}

	public void setLoyalty(int loyalty)
	{
		this.loyalty = loyalty;
	}

	public ManaColor[] getColors()
	{
		return colors;
	}

	public void setColors(ManaColor[] colors)
	{
		this.colors = colors;
	}

	public ManaColor[] getManaCost()
	{
		return manaCost;
	}

	public void setManaCost(ManaColor[] manaCost)
	{
		this.manaCost = manaCost;
	}

	public double getCmc()
	{
		return cmc;
	}

	public void setCmc(double cmc)
	{
		this.cmc = cmc;
	}

	public Layout getLayout()
	{
		return layout;
	}

	public void setLayout(Layout layout)
	{
		this.layout = layout;
	}

	public double getCost()
	{
		return cost;
	}

	public void setCost(double cost)
	{
		this.cost = cost;
	}

	public double getFoilCost()
	{
		return foilCost;
	}

	public void setFoilCost(double foilCost)
	{
		this.foilCost = foilCost;
	}

	public int[] getVariations()
	{
		return variations;
	}

	public void setVariations(int[] variations)
	{
		this.variations = variations;
	}

	public ObservableMap<String, String> getFlavorMap()
	{
		return flavor;
	}

	public boolean isLegendary()
	{
		return legendary;
	}

	public void setLegendary(boolean legendary)
	{
		this.legendary = legendary;
	}

	public boolean isWorld()
	{
		return world;
	}

	public void setWorld(boolean world)
	{
		this.world = world;
	}

	public boolean isSnow()
	{
		return snow;
	}

	public void setSnow(boolean snow)
	{
		this.snow = snow;
	}

	public boolean isOngoing()
	{
		return ongoing;
	}

	public void setOngoing(boolean ongoing)
	{
		this.ongoing = ongoing;
	}

	public ManaColor[] getColorIdentity()
	{
		return colorIdentity;
	}

	public void setColorIdentity(ManaColor[] colorIdentity)
	{
		this.colorIdentity = colorIdentity;
	}

	public int getVanguardHand()
	{
		return vanguardHand;
	}

	public void setVanguardHand(int vanguardHand)
	{
		this.vanguardHand = vanguardHand;
	}

	public int getVanguardLife()
	{
		return vanguardLife;
	}

	public void setVanguardLife(int vanguardLife)
	{
		this.vanguardLife = vanguardLife;
	}

	public boolean isReserved()
	{
		return reserved;
	}

	public void setReserved(boolean reserved)
	{
		this.reserved = reserved;
	}

	public Date getReleaseDate()
	{
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate)
	{
		this.releaseDate = releaseDate;
	}

	public String getBorder()
	{
		return border;
	}

	public void setBorder(String border)
	{
		this.border = border;
	}

	public String getArtist()
	{
		return artist;
	}

	public void setArtist(String artist)
	{
		this.artist = artist;
	}

	public String getImageName()
	{
		return imageName;
	}

	public void setImageName(String imageName)
	{
		this.imageName = imageName;
	}

	public String getWatermark()
	{
		return watermark;
	}

	public void setWatermark(String watermark)
	{
		this.watermark = watermark;
	}

	public void setPreview()
	{
		if (getMuId().isEmpty())
		{
			int muId = getNextAvailableMuId();
			getMuId().put("en", muId);
			getMuId().put("fr", muId + 1);
		}
		preview = true;
	}

	public boolean isPreview()
	{
		return preview;
	}

	public static int getNextAvailableMuId()
	{
		AtomicInteger muId = new AtomicInteger(1000000);
		while (MySQL.getAllCards().stream().filter(c -> c.getMuId().values().contains(muId.get())).findAny().isPresent())
			muId.incrementAndGet();
		return muId.get();
	}
}