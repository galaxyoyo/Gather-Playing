package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.galaxyoyo.gatherplaying.capacity.Capacity;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.markers.MarkerType;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixDestroyCard;
import fr.galaxyoyo.gatherplaying.server.Server;
import java8.util.stream.StreamSupport;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.HashSet;
import java.util.List;

public class PlayedCard implements Targetable
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
	private IntegerProperty damages = new SimpleIntegerProperty(0);
	private ObservableList<PlayedCard> associatedCards = FXCollections.observableArrayList();
	private PlayedCard associatedCard;
	private Card relatedCard = null;
	private boolean hidden = false;
	private boolean tapped = false;
	private boolean hand = false;

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
			String number = card.getCard().getNumber().replaceAll("[^\\d]", "");
			String letter = card.getCard().getNumber().replace(number, "");
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
		}
		else if (card.getCard().getAbilityMap().get("en") != null && card.getCard().getAbilityMap().get("en").contains("Morph"))
			relatedCard = card.getCard();
		if (Utils.getSide() == Side.CLIENT)
		{
			power.addListener((observable, oldValue, newValue) -> CardShower.getShower(this).updatePower());
			toughness.addListener((observable, oldValue, newValue) -> CardShower.getShower(this).updatePower());
			loyalty.addListener((observable, oldValue, newValue) -> CardShower.getShower(this).updatePower());
		}

		loyalty.addListener((observableValue, number, t1) -> {
			if (t1.intValue() <= 0)
				destroy(PacketMixDestroyCard.Destination.GRAVEYARD);
		});
		damages.addListener((observableValue, number, t1) -> {
			if (getDamages() >= getPower())
				destroy(PacketMixDestroyCard.Destination.GRAVEYARD);
		});
	}

	public void destroy(PacketMixDestroyCard.Destination dest)
	{
		Party p = getController().runningParty;
		OwnedCard c = new OwnedCard(getCard(), getOwner(), isFoiled());
		if (isToken())
		{
			if (Utils.getSide() == Side.CLIENT)
			{
				if (getController() == Client.localPlayer)
					Platform.runLater(() -> GameMenu.instance().creatures.getChildren().remove(CardShower.getShower(this)));
				else
					Platform.runLater(() -> GameMenu.instance().adverseCreatures.getChildren().remove(CardShower.getShower(this)));
			}
			else
			{
				PacketMixDestroyCard pkt = PacketManager.createPacket(PacketMixDestroyCard.class);
				pkt.dest = dest;
				pkt.card = this;
				pkt.index = p.getData(getController()).getPlayed().indexOf(this);
				PacketManager.sendPacketToParty(p, pkt);
			}
			Client.localPlayer.getData().getPlayed().remove(this);
			return;
		}
		if (Utils.getSide() == Side.CLIENT)
		{
			if (owner == Client.localPlayer)
			{
				if (dest == PacketMixDestroyCard.Destination.EXILE)
					GameMenu.instance().playerInfos.exile(this);
				else if (dest == PacketMixDestroyCard.Destination.GRAVEYARD)
					GameMenu.instance().playerInfos.graveyard(this);
				else if (dest == PacketMixDestroyCard.Destination.HAND)
				{
					setHand(true);
					Platform.runLater(() -> GameMenu.instance().hand.getChildren().add(new CardShower(this)));
				}
				else
					GameMenu.instance().playerInfos.setLibrary(GameMenu.instance().playerInfos.getLibrary() + 1);
			}
			else
			{
				if (dest == PacketMixDestroyCard.Destination.EXILE)
					GameMenu.instance().adverseInfos.exile(this);
				else if (dest == PacketMixDestroyCard.Destination.GRAVEYARD)
					GameMenu.instance().adverseInfos.graveyard(this);
				else if (dest == PacketMixDestroyCard.Destination.HAND)
				{
					setHand(true);
					Platform.runLater(() -> GameMenu.instance().adverseHand.getChildren().add(new CardShower(this)));
				}
				else
					GameMenu.instance().adverseInfos.addLibrary();
			}
			CardShower shower = CardShower.getShower(this);
			shower.destroy();
			if (getType().is(CardType.LAND))
			{
				if (getOwner() == Client.localPlayer)
					Platform.runLater(() -> GameMenu.instance().lands.getChildren().remove(shower));
				else
					Platform.runLater(() -> GameMenu.instance().adverseLands.getChildren().remove(shower));
			}
			else if ((getType().is(CardType.ENCHANTMENT) && !getSubtypes().contains(SubType.valueOf("Aura"))) || getType().is(CardType.ARTIFACT) ||
					getType().is(CardType.PLANESWALKER))
			{
				if (getOwner() == Client.localPlayer)
					Platform.runLater(() -> GameMenu.instance().enchants.getChildren().remove(shower));
				else
					Platform.runLater(() -> GameMenu.instance().adverseEnchants.getChildren().remove(shower));
			}
			else
			{
				if (getOwner() == Client.localPlayer)
					Platform.runLater(() -> GameMenu.instance().creatures.getChildren().remove(shower));
				else
					Platform.runLater(() -> GameMenu.instance().adverseCreatures.getChildren().remove(shower));
			}
		}
		else
		{
			PacketMixDestroyCard pkt = PacketManager.createPacket(PacketMixDestroyCard.class);
			pkt.dest = dest;
			pkt.card = this;
			pkt.index = p.getData(getController()).getPlayed().indexOf(this);
			PacketManager.sendPacketToParty(p, pkt);
			if (dest == PacketMixDestroyCard.Destination.GRAVEYARD)
				Server.sendChat(p, "chat.wasdestroyed", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>");
			else if (dest == PacketMixDestroyCard.Destination.EXILE)
				Server.sendChat(p, "chat.wasexiled", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>");
			else if (dest == PacketMixDestroyCard.Destination.HAND)
				Server.sendChat(p, "chat.tohand", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>", c
						.getOwner().name);
			else if (dest == PacketMixDestroyCard.Destination.UP_LIBRARY)
				Server.sendChat(p, "chat.touplibrary", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>", c.getOwner().name);
			else if (dest == PacketMixDestroyCard.Destination.DOWN_LIBRARY)
				Server.sendChat(p, "chat.todownlibrary", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>", c.getOwner().name);
		}
		PlayerData data = p.getData(getController());
		data.getPlayed().remove(this);
		if (dest == PacketMixDestroyCard.Destination.EXILE)
			data.getExile().add(new PlayedCard(c));
		else if (dest == PacketMixDestroyCard.Destination.GRAVEYARD)
			data.getGraveyard().add(new PlayedCard(c));
		else if (dest == PacketMixDestroyCard.Destination.HAND)
			data.getHand().add(new PlayedCard(c));
		else if (Utils.getSide() == Side.SERVER)
		{
			Library lib = data.getLibrary();
			if (dest == PacketMixDestroyCard.Destination.DOWN_LIBRARY)
				lib.addCard(c);
			else
				lib.addCardUp(c);
		}
	}

	@Override
	public int getDamages()
	{
		return damages.get();
	}

	public int getPower()
	{
		return power.get();
	}

	public void setPower(int power)
	{
		this.power.set(power);
	}

	public Player getController()
	{
		return controller;
	}

	public Card getCard() { return (Card) card.get(); }

	public Player getOwner()
	{
		return owner;
	}

	public boolean isFoiled()
	{
		return foiled;
	}

	public boolean isToken() { return card.get() instanceof Token; }

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

	public void setController(Player controller)
	{
		this.controller = controller;
	}

	@Override
	public void sendDamages(int damages)
	{
		if (type.is(CardType.CREATURE))
			this.damages.set(getDamages() + damages);
		if (type.is(CardType.PLANESWALKER))
			setLoyalty(getLoyalty() - damages);
	}

	public int getLoyalty()
	{
		return loyalty.get();
	}

	public void setLoyalty(int loyalty)
	{
		this.loyalty.set(loyalty);
	}

	@Override
	public void resetDamages()
	{
		damages.set(0);
	}

	@Override
	public Node getVisible()
	{
		return CardShower.getShower(this);
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

	public IntegerProperty loyaltyProperty()
	{
		return loyalty;
	}

	public ObjectProperty<Object> cardProperty()
	{
		return card;
	}

	public boolean hasCapacity(Class<? extends Capacity> capacity)
	{
		return getCapacities().stream().filter(c -> c.getClass() == capacity).findAny().isPresent();
	}

	public ObservableList<Capacity> getCapacities()
	{
		return capacities;
	}

	public boolean hasSummoningSickness()
	{
		return summoningSickness;
	}

	public void setSummoningSickness(boolean summoningSickness)
	{
		this.summoningSickness = summoningSickness;
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

	public boolean isHidden()
	{
		return hidden;
	}

	public void hide(boolean hidden)
	{
		this.hidden = hidden;
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

	public List<Marker> getMarkers()
	{
		return markers;
	}

	public void setMarkers(List<Marker> markers)
	{
		this.markers = markers;
	}

	public boolean isHand()
	{
		return hand;
	}

	public void setHand(boolean hand)
	{
		this.hand = hand;
	}

	public PlayedCard duplicate()
	{
		if (isCard())
			return new PlayedCard(toOwnedCard());
		else if (isToken())
			return new PlayedCard(getToken(), getController());
		else
			return null;
	}

	public OwnedCard toOwnedCard()
	{
		return new OwnedCard(getCard(), getOwner(), isFoiled());
	}

	@Override
	public int hashCode()
	{
		int result = owner.hashCode();
		result = 31 * result + (foiled ? 1 : 0);
		result = 31 * result + capacities.hashCode();
		result = 31 * result + card.get().hashCode();
		result = 31 * result + controller.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + subtypes.hashCode();
		result = 31 * result + (summoningSickness ? 1 : 0);
		result = 31 * result + markers.hashCode();
		result = 31 * result + power.get();
		result = 31 * result + toughness.get();
		result = 31 * result + loyalty.get();
		result = 31 * result + damages.hashCode();
		result = 31 * result + associatedCards.hashCode();
		result = 31 * result + (associatedCard != null ? associatedCard.hashCode() : 0);
		result = 31 * result + (relatedCard != null ? relatedCard.hashCode() : 0);
		result = 31 * result + (hidden ? 1 : 0);
		result = 31 * result + (tapped ? 1 : 0);
		result = 31 * result + (hand ? 1 : 0);
		return result;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PlayedCard that = (PlayedCard) o;

		if (foiled != that.foiled) return false;
		if (summoningSickness != that.summoningSickness) return false;
		if (hidden != that.hidden) return false;
		if (tapped != that.tapped) return false;
		if (hand != that.hand) return false;
		if (!owner.equals(that.owner)) return false;
		if (!capacities.equals(that.capacities)) return false;
		if (!card.equals(that.card)) return false;
		if (!controller.equals(that.controller)) return false;
		if (type != that.type) return false;
		if (!subtypes.equals(that.subtypes)) return false;
		if (!markers.equals(that.markers)) return false;
		if (!power.getValue().equals(that.getPower())) return false;
		if (!toughness.getValue().equals(that.getToughness())) return false;
		if (!loyalty.getValue().equals(that.getLoyalty())) return false;
		if (!damages.equals(that.damages)) return false;
		if (!associatedCards.equals(that.associatedCards)) return false;
		if (associatedCard != null ? !associatedCard.equals(that.associatedCard) : that.associatedCard != null) return false;
		return relatedCard != null ? relatedCard.equals(that.relatedCard) : that.relatedCard == null;

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

	public Token getToken() { return (Token) card.get(); }
}