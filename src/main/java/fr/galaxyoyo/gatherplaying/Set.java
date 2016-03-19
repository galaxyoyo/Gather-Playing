package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.galaxyoyo.gatherplaying.client.Config;
import java8.util.function.Predicate;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Set implements Comparable<Set>
{
	public Object[] booster;
	protected Map<String, String> translations = Maps.newHashMap();
	private java.util.Set<Card> cards = Sets.newHashSet();
	private HashSet<PreconstructedDeck> preconstructeds = Sets.newHashSet();
	private String code;
	private String name;
	private String magicCardsInfoCode;
	private Date releaseDate;
	private String border;
	private String type;
	private String block;
	@SuppressWarnings("unused")
	private String mkm_name;
	@SuppressWarnings("unused")
	private int mkm_id = -1;
	private boolean buyable = false;
	private String finishedTranslations = "";

	public static Set read(String jsoned)
	{
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, CardSerializer.DATE).registerTypeAdapter(Card.class, new CardAdapter()).create();
		Set set = gson.fromJson(jsoned, Set.class);
		if (set.magicCardsInfoCode == null)
			set.magicCardsInfoCode = set.code.toLowerCase();
		for (Card card : Sets.newHashSet(set.cards))
		{
			if (card.getType() == null)
			{
				System.out.println(card);
				set.cards.remove(card);
				continue;
			}
			card.setSet(set);
			if (card.getMuId("en") == null)
			{
				System.out.println("MULTIVERSE ID == NULL : " + card);
				set.cards.remove(card);
				//	System.exit(1);
			}
			if (card.getBorder() == null)
				card.setBorder(set.border);
			if (set.code.equals("BFZ") && card.getType() == CardType.LAND && card.isBasic())
				card.setCardId(card.getCardId() + (Byte.parseByte(card.getImageName().charAt(card.getImageName().length() - 1) + "") % 2 == 1 ? 'a' : 'b'));
		}
		return set;
	}

	public void addLang(String language)
	{
		try
		{
			String json = IOUtils.toString(new URL("http://gatherplaying.arathia.fr/json/" + code.replace("CON", "CON_") + "." + language + ".json"), "UTF-8");
			Gson gson = new Gson();
			LanguageData data = gson.fromJson(json, LanguageData.class);
			for (CardLanguageData cardData : data.cards)
			{
				Card card = StreamSupport.stream(cards).filter(c -> Integer.toString(cardData.multiverseid).equalsIgnoreCase(c.getMuId(language))).findAny().orElse(null);
				if (card == null)
				{
					for (Card c : cards)
					{
						if (c.isBasic())
							System.out.println(c.getMuId("fr"));
					}
					System.out.println(cardData.multiverseid + " (" + code + ")");
					System.out.println(cardData.originalText);
					System.exit(0);
				}
				card.getAbilityMap().put(language, cardData.originalText);
				card.getFlavorMap().put(language, cardData.flavor);
				MySQL.updateCard(card);
			}
		} catch (FileNotFoundException ignored)
		{
		} catch (IOException ex)
		{
			ex.printStackTrace();
			return;
		}

		finishedTranslations += language;
		MySQL.update("sets", MySQL.Condition.equals(new MySQL.Value("code", code)), new MySQL.Value("finished_translations", finishedTranslations));
	}

	@SuppressWarnings("unused")
	public Card[] generateBooster()
	{
		if (booster == null)
			return null;
		Object[] universe = booster.clone();
		Random random = Utils.RANDOM;
		List<Card> booster = Lists.newArrayList();
		if (block.equalsIgnoreCase("Battle For Zendikar") && random.nextInt(488) == 42)
		{
			List<Card> expCards = Lists.newArrayList(MySQL.getSet("EXP").cards);
			booster.add(expCards.get(random.nextInt(expCards.size())));
		}
		Card foilCard = null;
		if (random.nextInt(63) < 15)
		{
			int nb = random.nextInt(128);
			Predicate<Card> p;
			if (nb == 0)
				p = card -> card.getRarity() == Rarity.MYTHIC;
			else if (nb <= 8)
				p = card -> card.getRarity() == Rarity.RARE;
			else if (nb <= 32)
				p = card -> card.getRarity() == Rarity.UNCOMMON;
			else
				p = card -> card.getRarity() == Rarity.COMMON;
			List<Card> matching = StreamSupport.stream(cards).filter(p).collect(Collectors.toList());
			foilCard = matching.get(random.nextInt(matching.size()));
			booster.add(foilCard);
			universe = ArrayUtils.removeElement(universe, "common");
		}

		for (Object o : universe)
		{
			if (o.toString().equalsIgnoreCase("[rare, mythic rare]"))
			{
				if (random.nextInt(8) == 0)
					o = "mythic";
				else
					o = "rare";
			}
			String name = o.toString().toUpperCase().replace("MYTHIC_RARE", "MYTHIC");
			if (name.equalsIgnoreCase("MARKETING") || name.contains("LAND"))
				continue;
			List<Card> matching;
			try
			{
				Rarity r = Rarity.valueOf(name);
				matching = StreamSupport.stream(cards).filter(card -> card.getRarity() == r).collect(Collectors.toList());
			} catch (IllegalArgumentException e1)
			{
				try
				{
					Layout l = Layout.valueOf(name);
					matching = StreamSupport.stream(cards).filter(card -> card.getLayout() == l).collect(Collectors.toList());
				} catch (IllegalArgumentException e2)
				{
					try
					{
						CardType t = CardType.valueOf(name);
						matching = StreamSupport.stream(cards).filter(card -> card.getType() == t).collect(Collectors.toList());
					} catch (IllegalArgumentException e3)
					{
						e3.printStackTrace();
						continue;
					}
				}
			}
			booster.add(matching.get(random.nextInt(matching.size())));
		}
		if (foilCard != null)
			booster.add(null);
		return booster.toArray(new Card[booster.size()]);
	}

	@Override
	public String toString()
	{
		return "{Set=" + geName() + ", " + cards.size() + " cards, code=" + code + ", mcic=" + magicCardsInfoCode + ", releaseDate=" + releaseDate + ", border=" + border +
				", type=" + type + ", block=" + block + "}";
	}

	public String geName()
	{
		String language = Config.getLocaleCode();
		if (translations.get(language) != null)
			return translations.get(language);
		return name;
	}

	@Override
	public int compareTo(Set o) { return releaseDate.compareTo(o.releaseDate); }

	public java.util.Set<Card> getCards()
	{
		return cards;
	}

	public void setCards(java.util.Set<Card> cards)
	{
		this.cards = cards;
	}

	public HashSet<PreconstructedDeck> getPreconstructeds()
	{
		return preconstructeds;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getMagicCardsInfoCode()
	{
		return magicCardsInfoCode;
	}

	public void setMagicCardsInfoCode(String magicCardsInfoCode)
	{
		this.magicCardsInfoCode = magicCardsInfoCode;
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

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getBlock()
	{
		return block;
	}

	public void setBlock(String block)
	{
		this.block = block;
	}

	public String getMKMName()
	{
		return mkm_name;
	}

	public void setMKMName(String mkm_name)
	{
		this.mkm_name = mkm_name;
	}

	public int getMKMId()
	{
		return mkm_id;
	}

	public void setMKMId(int mkm_id)
	{
		this.mkm_id = mkm_id;
	}

	public boolean isBuyable()
	{
		return buyable;
	}

	public void setBuyable(boolean buyable)
	{
		this.buyable = buyable;
	}

	public String getFinishedTranslations()
	{
		return finishedTranslations;
	}

	public void setFinishedTranslations(String finishedTranslations)
	{
		this.finishedTranslations = finishedTranslations;
	}

	private class LanguageData
	{
		@SuppressWarnings({"MismatchedReadAndWriteOfArray", "unused"})
		private CardLanguageData[] cards;

	}

	@SuppressWarnings("unused")
	private class CardLanguageData
	{
		private String flavor;
		private int multiverseid;
		private String originalText;
		private String originalType;
	}
}