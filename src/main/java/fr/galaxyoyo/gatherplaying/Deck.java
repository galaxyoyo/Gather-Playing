package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Deck
{
	private UUID uuid;
	private boolean free;
	private ObservableSet<OwnedCard> cards = FXCollections.observableSet(Sets.newHashSet());
	private ObservableSet<OwnedCard> sideboard = FXCollections.observableSet(Sets.newHashSet());
	private Player owner;
	private HashSet<Rules> legalities = Sets.newHashSet();
	private ManaColor[] colors = new ManaColor[0];
	private SimpleStringProperty name = new SimpleStringProperty("");
	private String desc = "";

	@SuppressWarnings("unchecked")
	public void calculateLegalities()
	{
		legalities = Sets.newHashSet(Rules.values());
		for (OwnedCard card : getAllCards())
		{
			if (card.getCard().isBasic())
				continue;
			((Collection<Rules>) legalities.clone()).stream().filter(r -> !card.getCard().isLegal(r)).forEach(r -> legalities.remove(r));
		}
	}

	public List<OwnedCard> getAllCards()
	{
		List<OwnedCard> list = Lists.newArrayList(cards);
		list.addAll(sideboard);
		return list;
	}

	public void calculateColors()
	{
		Map<ManaColor, Integer> counts = new DefaultHashMap<>(0);
		for (OwnedCard card : getAllCards())
		{
			for (ManaColor c : card.getCard().getColors())
				counts.put(c, counts.get(c) + 1);
		}
		//noinspection unchecked
		counts.entrySet().stream().filter(entry -> entry.getValue() < 10).forEach(counts::remove);
		colors = counts.keySet().toArray(new ManaColor[counts.size()]);
	}

	public void importDeck(Deck deck)
	{
		name = deck.name;
		desc = deck.desc;
		cards = deck.cards;
		sideboard = deck.sideboard;
		colors = deck.colors;
		legalities = deck.legalities;
	}

	public Map<String, Integer> stackedCardsByName()
	{
		Map<String, Integer> map = new DefaultHashMap<>(0);
		for (OwnedCard card : cards)
			map.put(card.getCard().getName().get("en"), map.get(card.getCard().getName().get("en")) + 1);
		return map;
	}

	public Map<String, Integer> stackedSideboardByName()
	{
		Map<String, Integer> map = new DefaultHashMap<>(0);
		for (OwnedCard card : sideboard)
			map.put(card.getCard().getName().get("en"), map.get(card.getCard().getName().get("en")) + 1);
		return map;
	}

	public Map<CardType, Map<Card, AtomicInteger>> cardsByType()
	{
		Map<CardType, Map<Card, AtomicInteger>> map = new DefaultTreeMap<>(Enum::compareTo, () -> new DefaultTreeMap<>((o1, o2) -> {
			int ret = String.CASE_INSENSITIVE_ORDER.compare(o1.getTranslatedName().get(), o2.getTranslatedName().get());
			if (ret == 0)
				ret = o1.compareTo(o2);
			return ret;
		}, () -> new AtomicInteger(0)));
		for (OwnedCard card : cards)
			map.get(card.getCard().getType()).get(card.getCard()).incrementAndGet();
		return map;
	}

	public Map<CardType, Map<Card, AtomicInteger>> sideboardByType()
	{
		Map<CardType, Map<Card, AtomicInteger>> map = new DefaultTreeMap<>(Enum::compareTo, () -> new DefaultTreeMap<>((o1, o2) -> {
			int ret = String.CASE_INSENSITIVE_ORDER.compare(o1.getTranslatedName().get(), o2.getTranslatedName().get());
			if (ret == 0)
				ret = o1.compareTo(o2);
			return ret;
		}, () -> new AtomicInteger(0)));
		for (OwnedCard card : sideboard)
			map.get(card.getCard().getType()).get(card.getCard()).incrementAndGet();
		return map;
	}

	@Override
	public int hashCode()
	{
		return uuid.hashCode();
	}

	@Override
	public String toString()
	{
		return name.getValue();
	}

	public UUID getUuid()
	{
		return uuid;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public boolean isFree()
	{
		return free;
	}

	public void setFree(boolean free)
	{
		this.free = free;
	}

	public ObservableSet<OwnedCard> getCards()
	{
		return cards;
	}

	public void setCards(ObservableSet<OwnedCard> cards)
	{
		this.cards = cards;
	}

	public ObservableSet<OwnedCard> getSideboard()
	{
		return sideboard;
	}

	public void setSideboard(ObservableSet<OwnedCard> sideboard)
	{
		this.sideboard = sideboard;
	}

	public Player getOwner()
	{
		return owner;
	}

	public void setOwner(Player owner)
	{
		this.owner = owner;
	}

	public HashSet<Rules> getLegalities()
	{
		return legalities;
	}

	public void setLegalities(HashSet<Rules> legalities)
	{
		this.legalities = legalities;
	}

	public ManaColor[] getColors()
	{
		return colors;
	}

	public void setColors(ManaColor[] colors)
	{
		this.colors = colors;
	}

	public StringProperty nameProperty()
	{
		return name;
	}

	public String getName()
	{
		return name.get();
	}

	public void setName(String name)
	{
		this.name.set(name);
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}
}