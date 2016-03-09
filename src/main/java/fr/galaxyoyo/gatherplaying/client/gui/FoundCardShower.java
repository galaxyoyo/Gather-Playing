package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.OwnedCard;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixPlayFounded;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

public class FoundCardShower extends ImageView
{
	public FoundCardShower(OwnedCard card)
	{
		super();
		if (Utils.getSide() == Side.CLIENT)
			Platform.runLater(() -> setImage(CardImageManager.getImage(card.getCard())));
		setFitWidth(223.0D);
		setFitHeight(310.0D);
		setOnMouseEntered(event -> GameMenu.instance().setImage(card.getCard()));
		setOnMouseReleased(event -> {
			if (event.getButton() == MouseButton.SECONDARY && card.getOwner() == Client.localPlayer)
			{
				ContextMenu menu = new ContextMenu();

				MenuItem hand = new MenuItem("Renvoyer dans la main de son propriétaire");
				hand.setOnAction(e -> {
					PacketMixPlayFounded pkt = PacketManager.createPacket(PacketMixPlayFounded.class);
					pkt.dest = PacketMixPlayFounded.Destination.HAND;
					pkt.card = card;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(FoundCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				menu.getItems().add(hand);

				MenuItem graveyard = new MenuItem("Envoyer au cimetière");
				graveyard.setOnAction(e -> {
					PacketMixPlayFounded pkt = PacketManager.createPacket(PacketMixPlayFounded.class);
					pkt.dest = PacketMixPlayFounded.Destination.GRAVEYARD;
					pkt.card = card;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(FoundCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				menu.getItems().add(graveyard);

				MenuItem exil = new MenuItem("Exiler");
				exil.setOnAction(e -> {
					PacketMixPlayFounded pkt = PacketManager.createPacket(PacketMixPlayFounded.class);
					pkt.dest = PacketMixPlayFounded.Destination.EXILE;
					pkt.card = card;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(FoundCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				menu.getItems().add(exil);

				MenuItem upLib = new MenuItem("Envoyer au-dessus de la bibliothèque");
				upLib.setOnAction(e -> {
					PacketMixPlayFounded pkt = PacketManager.createPacket(PacketMixPlayFounded.class);
					pkt.dest = PacketMixPlayFounded.Destination.UP_LIBRARY;
					pkt.card = card;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(FoundCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				menu.getItems().add(upLib);

				MenuItem downLib = new MenuItem("Envoyer au-dessous de la bibliothèque");
				downLib.setOnAction(e -> {
					PacketMixPlayFounded pkt = PacketManager.createPacket(PacketMixPlayFounded.class);
					pkt.dest = PacketMixPlayFounded.Destination.DOWN_LIBRARY;
					pkt.card = card;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(FoundCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				menu.getItems().add(downLib);

				MenuItem bf = new MenuItem("Renvoyer sur le champ de bataille");
				bf.setOnAction(e -> {
					PacketMixPlayFounded pkt = PacketManager.createPacket(PacketMixPlayFounded.class);
					pkt.dest = PacketMixPlayFounded.Destination.BATTLEFIELD;
					pkt.card = card;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(FoundCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				menu.getItems().add(bf);

				menu.show(FoundCardShower.this, event.getScreenX(), event.getScreenY());
			}
		});
	}
}
