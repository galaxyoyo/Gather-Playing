package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.client.gui.PlayerInfos;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

public class PacketMixPlayCard extends Packet
{
	public Player controller;
	public short index;
	public Action action = Action.PLAY;
	public boolean hidden = false;

	@Override
	public void read(ByteBuf buf)
	{
		action = Action.values()[buf.readByte()];
		controller = player.runningParty.getPlayer(readUUID(buf));
		PlayerData data = player.runningParty.getData(controller);
		index = buf.readShort();
		PlayedCard card = data.getHand().get(index);
		System.err.println("Card: " + card.getTranslatedName().get());
		hidden = buf.readBoolean();
		int handIndex = data.getHand().indexOf(card);
		if (action != Action.REVEAL)
		{
			data.getHand().remove(handIndex);
			data.setMulligan((byte) 0xFF);
			if (hidden)
			{
				if (card.getCard().getLayout() == Layout.NORMAL)
					card.hide(true);
				else
				{
					Card oldCard = card.getCard();
					card.card.set(card.getRelatedCard());
					card.setRelatedCard(oldCard);
				}
			}
			if (action == Action.DISCARD)
				data.getGraveyard().add(card);
			else if (action == Action.EXILE)
				data.getExile().add(card);
			else if (action != Action.PLAY)
			{
				if (Utils.getSide() == Side.CLIENT)
					PlayerInfos.getInfos(player).addLibrary();
				else
				{
					Library lib = data.getLibrary();
					if (action == Action.UP_LIBRARY)
						lib.addCardUp(card.toOwnedCard());
					else
						lib.addCard(card.toOwnedCard());
				}
			}
		}
		card.setHand(false);
		if (Utils.getSide() == Side.CLIENT)
			Platform.runLater(() -> CardShower.getShower(card).reload());
		if (action == Action.PLAY)
		{
			if (Utils.getSide() == Side.SERVER)
				Server.sendChat(player.runningParty, "chat.play", "color: blue;", card.getController().name, "<i>" + card.getTranslatedName().get() + "</i>");
			if (card.getType().is(CardType.LAND))
			{
				data.getPlayed().add(card);
				if (Utils.getSide() == Side.CLIENT)
				{
					CardShower shower = CardShower.getShower(card);
					final PlayedCard finalPlayed = card;
					Platform.runLater(() -> {
						shower.reload();
						if (finalPlayed.getOwner() == Client.localPlayer)
							GameMenu.instance().lands.getChildren().add(shower);
						else
							GameMenu.instance().adverseLands.getChildren().add(shower);
						HBox.setMargin(shower, new Insets(0.0D, 10.5D, 0.0D, 10.5D));
					});
				}
			}
			else
			{
				if (player.runningParty.getCurrentSpell() == null)
					player.runningParty.setCurrentSpell(new SpellTimer(card, player.runningParty));
				else
					player.runningParty.getCurrentSpell().addSpell(card);
			}
		}
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
		else
		{
			if (card.getOwner() == Client.localPlayer)
				Platform.runLater(() -> GameMenu.instance().hand.getChildren().remove(CardShower.getShower(card)));
			else
				Platform.runLater(() -> GameMenu.instance().adverseHand.getChildren().remove(CardShower.getShower(card)));
			if (action == Action.DISCARD)
				PlayerInfos.getInfos(card.getOwner()).graveyard(card);
			else if (action == Action.EXILE)
				PlayerInfos.getInfos(card.getOwner()).exile(card);
			else if (action == Action.REVEAL)
			{
				CardShower shower = (CardShower) (card.getOwner() == player ? GameMenu.instance().hand : GameMenu.instance().adverseHand).getChildren().get(handIndex);
				shower.reveal();
			}
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(action.ordinal());
		writeUUID(controller.uuid, buf);
		buf.writeShort(index);
		buf.writeBoolean(hidden);
	}

	public void setCard(PlayedCard card)
	{
		controller = card.getController();
		index = (short) card.getController().getData().getHand().indexOf(card);
	}

	public enum Action
	{
		PLAY, DISCARD, EXILE, REVEAL, UP_LIBRARY, DOWN_LIBRARY
	}
}