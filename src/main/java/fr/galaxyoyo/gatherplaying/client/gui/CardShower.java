package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.markers.MarkerType;
import fr.galaxyoyo.gatherplaying.protocol.packets.*;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Map;
import java.util.concurrent.Executors;

public class CardShower extends AnchorPane
{
	private static final Map<PlayedCard, CardShower> showers = Maps.newHashMap();
	public final PlayedCard played;
	private final ImageView view;
	private final OwnedCard hand;
	private Arrow currentArrow = null;
	private Text powerText = null;

	private boolean revealed = false;

	public CardShower(PlayedCard played)
	{
		super();
		view = new ImageView();
		if (Utils.getSide() == Side.CLIENT)
			view.imageProperty().bind(Bindings.createObjectBinding(() -> CardImageManager.getImage(played), played.card));
		view.setFitWidth(74.0D);
		view.setFitHeight(103.0D);
		setMinSize(103.0D, 103.0D);
		setMaxSize(103.0D, 103.0D);
		setPrefSize(103.0D, 103.0D);
		view.setSmooth(true);
		getChildren().add(view);
		this.played = played;
		this.hand = null;
		played.associatedCards.addListener((ListChangeListener<? super PlayedCard>) c -> {
			ObservableList<PlayedCard> associatedCards = played.associatedCards;
			getChildren().clear();
			for (int i = 0; i < associatedCards.size(); i++)
			{
				PlayedCard associated = associatedCards.get(i);
				CardShower shower = getShower(associated);
				if (associated.associatedCard == null)
				{
					associated.associatedCard = played;
					HBox box = (HBox) shower.getParent();
					box.getChildren().remove(shower);
				}
				shower.setTranslateX(14 * i);
				shower.setTranslateY(-14 * i);
				shower.setRotate(90.0D);
				getChildren().add(shower);
			}
			getChildren().add(view);
			updatePower();
		});
		setOnMouseEntered(event -> GameMenu.INSTANCE.setImage(played));
		setOnMouseReleased(event -> {
			if (!played.type.isPermanent())
				return;
			int index = Client.localPlayer.runningParty.getData(played.controller).getPlayed().indexOf(played);
			if (event.getButton() == MouseButton.PRIMARY && Client.localPlayer == played.controller && event.getClickCount() == 2)
			{
				PacketMixTapCard pkt = PacketManager.createPacket(PacketMixTapCard.class);
				pkt.shouldTap = !played.isTapped();
				pkt.card = played;
				pkt.index = index;
				PacketManager.sendPacketToServer(pkt);
			}
			else if (event.getButton() == MouseButton.SECONDARY)
			{
				if (currentArrow != null)
				{
					CardShower dest = null;
					for (CardShower shower : showers.values())
					{
						if (shower.localToScene(shower.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY()))
						{
							dest = shower;
							break;
						}
					}
					Client.getStackPane().getChildren().remove(currentArrow);
					currentArrow = null;
					if (dest == null || dest == this)
						return;
					PacketMixDrawLine pkt = PacketManager.createPacket(PacketMixDrawLine.class);
					pkt.from = played;
					pkt.to = dest.played;
					PacketManager.sendPacketToServer(pkt);
					return;
				}
				if (Client.localPlayer != played.controller)
				{
					ContextMenu menu = new ContextMenu();
					MenuItem gainControl = new MenuItem("Acquérir le contrôle");
					gainControl.setOnAction(e -> {
						PacketMixGainControl pkt = PacketManager.createPacket(PacketMixGainControl.class);
						pkt.index = index;
						pkt.oldController = played.controller;
						pkt.newController = Client.localPlayer;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(gainControl);
					menu.show(this, event.getScreenX(), event.getScreenY());
					return;
				}
				ContextMenu menu = new ContextMenu();
				MenuItem tap = new MenuItem("Engager");
				tap.setOnAction(e -> {
					PacketMixTapCard pkt = PacketManager.createPacket(PacketMixTapCard.class);
					pkt.shouldTap = true;
					pkt.card = played;
					pkt.index = index;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(tap);
				MenuItem untap = new MenuItem("Dégager");
				untap.setOnAction(e -> {
					PacketMixTapCard pkt = PacketManager.createPacket(PacketMixTapCard.class);
					pkt.shouldTap = false;
					pkt.card = played;
					pkt.index = index;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(untap);
				if (played.isCard() && ((played.getCard().ability.get("en") != null && played.getCard().ability.get("en").contains("Morph") && played.relatedCard == null) ||
										played.getCard().layout == Layout.DOUBLE_FACED || played.getCard().layout == Layout.SPLIT || played.getCard().layout == Layout.FLIP))
				{
					menu.getItems().add(new SeparatorMenuItem());
					MenuItem ret = new MenuItem("Retourner");
					ret.setOnAction(e -> {
						PacketMixReturnCard pkt = PacketManager.createPacket(PacketMixReturnCard.class);
						pkt.index = (short) Client.getRunningParty().getData(played.controller).getPlayed().indexOf(played);
						pkt.p = played.controller;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(ret);
				}
				menu.getItems().add(new SeparatorMenuItem());
				Menu addMarker = new Menu("Ajouter un marqueur ...");
				for (MarkerType type : MarkerType.values())
				{
					if (!type.isApplicable(played.type))
						continue;
					MenuItem item = new MenuItem(type.getTranslatedName());
					item.setOnAction(e -> {
						PacketMixAddMarker pkt = PacketManager.createPacket(PacketMixAddMarker.class);
						pkt.card = played;
						pkt.type = type;
						PacketManager.sendPacketToServer(pkt);
					});
					addMarker.getItems().add(item);
				}
				if (addMarker.getItems().size() > 0)
					menu.getItems().add(addMarker);
				Menu removeMarker = new Menu("Retirer un marqueur ...");
				for (Marker marker : played.markers)
				{
					MenuItem item = new MenuItem(marker.getType().getTranslatedName());
					item.setOnAction(e -> {
						PacketMixRemoveMarker pkt = PacketManager.createPacket(PacketMixRemoveMarker.class);
						pkt.card = played;
						pkt.index = (short) played.markers.indexOf(marker);
						PacketManager.sendPacketToServer(pkt);
					});
					removeMarker.getItems().add(item);
				}
				if (removeMarker.getItems().size() > 0)
					menu.getItems().add(removeMarker);
				if (played.type.is(CardType.CREATURE))
				{
					MenuItem setLife = new MenuItem("Définir les stats");
					setLife.setOnAction(e -> {
						for (Marker m : played.markers)
							m.onCardUnmarked(played);
						Dialog<ButtonType> dialog = new Dialog<>();
						GridPane pane = new GridPane();
						pane.getChildren().add(new Label("Force :"));
						Label toughnessLbl = new Label("Endurance :");
						pane.getChildren().add(toughnessLbl);
						GridPane.setRowIndex(toughnessLbl, 1);
						Spinner<Integer> power = new Spinner<>(0, 0x7FFFFFFF, played.power.get());
						pane.getChildren().add(power);
						GridPane.setColumnIndex(power, 1);
						Spinner<Integer> toughness = new Spinner<>(0, 0x7FFFFFFF, played.toughness.get());
						pane.getChildren().add(toughness);
						GridPane.setRowIndex(toughness, 1);
						GridPane.setColumnIndex(toughness, 1);
						dialog.getDialogPane().setContent(pane);
						dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
						dialog.showAndWait().ifPresent(e1 -> {
							if (e1 == ButtonType.APPLY)
							{
								PacketMixSetLife pkt = PacketManager.createPacket(PacketMixSetLife.class);
								pkt.card = played;
								pkt.newPower = power.getValue();
								pkt.newToughness = toughness.getValue();
								PacketManager.sendPacketToServer(pkt);
							}
						});
					});
					menu.getItems().add(setLife);
				}
				if (addMarker.getItems().size() > 0)
					menu.getItems().add(new SeparatorMenuItem());
				MenuItem setType = new MenuItem("Changer le type ...");
				setType.setOnAction(e -> {
					ChoiceDialog<CardType> dialog = new ChoiceDialog<>();
					dialog.getItems().addAll(CardType.values());
					dialog.setSelectedItem(played.type);
					dialog.showAndWait().ifPresent(cardType -> {
						if (cardType != null)
						{
							PacketMixSetCardType pkt = PacketManager.createPacket(PacketMixSetCardType.class);
							pkt.card = played;
							pkt.newType = cardType;
							PacketManager.sendPacketToServer(pkt);
						}
					});
				});
				menu.getItems().add(setType);
				MenuItem subtypes = new MenuItem("Définir les sous-types ...");
				subtypes.setOnAction(e -> {
					Dialog<ButtonType> dialog = new Dialog<>();
					GridPane pane = new GridPane();
					dialog.getDialogPane().setContent(pane);
					dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
					int x = 0, y = 0;
					Map<SubType, CheckBox> map = Maps.newHashMap();
					for (SubType type : SubType.values())
					{
						CheckBox box = new CheckBox(type.toString());
						box.setSelected(played.subtypes.contains(type));
						map.put(type, box);
						pane.getChildren().add(box);
						GridPane.setRowIndex(box, y);
						GridPane.setColumnIndex(box, x);
						++x;
						if (x >= 10)
						{
							++y;
							x = 0;
						}
					}
					dialog.showAndWait().ifPresent(buttonType -> {
						if (buttonType == ButtonType.APPLY)
						{
							PacketMixSetCardSubtypes pkt = PacketManager.createPacket(PacketMixSetCardSubtypes.class);
							pkt.card = played;
							pkt.newSubtypes = Sets.newHashSet();
							//noinspection unchecked
							pkt.newSubtypes
									.addAll(StreamSupport.stream(map.entrySet()).filter(entry -> entry.getValue().isSelected()).map(Map.Entry::getKey).collect(Collectors.toList
											()));
							PacketManager.sendPacketToServer(pkt);
						}
					});
				});
				menu.getItems().add(subtypes);
				menu.getItems().add(new SeparatorMenuItem());
				MenuItem invokeToken = new MenuItem("Invoquer un jeton");
				invokeToken.setOnAction(e -> {
					ChoiceDialog<Token> dialog = new ChoiceDialog<>();
					dialog.getItems().addAll(Token.values());
					dialog.setSelectedItem(dialog.getItems().get(0));
					dialog.showAndWait().ifPresent(token -> {
						PacketMixInvokeToken pkt = PacketManager.createPacket(PacketMixInvokeToken.class);
						pkt.token = token;
						pkt.p = played.controller;
						PacketManager.sendPacketToServer(pkt);
					});
				});
				menu.getItems().add(invokeToken);
				menu.getItems().add(new SeparatorMenuItem());
				MenuItem graveyard = new MenuItem("Envoyer au cimetière");
				graveyard.setOnAction(e -> {
					PacketMixDestroyCard pkt = PacketManager.createPacket(PacketMixDestroyCard.class);
					pkt.dest = PacketMixDestroyCard.Destination.GRAVEYARD;
					pkt.card = played;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(graveyard);
				MenuItem exile = new MenuItem("Exiler");
				exile.setOnAction(e -> {
					PacketMixDestroyCard pkt = PacketManager.createPacket(PacketMixDestroyCard.class);
					pkt.dest = PacketMixDestroyCard.Destination.EXILE;
					pkt.card = played;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(exile);
				MenuItem hand = new MenuItem("Renvoyer dans la main");
				hand.setOnAction(e -> {
					PacketMixDestroyCard pkt = PacketManager.createPacket(PacketMixDestroyCard.class);
					pkt.dest = PacketMixDestroyCard.Destination.HAND;
					pkt.card = played;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(hand);
				if (Client.getRunningParty().currentSpell != null)
				{
					for (MenuItem item : menu.getItems())
						item.setDisable(true);
				}
				menu.show(CardShower.this, event.getScreenX(), event.getScreenY());
			}
		});
		setOnMouseDragged(event -> {
			if (event.getButton() == MouseButton.PRIMARY)
			{
				if (played.controller != Client.localPlayer)
					return;

				CardShower dest = null;
				for (CardShower shower : showers.values())
				{
					if (shower != this && shower.localToScene(shower.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY()))
					{
						dest = shower;
						break;
					}
				}
				if (dest != null && dest != this && dest.played.associatedCard == null)
				{
					double diff = event.getSceneX() - dest.localToScene(dest.getBoundsInLocal()).getMinX();
					if (played.associatedCard != null)
					{
						CardShower shower = getShower(played.associatedCard);
						((HBox) shower.getParent()).getChildren().add(this);
						played.associatedCard.associatedCards.remove(played);
						played.associatedCard = null;
						setTranslateX(0.0D);
						setTranslateY(0.0D);
						setRotate(42.0D);
					}

					if (diff > 20.0D && diff < 54.0D)
					{
						dest.played.associatedCards.add(played);
					}
					else
					{
						PacketMixMoveCard pkt = PacketManager.createPacket(PacketMixMoveCard.class);
						pkt.dataPos = (short) played.controller.getData().getPlayed().indexOf(played);
						if (dest.played.associatedCard == null)
							pkt.newPos = (short) ((HBox) dest.getParent()).getChildren().indexOf(dest);
						else
							pkt.newPos = (short) ((HBox) dest.getParent().getParent()).getChildren().indexOf(dest);
						pkt.parentId = getParent().getId();
						pkt.p = played.controller;
						PacketManager.sendPacketToServer(pkt);
					}
				}
			}
			else if (event.getButton() == MouseButton.SECONDARY)
			{
				if (currentArrow == null)
				{
					currentArrow = new Arrow();
					currentArrow.setX1(event.getSceneX());
					currentArrow.setY1(event.getSceneY());
				}
				currentArrow.setX2(event.getSceneX());
				currentArrow.setY2(event.getSceneY());
			}
		});
		showers.put(played, this);
		if (Utils.getSide() == Side.CLIENT)
		{
			updatePower();
			updateTap();
		}
	}

	public CardShower(OwnedCard hand)
	{
		super();
		view = new ImageView();
		if (Utils.getSide() == Side.CLIENT)
		{
			Platform.runLater(() -> view.setImage(CardImageManager.getImage(hand.getOwner() == Client.localPlayer ? hand.getCard() : null)));
			if (hand.getOwner() != Client.localPlayer)
				setRotate(180.0D);
		}
		view.setFitWidth(74.0D);
		view.setFitHeight(103.0D);
		getChildren().add(view);
		this.played = null;
		this.hand = hand;
		setOnMouseEntered(event -> {
			if (revealed || hand.getOwner() == Client.localPlayer)
				GameMenu.INSTANCE.setImage(hand.getCard());
		});
		setOnMouseReleased(event -> {
			if (hand.getOwner() != Client.localPlayer)
				return;
			if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
			{
				if ((Client.getRunningParty().player != Client.localPlayer || !Client.getRunningParty().getCurrentPhase().isMain() ||
					 Client.getRunningParty().currentSpell != null) && !hand.getCard().type.is(CardType.INSTANT) &&
					(hand.getCard().ability.get("en") == null || !hand.getCard().ability.get("en").contains("Flash")))
					return;
				PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
				pkt.card = hand;
				pkt.action = PacketMixPlayCard.Action.PLAY;
				PacketManager.sendPacketToServer(pkt);
			}
			else if (event.getButton() == MouseButton.SECONDARY)
			{
				ContextMenu menu = new ContextMenu();

				MenuItem play = new MenuItem("Jouer");
				play.setOnAction(e -> {
					PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
					pkt.card = hand;
					pkt.action = PacketMixPlayCard.Action.PLAY;
					PacketManager.sendPacketToServer(pkt);
				});
				if ((Client.getRunningParty().player != Client.localPlayer || !Client.getRunningParty().getCurrentPhase().isMain() ||
					 Client.getRunningParty().currentSpell != null) && !hand.getCard().type.is(CardType.INSTANT) &&
					(hand.getCard().ability.get("en") == null || !hand.getCard().ability.get("en").contains("Flash")))
					play.setDisable(true);
				menu.getItems().add(play);

				MenuItem playHided = new MenuItem("Jouer retournée");
				playHided.setOnAction(e -> {
					PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
					pkt.card = hand;
					pkt.action = PacketMixPlayCard.Action.PLAY;
					pkt.hided = true;
					PacketManager.sendPacketToServer(pkt);
				});
				if ((Client.getRunningParty().player != Client.localPlayer || !Client.getRunningParty().getCurrentPhase().isMain() ||
					 Client.getRunningParty().currentSpell != null) && !hand.getCard().type.is(CardType.INSTANT) &&
					(hand.getCard().ability.get("en") == null || !hand.getCard().ability.get("en").contains("Flash")))
					playHided.setDisable(true);
				if ((hand.getCard().ability.get("en") != null && (hand.getCard().ability.get("en").contains("Morph") || hand.getCard().ability.get("en").contains("Manifest"))) ||
					hand.getCard().layout == Layout.DOUBLE_FACED || hand.getCard().layout == Layout.SPLIT || hand.getCard().layout == Layout.FLIP)
					menu.getItems().add(playHided);
				MenuItem discard = new MenuItem("Défausser");
				discard.setOnAction(e -> {
					PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
					pkt.card = hand;
					pkt.action = PacketMixPlayCard.Action.DISCARD;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(discard);
				MenuItem exile = new MenuItem("Exiler");
				exile.setOnAction(e -> {
					PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
					pkt.card = hand;
					pkt.action = PacketMixPlayCard.Action.EXILE;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(exile);
				MenuItem reveal = new MenuItem("Révéler");
				reveal.setOnAction(e -> {
					PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
					pkt.card = hand;
					pkt.action = PacketMixPlayCard.Action.REVEAL;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(reveal);
				MenuItem upLib = new MenuItem("Placer sur la biblithèque");
				upLib.setOnAction(e -> {
					PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
					pkt.card = hand;
					pkt.action = PacketMixPlayCard.Action.UP_LIBRARY;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(upLib);
				MenuItem downLib = new MenuItem("Placer sous la biblithèque");
				downLib.setOnAction(e -> {
					PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
					pkt.card = hand;
					pkt.action = PacketMixPlayCard.Action.DOWN_LIBRARY;
					PacketManager.sendPacketToServer(pkt);
				});
				menu.getItems().add(downLib);

				menu.show(CardShower.this, event.getScreenX(), event.getScreenY());
			}
		});
	}

	public static CardShower getShower(PlayedCard card)
	{
		return showers.get(card);
	}

	public void reveal()
	{
		assert hand != null;
		if (revealed)
			return;
		revealed = true;
		view.setImage(CardImageManager.getImage(hand.getCard()));
		Executors.newSingleThreadExecutor().submit(() -> {
			try
			{
				Thread.sleep(5000L);
				revealed = false;
				view.setImage(CardImageManager.getImage(hand.getOwner() == Client.localPlayer ? hand.getCard() : null));
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		});
	}

	public void destroy()
	{
		showers.remove(played);
	}

	public void updateTap()
	{
		Platform.runLater(() -> {
			double base = 0.0D;
			if (played.controller != Client.localPlayer)
				base = 180.0D;
			if (played.isTapped())
				setRotate(90.0D + base);
			else
				setRotate(base);
		});
	}

	public void updatePower()
	{
		Platform.runLater(() -> {
			if (powerText != null)
				getChildren().remove(powerText);
			powerText = new Text();
			if (played.type.is(CardType.CREATURE))
				powerText.setText(played.power.get() + "/" + played.toughness.get());
			else if (played.type.is(CardType.PLANESWALKER))
				powerText.setText(played.loyalty.getValue().toString());
			powerText.setFill(Color.YELLOW);
			powerText.setFont(new Font(10.0D));
			powerText.applyCss();
			powerText.setTranslateX(73.0D - powerText.getLayoutBounds().getWidth());
			powerText.setTranslateY(101.0D);
			Rectangle rect = new Rectangle();
			rect.setFill(Color.BLACK);
			rect.setWidth(powerText.getLayoutBounds().getWidth());
			rect.setHeight(powerText.getLayoutBounds().getHeight() - 2);
			rect.translateXProperty().bind(powerText.translateXProperty());
			rect.translateYProperty().bind(powerText.translateYProperty().subtract(powerText.getLayoutBounds().getHeight() - 4));
			getChildren().add(rect);
			getChildren().add(powerText);
		});
	}
}
