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
				if (card.getCard().getLayout() == Layout.NORMAL)
					played.setHided(true);
				else
				{
					Card oldCard = card.getCard();
					played.card.set(played.getRelatedCard());
					played.setRelatedCard(oldCard);
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
				Server.sendChat(player.runningParty, "chat.play", "color: blue;", played.getController().name, "<i>" + played.getTranslatedName().get() + "</i>");
			if (played.getType().is(CardType.LAND))
			{
				data.getPlayed().add(played);
				if (Utils.getSide() == Side.CLIENT)
				{
					CardShower shower = new CardShower(played);
					final PlayedCard finalPlayed = played;
					Platform.runLater(() -> {
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
					player.runningParty.setCurrentSpell(new SpellTimer(played, player.runningParty));
				else
					player.runningParty.getCurrentSpell().addSpell(played);
			}
		}
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
		else
		{
			if (action != Action.REVEAL)
			{
				if (card.getOwner() == player)
					Platform.runLater(() -> GameMenu.instance().hand.getChildren().remove(handIndex));
				else
					Platform.runLater(() -> GameMenu.instance().adverseHand.getChildren().remove(handIndex));
			}
			assert played != null;
			if (action == Action.DISCARD)
				PlayerInfos.getInfos(played.getOwner()).graveyard(played);
			else if (action == Action.EXILE)
				PlayerInfos.getInfos(played.getOwner()).exile(played);
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