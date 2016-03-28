package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.galaxyoyo.gatherplaying.capacity.Capacity;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.markers.MarkerType;
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
	private final Player owner;
	private final boolean foiled;
	private final ObservableList<Capacity> capacities;
	public ObjectProperty<Object> card;
	private Player controller;
	private CardType type;
	private HashSet<SubType> subtypes;
	private boolean summoningSickness;
	private List<Marker> markers = Lists.newArrayList();
	private IntegerProperty power = new SimpleIntegerProperty(0);
	private IntegerProperty toughness = new SimpleIntegerProperty(0);
	private IntegerProperty loyalty = new SimpleIntegerProperty(0);
	private ObservableList<PlayedCard> associatedCards = FXCollections.observableArrayList();
	private PlayedCard associatedCard;
	private Card relatedCard = null;
	private boolean hided = false;
	private boolean tapped = false;

	public PlayedCard(OwnedCard card)
	{
		this.card = new SimpleObjectProperty<>(card.getCard());
		this.owner = card.getOwner();
		this.controller = this.owner;
		this.foiled = card.isFoiled();
		this.type = card.getCard().getType();
		this.subtypes = Sets.newHashSet(card.getCard().getSubtypes());
		this.summoningSickness = type.is(CardType.CREATURE);
		this.capacities = Capacity.guessCapacities(this);
		if (card.getCard().getLayout() == Layout.DOUBLE_FACED || card.getCard().getLayout() == Layout.FLIP || card.getCard().getLayout() == Layout.SPLIT)
		{
			String number = card.getCard().getMciNumber().replaceAll("[^\\d]", "");
			String letter = card.getCard().getMciNumber().replace(number, "");
			if (letter.equals("a"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.getSet() == card.getCard().getSet() && (number + "b").equals(c.getMciNumber())).findAny().get();
			else if (letter.equals("b") && card.getCard().getMuId("en").equals("74358"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.getSet() == card.getCard().getSet() && (number + "c").equals(c.getMciNumber())).findAny().get();
			else if (letter.equals("b"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.getSet() == card.getCard().getSet() && (number + "a").equals(c.getMciNumber())).findAny().get();
			else if (letter.equals("c"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.getSet() == card.getCard().getSet() && (number + "d").equals(c.getMciNumber())).findAny().get();
			else if (letter.equals("d"))
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.getSet() == card.getCard().getSet() && (number + "e").equals(c.getMciNumber())).findAny().get();
			else
				relatedCard = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.getSet() == card.getCard().getSet() && (number + "a").equals(c.getMciNumber())).findAny().get();
		} else if (card.getCard().getAbilityMap().get("en") != null && card.getCard().getAbilityMap().get("en").contains("Morph"))
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
		this.type = token.getType();
		this.subtypes = Sets.newHashSet(token.getSubtypes());
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

	public boolean isToken() { return card.get() instanceof Token; }

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

	@Override
	public String toString()
	{
		return getTranslatedName().get();
	}

	public StringBinding getTranslatedName()
	{
		if (isCard())
			return getCard().getTranslatedName();
		else
			return getToken().getTranslatedName();
	}

	public boolean isCard() { return card.get() instanceof Card; }

	public Card getCard() { return (Card) card.get(); }

	public Token getToken() { return (Token) card.get(); }

	public int getPower()
	{
		return power.get();
	}

	public void setPower(int power)
	{
		this.power.set(power);
	}

	public IntegerProperty powerProperty()
	{
		return power;
	}

	public int getToughness()
	{
		return toughness.get();
	}

	public void setToughness(int toughness)
	{
		this.toughness.set(toughness);
	}

	public IntegerProperty toughnessProperty()
	{
		return toughness;
	}

	public int getLoyalty()
	{
		return loyalty.get();
	}

	public void setLoyalty(int loyalty)
	{
		this.loyalty.set(loyalty);
	}

	public IntegerProperty loyaltyProperty()
	{
		return loyalty;
	}

	public ObjectProperty<Object> cardProperty()
	{
		return card;
	}

	public Player getOwner()
	{
		return owner;
	}

	public boolean isFoiled()
	{
		return foiled;
	}

	public ObservableList<Capacity> getCapacities()
	{
		return capacities;
	}

	public Player getController()
	{
		return controller;
	}

	public void setController(Player controller)
	{
		this.controller = controller;
	}

	public CardType getType()
	{
		return type;
	}

	public void setType(CardType type)
	{
		this.type = type;
	}

	public HashSet<SubType> getSubtypes()
	{
		return subtypes;
	}

	public void setSubtypes(HashSet<SubType> subtypes)
	{
		this.subtypes = subtypes;
	}

	public boolean isSummoningSickness()
	{
		return summoningSickness;
	}

	public void setSummoningSickness(boolean summoningSickness)
	{
		this.summoningSickness = summoningSickness;
	}

	public List<Marker> getMarkers()
	{
		return markers;
	}

	public void setMarkers(List<Marker> markers)
	{
		this.markers = markers;
	}

	public ObservableList<PlayedCard> getAssociatedCards()
	{
		return associatedCards;
	}

	public void setAssociatedCards(ObservableList<PlayedCard> associatedCards)
	{
		this.associatedCards = associatedCards;
	}

	public PlayedCard getAssociatedCard()
	{
		return associatedCard;
	}

	public void setAssociatedCard(PlayedCard associatedCard)
	{
		this.associatedCard = associatedCard;
	}

	public Card getRelatedCard()
	{
		return relatedCard;
	}

	public void setRelatedCard(Card relatedCard)
	{
		this.relatedCard = relatedCard;
	}

	public boolean isHided()
	{
		return hided;
	}

	public void setHided(boolean hided)
	{
		this.hided = hided;
	}

	public void setDefaultStats()
	{
		if (isCard())
		{
			if (getCard().getType().is(CardType.CREATURE))
			{
				try
				{
					setPower(Integer.parseInt(getCard().getPower()));
				}
				catch (NumberFormatException ex)
				{
					setPower(0);
				}
				try
				{
					setToughness(Integer.parseInt(getCard().getToughness()));
				}
				catch (NumberFormatException ex)
				{
					setToughness(0);
				}
			}
			else if (getCard().getType().is(CardType.PLANESWALKER))
			{
				setLoyalty(0);
				for (int i = 0; i < getCard().getLoyalty(); ++i)
				{
					Marker m = MarkerType.LOYALTY.newInstance();
					m.onCardMarked(this);
					getMarkers().add(m);
				}
			}
		}
	}
}