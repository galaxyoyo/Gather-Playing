package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Library
{
	private LinkedList<OwnedCard> cards = Lists.newLinkedList();
	private Player owner;

	public Library(Deck deck)
	{
		this(deck.owner);
		cards = Lists.newLinkedList(deck.cards);
		shuffle();
	}

	public Library(Player owner) { this.owner = owner; }

	public void addCard(OwnedCard card) { cards.offerLast(card); }

	public void addCardUp(OwnedCard card) { cards.addFirst(card); }

	public void addCards(Collection<OwnedCard> cards)
	{
		this.cards.addAll(cards);
	}

	public void removeCard(OwnedCard card) { cards.remove(card); }

	public OwnedCard drawCard() { return cards.poll(); }

	public void shuffle() { Collections.shuffle(cards, Utils.RANDOM); }

	public Player getOwner() { return owner; }

	public List<OwnedCard> getSortedCards() { return cards; }
}