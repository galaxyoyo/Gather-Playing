package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.OwnedCard;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixPlayDestroyed;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

public class DestroyedCardShower extends ImageView
{
	public DestroyedCardShower(OwnedCard card, boolean exiled)
	{
		super();
		Platform.runLater(() -> setImage(CardImageManager.getImage(card.getCard())));
		setFitWidth(223.0D);
		setFitHeight(310.0D);
		setOnMouseEntered(event -> GameMenu.INSTANCE.setImage(card.getCard()));
		setOnMouseReleased(event -> {
			if (event.getButton() == MouseButton.SECONDARY && card.getOwner() == Client.localPlayer)
			{
				ContextMenu menu = new ContextMenu();
				int index = getParent().getChildrenUnmodifiable().indexOf(this);

				MenuItem hand = new MenuItem("Renvoyer dans la main de son propriétaire");
				hand.setOnAction(e -> {
					PacketMixPlayDestroyed pkt = PacketManager.createPacket(PacketMixPlayDestroyed.class);
					pkt.exiled = exiled;
					pkt.dest = PacketMixPlayDestroyed.Destination.HAND;
					pkt.index = index;
					pkt.p = Client.localPlayer;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(DestroyedCardShower.this);
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
					PacketMixPlayDestroyed pkt = PacketManager.createPacket(PacketMixPlayDestroyed.class);
					pkt.exiled = exiled;
					pkt.dest = PacketMixPlayDestroyed.Destination.GRAVEYARD;
					pkt.index = index;
					pkt.p = Client.localPlayer;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(DestroyedCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				if (exiled)
					menu.getItems().add(graveyard);

				MenuItem exil = new MenuItem("Exiler");
				exil.setOnAction(e -> {
					PacketMixPlayDestroyed pkt = PacketManager.createPacket(PacketMixPlayDestroyed.class);
					pkt.exiled = exiled;
					pkt.dest = PacketMixPlayDestroyed.Destination.EXILE;
					pkt.index = index;
					pkt.p = Client.localPlayer;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(DestroyedCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				if (!exiled)
					menu.getItems().add(exil);

				MenuItem upLib = new MenuItem("Mettre au-dessus de la bibliothèque");
				upLib.setOnAction(e -> {
					PacketMixPlayDestroyed pkt = PacketManager.createPacket(PacketMixPlayDestroyed.class);
					pkt.exiled = exiled;
					pkt.dest = PacketMixPlayDestroyed.Destination.UP_LIBRARY;
					pkt.index = index;
					pkt.p = Client.localPlayer;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(DestroyedCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				menu.getItems().add(upLib);

				MenuItem downLib = new MenuItem("Mettre au-dessous de la bibliothèque");
				downLib.setOnAction(e -> {
					PacketMixPlayDestroyed pkt = PacketManager.createPacket(PacketMixPlayDestroyed.class);
					pkt.exiled = exiled;
					pkt.dest = PacketMixPlayDestroyed.Destination.DOWN_LIBRARY;
					pkt.index = index;
					pkt.p = Client.localPlayer;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(DestroyedCardShower.this);
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
					PacketMixPlayDestroyed pkt = PacketManager.createPacket(PacketMixPlayDestroyed.class);
					pkt.exiled = exiled;
					pkt.dest = PacketMixPlayDestroyed.Destination.BATTLEFIELD;
					pkt.index = index;
					pkt.p = Client.localPlayer;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(DestroyedCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				menu.getItems().add(bf);

				MenuItem obf = new MenuItem("Renvoyer sur le champ de bataille sous le contrôle de l'adversaire");
				obf.setOnAction(e -> {
					PacketMixPlayDestroyed pkt = PacketManager.createPacket(PacketMixPlayDestroyed.class);
					pkt.exiled = exiled;
					pkt.dest = PacketMixPlayDestroyed.Destination.OTHER_BATTLEFIELD;
					pkt.index = index;
					pkt.p = Client.localPlayer;
					PacketManager.sendPacketToServer(pkt);
					HBox box = (HBox) getParent();
					box.getChildren().remove(DestroyedCardShower.this);
					if (box.getChildren().isEmpty())
					{
						DialogPane pane = (DialogPane) box.getParent().getParent().getParent().getParent();
						ButtonType button = ButtonType.CANCEL;
						pane.getButtonTypes().add(button);
						((Button) pane.lookupButton(button)).fire();
					}
				});
				menu.getItems().add(obf);

				menu.show(DestroyedCardShower.this, event.getScreenX(), event.getScreenY());
			}
		});
	}
}
