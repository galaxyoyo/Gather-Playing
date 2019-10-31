package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.markers.MarkerType;
import fr.galaxyoyo.gatherplaying.protocol.packets.*;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CardShower extends AnchorPane
{
	private static final java.util.Set<CardShower> showers = Sets.newHashSet();
	public final PlayedCard card;
	private final ImageView view;
	private Arrow currentArrow = null;
	private Text powerText = null;

	private boolean revealed = false;

	public CardShower(PlayedCard card)
	{
		super();
		view = new ImageView();
		this.card = card;
		reload();
	}

	public void reload()
	{
		if (view.imageProperty().isBound())
			view.imageProperty().unbind();
		getChildren().clear();
		if (card.isHand())
		{
			if (Utils.getSide() == Side.CLIENT)
			{
				Platform.runLater(() -> view.setImage(CardImageManager.getImage(card.getOwner() == Client.localPlayer ? card.getCard() : null)));
				if (card.getController() != Client.localPlayer)
					setRotate(180.0D);
				else
					setRotate(0.0D);
			}
			view.setFitWidth(74.0D);
			view.setFitHeight(103.0D);
			getChildren().add(view);
			setOnMouseEntered(event ->
			{
				if (revealed || card.getOwner() == Client.localPlayer)
					GameMenu.instance().setImage(card.getCard());
			});
			setOnMouseReleased(event ->
			{
				if (card.getOwner() != Client.localPlayer)
					return;
				if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
				{
					if ((Client.getRunningParty().getPlayer() != Client.localPlayer || !Client.getRunningParty().getCurrentPhase().isMain() ||
							Client.getRunningParty().getCurrentSpell() != null) && !card.getCard().getType().is(CardType.INSTANT) &&
							(card.getCard().getAbilityMap().get("en") == null || !card.getCard().getAbilityMap().get("en").contains("Flash")))
						return;
					PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
					pkt.setCard(card);
					pkt.action = PacketMixPlayCard.Action.PLAY;
					PacketManager.sendPacketToServer(pkt);
				}
				else if (event.getButton() == MouseButton.SECONDARY)
				{
					ContextMenu menu = new ContextMenu();

					MenuItem play = new MenuItem("Jouer");
					play.setOnAction(e ->
					{
						PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
						pkt.setCard(card);
						pkt.action = PacketMixPlayCard.Action.PLAY;
						PacketManager.sendPacketToServer(pkt);
					});
					if ((Client.getRunningParty().getPlayer() != Client.localPlayer || !Client.getRunningParty().getCurrentPhase().isMain() ||
							Client.getRunningParty().getCurrentSpell() != null) && !card.getCard().getType().is(CardType.INSTANT) &&
							(card.getCard().getAbilityMap().get("en") == null || !card.getCard().getAbilityMap().get("en").contains("Flash")))
						play.setDisable(true);
					menu.getItems().add(play);

					MenuItem playHided = new MenuItem("Jouer retournée");
					playHided.setOnAction(e ->
					{
						PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
						pkt.setCard(card);
						pkt.action = PacketMixPlayCard.Action.PLAY;
						pkt.hidden = true;
						PacketManager.sendPacketToServer(pkt);
					});
					if ((Client.getRunningParty().getPlayer() != Client.localPlayer || !Client.getRunningParty().getCurrentPhase().isMain() ||
							Client.getRunningParty().getCurrentSpell() != null) && !card.getCard().getType().is(CardType.INSTANT) &&
							(card.getCard().getAbilityMap().get("en") == null || !card.getCard().getAbilityMap().get("en").contains("Flash")))
						playHided.setDisable(true);
					if ((card.getCard().getAbilityMap().get("en") != null &&
							(card.getCard().getAbilityMap().get("en").contains("Morph") || card.getCard().getAbilityMap().get("en").contains("Manifest"))) ||
							card.getCard().getLayout() == Layout.DOUBLE_FACED || card.getCard().getLayout() == Layout.MELD || card.getCard().getLayout() == Layout.SPLIT || card.getCard()
							.getLayout() == Layout.FLIP)
						menu.getItems().add(playHided);
					MenuItem discard = new MenuItem("Défausser");
					discard.setOnAction(e ->
					{
						PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
						pkt.setCard(card);
						pkt.action = PacketMixPlayCard.Action.DISCARD;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(discard);
					MenuItem exile = new MenuItem("Exiler");
					exile.setOnAction(e ->
					{
						PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
						pkt.setCard(card);
						pkt.action = PacketMixPlayCard.Action.EXILE;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(exile);
					MenuItem reveal = new MenuItem("Révéler");
					reveal.setOnAction(e ->
					{
						PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
						pkt.setCard(card);
						pkt.action = PacketMixPlayCard.Action.REVEAL;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(reveal);
					MenuItem upLib = new MenuItem("Placer sur la biblithèque");
					upLib.setOnAction(e ->
					{
						PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
						pkt.setCard(card);
						pkt.action = PacketMixPlayCard.Action.UP_LIBRARY;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(upLib);
					MenuItem downLib = new MenuItem("Placer sous la biblithèque");
					downLib.setOnAction(e ->
					{
						PacketMixPlayCard pkt = PacketManager.createPacket(PacketMixPlayCard.class);
						pkt.setCard(card);
						pkt.action = PacketMixPlayCard.Action.DOWN_LIBRARY;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(downLib);

					menu.show(CardShower.this, event.getScreenX(), event.getScreenY());
				}
			});
		}
		else
		{
			if (Utils.getSide() == Side.CLIENT)
				view.imageProperty().bind(Bindings.createObjectBinding(() -> CardImageManager.getImage(card), card.card));
			view.setFitWidth(74.0D);
			view.setFitHeight(103.0D);
			setMinSize(103.0D, 103.0D);
			setMaxSize(103.0D, 103.0D);
			setPrefSize(103.0D, 103.0D);
			setPickOnBounds(false);
			view.setSmooth(true);
			getChildren().add(view);
			card.getAssociatedCards().addListener((ListChangeListener<? super PlayedCard>) c -> Platform.runLater(() ->
			{
				ObservableList<PlayedCard> associatedCards = card.getAssociatedCards();
				getChildren().clear();
				for (int i = 0; i < associatedCards.size(); i++)
				{
					PlayedCard associated = associatedCards.get(i);
					CardShower shower = getShower(associated);
					if (associated.getAssociatedCard() == null)
					{
						associated.setAssociatedCard(card);
						HBox box = (HBox) shower.getParent();
						box.getChildren().remove(shower);
					}
					shower.setTranslateX(14 * i - 305);
					shower.setTranslateY(-14 * i);
					getChildren().add(shower);
				}
				getChildren().add(view);
				updatePower();
			}));
			setOnMouseEntered(event -> GameMenu.instance().setImage(card));
			setOnMouseReleased(event ->
			{
				if (!card.getType().isPermanent())
					return;
				int index = Client.localPlayer.runningParty.getData(card.getController()).getPlayed().indexOf(card);
				if (event.getButton() == MouseButton.PRIMARY && Client.localPlayer == card.getController() && event.getClickCount() == 2)
				{
					PacketMixTapCard pkt = PacketManager.createPacket(PacketMixTapCard.class);
					pkt.shouldTap = !card.isTapped();
					pkt.card = card;
					pkt.index = index;
					PacketManager.sendPacketToServer(pkt);
				}
				else if (event.getButton() == MouseButton.SECONDARY)
				{
					if (currentArrow != null)
					{
						CardShower dest = null;
						for (CardShower shower : showers)
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
						pkt.from = card;
						pkt.to = dest.card;
						PacketManager.sendPacketToServer(pkt);
						return;
					}
					if (Client.localPlayer != card.getController())
					{
						ContextMenu menu = new ContextMenu();
						MenuItem gainControl = new MenuItem("Acquérir le contrôle");
						gainControl.setOnAction(e ->
						{
							PacketMixGainControl pkt = PacketManager.createPacket(PacketMixGainControl.class);
							pkt.index = index;
							pkt.oldController = card.getController();
							pkt.newController = Client.localPlayer;
							PacketManager.sendPacketToServer(pkt);
						});
						menu.getItems().add(gainControl);
						menu.show(this, event.getScreenX(), event.getScreenY());
						return;
					}
					ContextMenu menu = new ContextMenu();
					MenuItem tap = new MenuItem("Engager");
					tap.setOnAction(e ->
					{
						PacketMixTapCard pkt = PacketManager.createPacket(PacketMixTapCard.class);
						pkt.shouldTap = true;
						pkt.card = card;
						pkt.index = index;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(tap);
					MenuItem untap = new MenuItem("Dégager");
					untap.setOnAction(e ->
					{
						PacketMixTapCard pkt = PacketManager.createPacket(PacketMixTapCard.class);
						pkt.shouldTap = false;
						pkt.card = card;
						pkt.index = index;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(untap);
					if (card.isCard() &&
							((card.getCard().getAbilityMap().get("en") != null && card.getCard().getAbilityMap().get("en").contains("Morph") && card.getRelatedCard() == null) ||
									card.getCard().getLayout() == Layout.DOUBLE_FACED || card.getCard().getLayout() == Layout.MELD || card.getCard().getLayout() == Layout.SPLIT || card
									.getCard().getLayout() == Layout.FLIP))
					{
						menu.getItems().add(new SeparatorMenuItem());
						MenuItem ret = new MenuItem("Retourner");
						ret.setOnAction(e ->
						{
							PacketMixReturnCard pkt = PacketManager.createPacket(PacketMixReturnCard.class);
							pkt.index = (short) Client.getRunningParty().getData(card.getController()).getPlayed().indexOf(card);
							pkt.p = card.getController();
							PacketManager.sendPacketToServer(pkt);
						});
						menu.getItems().add(ret);
					}
					menu.getItems().add(new SeparatorMenuItem());
					Menu addMarker = new Menu("Ajouter un marqueur ...");
					for (MarkerType type : MarkerType.values())
					{
						if (!type.isApplicable(card.getType()))
							continue;
						MenuItem item = new MenuItem(type.getTranslatedName());
						item.setOnAction(e ->
						{
							PacketMixAddMarker pkt = PacketManager.createPacket(PacketMixAddMarker.class);
							pkt.card = card;
							pkt.type = type;
							PacketManager.sendPacketToServer(pkt);
						});
						addMarker.getItems().add(item);
					}
					if (addMarker.getItems().size() > 0)
						menu.getItems().add(addMarker);
					Menu removeMarker = new Menu("Retirer un marqueur ...");
					for (Marker marker : card.getMarkers())
					{
						MenuItem item = new MenuItem(marker.getType().getTranslatedName());
						item.setOnAction(e ->
						{
							PacketMixRemoveMarker pkt = PacketManager.createPacket(PacketMixRemoveMarker.class);
							pkt.card = card;
							pkt.index = (short) card.getMarkers().indexOf(marker);
							PacketManager.sendPacketToServer(pkt);
						});
						removeMarker.getItems().add(item);
					}
					if (removeMarker.getItems().size() > 0)
						menu.getItems().add(removeMarker);
					if (card.getType().is(CardType.CREATURE))
					{
						MenuItem setLife = new MenuItem("Définir les stats");
						setLife.setOnAction(e ->
						{
							for (Marker m : card.getMarkers())
								m.onCardUnmarked(card);
							Dialog<ButtonType> dialog = new Dialog<>();
							GridPane pane = new GridPane();
							pane.getChildren().add(new Label("Force :"));
							Label toughnessLbl = new Label("Endurance :");
							pane.getChildren().add(toughnessLbl);
							GridPane.setRowIndex(toughnessLbl, 1);
							Spinner<Integer> power = new Spinner<>(0, 0x7FFFFFFF, card.getPower());
							pane.getChildren().add(power);
							GridPane.setColumnIndex(power, 1);
							Spinner<Integer> toughness = new Spinner<>(0, 0x7FFFFFFF, card.getToughness());
							pane.getChildren().add(toughness);
							GridPane.setRowIndex(toughness, 1);
							GridPane.setColumnIndex(toughness, 1);
							dialog.getDialogPane().setContent(pane);
							dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
							dialog.showAndWait().ifPresent(e1 ->
							{
								if (e1 == ButtonType.APPLY)
								{
									PacketMixSetLife pkt = PacketManager.createPacket(PacketMixSetLife.class);
									pkt.card = card;
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
					setType.setOnAction(e ->
					{
						ChoiceDialog<CardType> dialog = new ChoiceDialog<>();
						dialog.getItems().addAll(CardType.values());
						dialog.setSelectedItem(card.getType());
						dialog.showAndWait().ifPresent(cardType ->
						{
							PacketMixSetCardType pkt = PacketManager.createPacket(PacketMixSetCardType.class);
							pkt.card = card;
							pkt.newType = cardType;
							PacketManager.sendPacketToServer(pkt);
						});
					});
					menu.getItems().add(setType);
					MenuItem subtypes = new MenuItem("Définir les sous-types ...");
					subtypes.setOnAction(e ->
					{
						Dialog<ButtonType> dialog = new Dialog<>();
						GridPane pane = new GridPane();
						dialog.getDialogPane().setContent(pane);
						dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
						int x = 0, y = 0;
						Map<SubType, CheckBox> map = Maps.newHashMap();
						for (SubType type : SubType.values())
						{
							CheckBox box = new CheckBox(type.toString());
							box.setSelected(card.getSubtypes().contains(type));
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
						dialog.showAndWait().ifPresent(buttonType ->
						{
							if (buttonType == ButtonType.APPLY)
							{
								PacketMixSetCardSubtypes pkt = PacketManager.createPacket(PacketMixSetCardSubtypes.class);
								pkt.card = card;
								pkt.newSubtypes = Sets.newHashSet();
								//noinspection unchecked
								pkt.newSubtypes
										.addAll(map.entrySet().stream().filter(entry -> entry.getValue().isSelected()).map(Map.Entry::getKey).collect(Collectors.toList
												()));
								PacketManager.sendPacketToServer(pkt);
							}
						});
					});
					menu.getItems().add(subtypes);
					menu.getItems().add(new SeparatorMenuItem());

					List<Token> canCreate = Arrays.stream(Token.values()).filter(token -> card.isCard() && token.getRelated().contains(card.getCard())).collect(Collectors.toList());

					Menu invokeToken = new Menu("Invoquer un jeton ...");
					for (Token token : canCreate)
					{
						MenuItem item = new MenuItem(token.toString());
						item.setOnAction(e ->
						{
							PacketMixInvokeToken pkt = PacketManager.createPacket(PacketMixInvokeToken.class);
							pkt.token = token;
							pkt.p = card.getController();
							PacketManager.sendPacketToServer(pkt);
						});
						invokeToken.getItems().add(item);
					}
					MenuItem other = new MenuItem("Invoquer un autre jeton ...");
					other.setOnAction(e ->
					{
						ChoiceDialog<Token> dialog = new ChoiceDialog<>();
						dialog.getItems().addAll(Token.values());
						dialog.setSelectedItem(dialog.getItems().get(0));
						dialog.showAndWait().ifPresent(token ->
						{
							PacketMixInvokeToken pkt = PacketManager.createPacket(PacketMixInvokeToken.class);
							pkt.token = token;
							pkt.p = card.getController();
							PacketManager.sendPacketToServer(pkt);
						});
					});
					invokeToken.getItems().add(other);

					menu.getItems().add(invokeToken);
					menu.getItems().add(new SeparatorMenuItem());
					MenuItem graveyard = new MenuItem("Envoyer au cimetière");
					graveyard.setOnAction(e ->
					{
						PacketMixDestroyCard pkt = PacketManager.createPacket(PacketMixDestroyCard.class);
						pkt.dest = PacketMixDestroyCard.Destination.GRAVEYARD;
						pkt.card = card;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(graveyard);
					MenuItem exile = new MenuItem("Exiler");
					exile.setOnAction(e ->
					{
						PacketMixDestroyCard pkt = PacketManager.createPacket(PacketMixDestroyCard.class);
						pkt.dest = PacketMixDestroyCard.Destination.EXILE;
						pkt.card = card;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(exile);
					MenuItem hand = new MenuItem("Renvoyer dans la main");
					hand.setOnAction(e ->
					{
						PacketMixDestroyCard pkt = PacketManager.createPacket(PacketMixDestroyCard.class);
						pkt.dest = PacketMixDestroyCard.Destination.HAND;
						pkt.card = card;
						PacketManager.sendPacketToServer(pkt);
					});
					menu.getItems().add(hand);
					if (Client.getRunningParty().getCurrentSpell() != null)
					{
						for (MenuItem item : menu.getItems())
							item.setDisable(true);
					}
					menu.show(CardShower.this, event.getScreenX(), event.getScreenY());
				}
			});
			setOnMouseDragged(event ->
			{
				if (event.getButton() == MouseButton.PRIMARY)
				{
					if (card.getController() != Client.localPlayer)
						return;

					CardShower dest = null;
					for (CardShower shower : showers)
					{
						if (shower != this && shower.localToScene(shower.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY()))
						{
							dest = shower;
							break;
						}
					}
					if (dest != null && dest != this && dest.card.getAssociatedCard() == null)
					{
				/*	double diff = event.getSceneX() - dest.localToScene(dest.getBoundsInLocal()).getMinX();
					if (card.getAssociatedCard() != null)
					{
						PacketMixAttachCard pkt = PacketManager.createPacket(PacketMixAttachCard.class);
						pkt.action = PacketMixAttachCard.Action.DETACH;
						pkt.attacher = card;
						pkt.attached = card.getAssociatedCard();
						PacketManager.sendPacketToServer(pkt);
					}*/

				/*	if (diff > 20.0D && diff < 54.0D)
					{
						PacketMixAttachCard pkt = PacketManager.createPacket(PacketMixAttachCard.class);
						pkt.action = PacketMixAttachCard.Action.ATTACH;
						pkt.attacher = dest.card;
						pkt.attached = card;
						PacketManager.sendPacketToServer(pkt);
					}
					else*/
						{
							PacketMixMoveCard pkt = PacketManager.createPacket(PacketMixMoveCard.class);
							pkt.dataPos = (short) card.getController().getData().getPlayed().indexOf(card);
							if (dest.card.getAssociatedCard() == null)
								pkt.newPos = (short) ((HBox) dest.getParent()).getChildren().indexOf(dest);
							else
								pkt.newPos = (short) ((HBox) dest.getParent().getParent()).getChildren().indexOf(dest);
							pkt.parentId = getParent().getId();
							pkt.p = card.getController();
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
			showers.add(this);
			if (Utils.getSide() == Side.CLIENT)
			{
				updatePower();
				updateTap();
			}
		}
	}

	public static CardShower getShower(PlayedCard card)
	{
		Optional<CardShower> opt = showers.stream().filter(s -> s.card == card).findAny();
		if (opt.isPresent())
			return opt.get();
		CardShower shower = new CardShower(card);
		showers.add(shower);
		return shower;
	}

	public void updatePower()
	{
		Platform.runLater(() ->
		{
			if (powerText != null)
				getChildren().remove(powerText);
			powerText = new Text();
			if (card.getType().is(CardType.CREATURE))
				powerText.setText(card.getPower() + "/" + card.getToughness());
			else if (card.getType().is(CardType.PLANESWALKER))
				powerText.setText(Integer.toString(card.getLoyalty()));
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

	public void updateTap()
	{
		Platform.runLater(() ->
		{
			double base = 0.0D;
			if (card.getController() != Client.localPlayer)
				base = 180.0D;
			if (card.isTapped())
				setRotate(90.0D + base);
			else
				setRotate(base);
		});
	}

	public void reveal()
	{
		assert card.isHand();
		if (revealed)
			return;
		revealed = true;
		view.setImage(CardImageManager.getImage(card.getCard()));
		Executors.newSingleThreadExecutor().submit(() ->
		{
			try
			{
				Thread.sleep(5000L);
				revealed = false;
				view.setImage(CardImageManager.getImage(card.getOwner() == Client.localPlayer ? card.getCard() : null));
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		});
	}

	public void destroy()
	{
		showers.remove(this);
	}
}
