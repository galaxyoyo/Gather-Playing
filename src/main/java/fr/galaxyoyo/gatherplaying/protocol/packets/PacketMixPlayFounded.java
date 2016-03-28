package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;

public class PacketMixPlayFounded extends Packet
{
	public Destination dest;
	public OwnedCard card;

	@Override
	public void read(ByteBuf buf)
	{
		dest = Destination.values()[buf.readByte()];
		card = new OwnedCard(readCard(buf), player.runningParty.getPlayer(readUUID(buf)), buf.readBoolean());
		PlayerData data = player.runningParty.getData(card.getOwner());
		if (Utils.getSide() == Side.SERVER)
		{
			data.getLibrary().getSortedCards().remove(card);
			sendToParty();
		}
		else if (dest != Destination.UP_LIBRARY && dest != Destination.DOWN_LIBRARY)
		{
			if (player == card.getOwner())
				GameMenu.instance().playerInfos.removeLibrary();
			else
				GameMenu.instance().adverseInfos.removeLibrary();
		}
		switch (dest)
		{
			case BATTLEFIELD:
				PlayedCard played = new PlayedCard(card);
				data.getPlayed().add(played);
				if (Utils.getSide() == Side.CLIENT)
				{
					if (played.getType().is(CardType.LAND))
					{
						if (played.getOwner() == Client.localPlayer)
							Platform.runLater(() -> GameMenu.instance().lands.getChildren().add(new CardShower(played)));
						else
							Platform.runLater(() -> GameMenu.instance().adverseLands.getChildren().add(new CardShower(played)));
					} else if ((played.getType().is(CardType.ENCHANTMENT) && !played.getSubtypes().contains(SubType.valueOf("Aura"))) || played.getType().is(CardType.ARTIFACT) ||
							played.getType().is(CardType.PLANESWALKER))
					{
						if (played.getOwner() == Client.localPlayer)
							Platform.runLater(() -> GameMenu.instance().enchants.getChildren().add(new CardShower(played)));
						else
							Platform.runLater(() -> GameMenu.instance().adverseEnchants.getChildren().add(new CardShower(played)));
					} else
					{
						if (played.getOwner() == Client.localPlayer)
							Platform.runLater(() -> GameMenu.instance().creatures.getChildren().add(new CardShower(played)));
						else
							Platform.runLater(() -> GameMenu.instance().adverseCreatures.getChildren().add(new CardShower(played)));
					}
				}
				break;
			case EXILE:
				data.getExile().add(card);
				if (Utils.getSide() == Side.CLIENT)
					if (card.getOwner() == player)
						GameMenu.instance().playerInfos.exile(new PlayedCard(card));
					else
						GameMenu.instance().adverseInfos.exile(new PlayedCard(card));
				break;
			case GRAVEYARD:
				data.getGraveyard().add(card);
				if (Utils.getSide() == Side.CLIENT)
					if (card.getOwner() == player)
						GameMenu.instance().playerInfos.graveyard(new PlayedCard(card));
					else
						GameMenu.instance().adverseInfos.graveyard(new PlayedCard(card));
				break;
			case HAND:
				data.getHand().add(card);
				if (Utils.getSide() == Side.CLIENT)
				{
					if (player == card.getOwner())
						Platform.runLater(() -> GameMenu.instance().hand.getChildren().add(new CardShower(card)));
					else
						Platform.runLater(() -> GameMenu.instance().adverseHand.getChildren().add(new CardShower(card)));
				}
				break;
			case UP_LIBRARY:
				if (Utils.getSide() == Side.SERVER)
					data.getLibrary().addCardUp(card);
				break;
			case DOWN_LIBRARY:
				if (Utils.getSide() == Side.SERVER)
					data.getLibrary().addCard(card);
				break;
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(dest.ordinal());
		writeCard(card.getCard(), buf);
		writeUUID(card.getOwner().uuid, buf);
		buf.writeBoolean(card.isFoiled());
	}

	public enum Destination
	{
		HAND, GRAVEYARD, EXILE, BATTLEFIELD, UP_LIBRARY, DOWN_LIBRARY
	}
}