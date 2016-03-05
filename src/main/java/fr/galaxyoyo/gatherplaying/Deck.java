package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java8.util.stream.StreamSupport;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Deck
{
	public UUID uuid;
	public boolean free;
	public ObservableSet<OwnedCard> cards = FXCollections.observableSet(Sets.newHashSet());
	public ObservableSet<OwnedCard> sideboard = FXCollections.observableSet(Sets.newHashSet());
	public Player owner;
	public HashSet<Rules> legalities = Sets.newHashSet();
	public ManaColor[] colors = new ManaColor[0];
	public SimpleStringProperty name = new SimpleStringProperty("");
	public String desc = "";

	@SuppressWarnings("unchecked")
	public void calculateLegalities()
	{
		legalities = Sets.newHashSet(Rules.values());
		for (OwnedCard card : getAllCards())
		{
			if (card.getCard().basic)
				continue;
			StreamSupport.stream((Collection<Rules>) legalities.clone()).filter(r -> !card.getCard().isLegal(r)).forEach(r -> legalities.remove(r));
		}
	}

	public void calculateColors()
	{
		Map<ManaColor, Integer> counts = new DefaultHashMap<>(0);
		for (OwnedCard card : getAllCards())
		{
			for (ManaColor c : card.getCard().colors)
				counts.put(c, counts.get(c) + 1);
		}
		//noinspection unchecked
		StreamSupport.stream(counts.entrySet()).filter(entry -> entry.getValue() < 10).forEach(counts::remove);
		colors = counts.keySet().toArray(new ManaColor[counts.size()]);
	}

	public List<OwnedCard> getAllCards()
	{
		List<OwnedCard> list = Lists.newArrayList(cards);
		list.addAll(sideboard);
		return list;
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
			map.put(card.getCard().name.get("en"), map.get(card.getCard().name.get("en")) + 1);
		return map;
	}

	public Map<String, Integer> stackedSideboardByName()
	{
		Map<String, Integer> map = new DefaultHashMap<>(0);
		for (OwnedCard card : sideboard)
			map.put(card.getCard().name.get("en"), map.get(card.getCard().name.get("en")) + 1);
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
			map.get(card.getCard().type).get(card.getCard()).incrementAndGet();
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
			map.get(card.getCard().type).get(card.getCard()).incrementAndGet();
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
}