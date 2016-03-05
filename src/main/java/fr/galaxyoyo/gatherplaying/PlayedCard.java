package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.galaxyoyo.gatherplaying.capacity.Capacity;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import java8.util.stream.StreamSupport;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.List;

public class PlayedCard
{
	public final Player owner;
	public final boolean foiled;
	public final ObservableList<Capacity> capacities;
	public ObjectProperty<Object> card;
	public Player controller;
	public CardType type;
	public HashSet<SubType> subtypes;
	public boolean tapped = false;
	public boolean summoningSickness;
	public List<Marker> markers = Lists.newArrayList();
	public IntegerProperty power = new SimpleIntegerProperty(0), toughness = new SimpleIntegerProperty(0), loyalty = new SimpleIntegerProperty(0);
	public ObservableList<PlayedCard> associatedCards = FXCollections.observableArrayList();
	public PlayedCard associatedCard;
	public Card relatedCard = null;
	public boolean hided = false;

	public PlayedCard(OwnedCard card)
	{
		this.card = new SimpleObjectProperty<>(card.getCard());
		this.owner = card.getOwner();
		this.controller = this.owner;
		this.foiled = card.isFoiled();
		this.type = card.getCard().type;
		this.subtypes = Sets.newHashSet(card.getCard().subtypes);
		this.summoningSickness = type.is(CardType.CREATURE);
		this.capacities = Capacity.guessCapacities(this);
		if (card.getCard().layout == Layout.DOUBLE_FACED || card.getCard().layout == Layout.FLIP || card.getCard().layout == Layout.SPLIT)
		{
			String number = card.getCard().mciNumber.replaceAll("[^\\d]", "");
			String letter = card.getCard().mciNumber.replace(number, "");
			if (letter.equals("a"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.set == card.getCard().set && (number + "b").equals(c.mciNumber)).findAny().get();
			else if (letter.equals("b") && card.getCard().muId.get("en").equals("74358"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.set == card.getCard().set && (number + "c").equals(c.mciNumber)).findAny().get();
			else if (letter.equals("b"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.set == card.getCard().set && (number + "a").equals(c.mciNumber)).findAny().get();
			else if (letter.equals("c"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.set == card.getCard().set && (number + "d").equals(c.mciNumber)).findAny().get();
			else if (letter.equals("d"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.set == card.getCard().set && (number + "e").equals(c.mciNumber)).findAny().get();
			else
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.set == card.getCard().set && (number + "a").equals(c.mciNumber)).findAny().get();
		}
		else if (card.getCard().ability.get("en") != null && card.getCard().ability.get("en").contains("Morph"))
			relatedCard = card.getCard();
		if (Utils.getSide() == Side.CLIENT)
		{
			power.addListener((observable, oldValue, newValue) -> CardShower.getShower(this).updatePower());
			toughness.addListener((observable, oldValue, newValue) -> CardShower.getShower(this).updatePower());
			loyalty.addListener((observable, oldValue, newValue) -> CardShower.getShower(this).updatePower());
		}
	}

	public PlayedCard(Token token, Player player)
	{
		this.card = new SimpleObjectProperty<>(token);
		this.owner = player;
		this.controller = player;
		this.foiled = false;
		this.type = token.type;
		this.subtypes = Sets.newHashSet(token.subtypes);
		if (Utils.getSide() == Side.CLIENT)
		{
			this.power.addListener((observable, oldValue, newValue) -> CardShower.getShower(this).updatePower());
			this.toughness.addListener((observable, oldValue, newValue) -> CardShower.getShower(this).updatePower());
		}
		this.capacities = FXCollections.emptyObservableList();
	}

	public boolean isTapped() { return tapped; }

	public void tap()
	{
		tapped = true;
	}

	public void untap()
	{
		tapped = false;
	}

	public boolean isCard() { return card.get() instanceof Card; }

	public Card getCard() { return (Card) card.get(); }

	public boolean isToken() { return card.get() instanceof Token; }

	public Token getToken() { return (Token) card.get(); }

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof PlayedCard))
			return false;
		PlayedCard other = (PlayedCard) obj;
		return owner == other.owner && card == other.card && foiled == other.foiled && capacities.equals(other.capacities) && tapped == other.tapped && this == obj;
	}

	public StringBinding getTranslatedName()
	{
		if (isCard())
			return getCard().getTranslatedName();
		else
			return getToken().getTranslatedName();
	}

	@Override
	public String toString()
	{
		return getTranslatedName().get();
	}
}