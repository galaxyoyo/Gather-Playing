package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.client.gui.PlayerInfos;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.markers.MarkerType;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

public class PacketMixPlayCard extends Packet
{
	public OwnedCard card;
	public Action action = Action.PLAY;
	public boolean hided = false;

	@Override
	public void read(ByteBuf buf)
	{
		action = Action.values()[buf.readByte()];
		card = new OwnedCard(readCard(buf), player.runningParty.getPlayer(readUUID(buf)), buf.readBoolean());
		PlayerData data = player.runningParty.getData(card.getOwner());
		hided = buf.readBoolean();
		int handIndex = data.getHand().indexOf(card);
		PlayedCard played = null;
		if (action != Action.REVEAL)
		{
			data.getHand().remove(handIndex);
			played = new PlayedCard(card);
			data.setMulligan((byte) 0xFF);
			if (hided)
			{
				if (card.getCard().layout == Layout.NORMAL)
					played.hided = true;
				else
				{
					Card oldCard = card.getCard();
					played.card.set(played.relatedCard);
					played.relatedCard = oldCard;
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
						lib.addCardUp(card);
					else
						lib.addCard(card);
				}
			}
		}
		if (action == Action.PLAY)
		{
			if (Utils.getSide() == Side.SERVER)
				Server.sendChat(player.runningParty, "chat.play", "color: blue;", played.controller.name, "<i>" + played.getTranslatedName().get() + "</i>");
			CardShower shower = new CardShower(played);
			if (played.getCard().type.is(CardType.CREATURE))
			{
				try
				{
					played.power.set(Integer.parseInt(played.getCard().power));
				}
				catch (NumberFormatException ex)
				{
					played.power.set(0);
				}
				try
				{
					played.toughness.set(Integer.parseInt(played.getCard().toughness));
				}
				catch (NumberFormatException ex)
				{
					played.toughness.set(0);
				}
			}
			else if (played.getCard().type.is(CardType.PLANESWALKER))
			{
				played.loyalty.set(0);
				for (int i = 0; i < played.getCard().loyalty; ++i)
				{
					Marker m = MarkerType.LOYALTY.newInstance();
					m.onCardMarked(played);
					played.markers.add(m);
				}
			}
			if (played.type.is(CardType.LAND))
			{
				data.getPlayed().add(played);
				if (Utils.getSide() == Side.CLIENT)
				{
					final PlayedCard finalPlayed = played;
					Platform.runLater(() -> {
						if (finalPlayed.owner == Client.localPlayer)
							GameMenu.INSTANCE.lands.getChildren().add(shower);
						else
							GameMenu.INSTANCE.adverseLands.getChildren().add(shower);
						HBox.setMargin(shower, new Insets(0.0D, 10.5D, 0.0D, 10.5D));
					});
				}
			}
			else
			{
				if (player.runningParty.currentSpell == null)
					player.runningParty.currentSpell = new SpellTimer(shower, player.runningParty);
				else
					player.runningParty.currentSpell.addSpell(shower);
			}
		}
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
		else
		{
			if (action != Action.REVEAL)
			{
				if (card.getOwner() == player)
					Platform.runLater(() -> GameMenu.INSTANCE.hand.getChildren().remove(handIndex));
				else
					Platform.runLater(() -> GameMenu.INSTANCE.adverseHand.getChildren().remove(handIndex));
			}
			assert played != null;
			if (action == Action.DISCARD)
				PlayerInfos.getInfos(played.owner).graveyard(played);
			else if (action == Action.EXILE)
				PlayerInfos.getInfos(played.owner).exile(played);
			else if (action == Action.REVEAL)
			{
				CardShower shower = (CardShower) (card.getOwner() == player ? GameMenu.INSTANCE.hand : GameMenu.INSTANCE.adverseHand).getChildren().get(handIndex);
				shower.reveal();
			}
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(action.ordinal());
		writeCard(card.getCard(), buf);
		writeUUID(card.getOwner().uuid, buf);
		buf.writeBoolean(card.isFoiled());
		buf.writeBoolean(hided);
	}

	public enum Action
	{
		PLAY, DISCARD, EXILE, REVEAL, UP_LIBRARY, DOWN_LIBRARY
	}
}