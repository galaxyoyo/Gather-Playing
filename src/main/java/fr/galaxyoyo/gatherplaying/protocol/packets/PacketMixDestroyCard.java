package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;

public class PacketMixDestroyCard extends Packet
{
	public Destination dest = Destination.GRAVEYARD;
	public PlayedCard card;
	public int index = -1;

	@Override
	public void read(ByteBuf buf)
	{
		dest = Destination.values()[buf.readByte()];
		Player controller = player.runningParty.getPlayer(readUUID(buf));
		PlayerData data = player.runningParty.getData(controller);
		int index = buf.readInt();
		card = data.getPlayed().get(index);
		Player owner = card.owner;
		if (card.isToken())
		{
			if (Utils.getSide() == Side.CLIENT)
			{
				if (card.controller == player)
					Platform.runLater(() -> GameMenu.INSTANCE.creatures.getChildren().remove(CardShower.getShower(card)));
				else
					Platform.runLater(() -> GameMenu.INSTANCE.adverseCreatures.getChildren().remove(CardShower.getShower(card)));
			}
			else
				sendToParty();
			data.getPlayed().remove(card);
			return;
		}
		OwnedCard c = new OwnedCard(card.getCard(), card.owner, card.foiled);
		if (Utils.getSide() == Side.CLIENT)
		{
			if (owner == player)
			{
				if (dest == Destination.EXILE)
					GameMenu.INSTANCE.playerInfos.exile(card);
				else if (dest == Destination.GRAVEYARD)
					GameMenu.INSTANCE.playerInfos.graveyard(card);
				else if (dest == Destination.HAND)
					Platform.runLater(() -> GameMenu.INSTANCE.hand.getChildren().add(new CardShower(c)));
				else
					GameMenu.INSTANCE.playerInfos.setLibrary(GameMenu.INSTANCE.playerInfos.getLibrary() + 1);
			}
			else
			{
				if (dest == Destination.EXILE)
					GameMenu.INSTANCE.adverseInfos.exile(card);
				else if (dest == Destination.GRAVEYARD)
					GameMenu.INSTANCE.adverseInfos.graveyard(card);
				else if (dest == Destination.HAND)
					Platform.runLater(() -> GameMenu.INSTANCE.adverseHand.getChildren().add(new CardShower(c)));
				else
					GameMenu.INSTANCE.adverseInfos.addLibrary();
			}
			CardShower shower = CardShower.getShower(card);
			shower.destroy();
			if (card.type.is(CardType.LAND))
			{
				if (card.owner == Client.localPlayer)
					Platform.runLater(() -> GameMenu.INSTANCE.lands.getChildren().remove(shower));
				else
					Platform.runLater(() -> GameMenu.INSTANCE.adverseLands.getChildren().remove(shower));
			}
			else if ((card.type.is(CardType.ENCHANTMENT) && !card.subtypes.contains(SubType.valueOf("Aura"))) || card.type.is(CardType.ARTIFACT) ||
					 card.type.is(CardType.PLANESWALKER))
			{
				if (card.owner == Client.localPlayer)
					Platform.runLater(() -> GameMenu.INSTANCE.enchants.getChildren().remove(shower));
				else
					Platform.runLater(() -> GameMenu.INSTANCE.adverseEnchants.getChildren().remove(shower));
			}
			else
			{
				if (card.owner == Client.localPlayer)
					Platform.runLater(() -> GameMenu.INSTANCE.creatures.getChildren().remove(shower));
				else
					Platform.runLater(() -> GameMenu.INSTANCE.adverseCreatures.getChildren().remove(shower));
			}
		}
		else
		{
			sendToParty();
			if (dest == Destination.GRAVEYARD)
				Server.sendChat(player.runningParty, "chat.wasdestroyed", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>");
			else if (dest == Destination.EXILE)
				Server.sendChat(player.runningParty, "chat.wasexiled", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>");
			else if (dest == Destination.HAND)
				Server.sendChat(player.runningParty, "chat.tohand", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>", c
						.getOwner().name);
			else if (dest == Destination.UP_LIBRARY)
				Server.sendChat(player.runningParty, "chat.touplibrary", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>", c.getOwner().name);
			else if (dest == Destination.DOWN_LIBRARY)
				Server.sendChat(player.runningParty, "chat.todownlibrary", null, "<i>" + c.getCard().getTranslatedName().get() + "</i>", c.getOwner().name);
		}
		data.getPlayed().remove(index);
		if (dest == Destination.EXILE)
			data.getExile().add(c);
		else if (dest == Destination.GRAVEYARD)
			data.getGraveyard().add(c);
		else if (dest == Destination.HAND)
			data.getHand().add(c);
		else if (Utils.getSide() == Side.SERVER)
		{
			Library lib = data.getLibrary();
			if (dest == Destination.DOWN_LIBRARY)
				lib.addCard(c);
			else
				lib.addCardUp(c);
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(dest.ordinal());
		writeUUID(card.controller.uuid, buf);
		buf.writeInt(index >= 0 ? index : player.runningParty.getData(card.controller).getPlayed().indexOf(card));
	}

	public enum Destination
	{
		GRAVEYARD, EXILE, HAND, UP_LIBRARY, DOWN_LIBRARY
	}
}