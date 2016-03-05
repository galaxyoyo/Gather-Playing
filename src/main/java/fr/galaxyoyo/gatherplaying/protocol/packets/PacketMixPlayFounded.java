package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
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
		switch (dest)
		{
			case BATTLEFIELD:
				PlayedCard played = new PlayedCard(card);
				data.getPlayed().add(played);
				if (Utils.getSide() == Side.CLIENT)
				{
					if (played.type.is(CardType.LAND))
					{
						if (played.owner == Client.localPlayer)
							Platform.runLater(() -> GameMenu.INSTANCE.lands.getChildren().add(new CardShower(played)));
						else
							Platform.runLater(() -> GameMenu.INSTANCE.adverseLands.getChildren().add(new CardShower(played)));
					}
					else if ((played.type.is(CardType.ENCHANTMENT) && !played.subtypes.contains(SubType.valueOf("Aura"))) || played.type.is(CardType.ARTIFACT) ||
							 played.type.is(CardType.PLANESWALKER))
					{
						if (played.owner == Client.localPlayer)
							Platform.runLater(() -> GameMenu.INSTANCE.enchants.getChildren().add(new CardShower(played)));
						else
							Platform.runLater(() -> GameMenu.INSTANCE.adverseEnchants.getChildren().add(new CardShower(played)));
					}
					else
					{
						if (played.owner == Client.localPlayer)
							Platform.runLater(() -> GameMenu.INSTANCE.creatures.getChildren().add(new CardShower(played)));
						else
							Platform.runLater(() -> GameMenu.INSTANCE.adverseCreatures.getChildren().add(new CardShower(played)));
					}
				}
				break;
			case EXILE:
				data.getExile().add(card);
				if (Utils.getSide() == Side.CLIENT)
					if (card.getOwner() == player)
						GameMenu.INSTANCE.playerInfos.exile(new PlayedCard(card));
					else
						GameMenu.INSTANCE.adverseInfos.exile(new PlayedCard(card));
				break;
			case GRAVEYARD:
				data.getGraveyard().add(card);
				if (Utils.getSide() == Side.CLIENT)
					if (card.getOwner() == player)
						GameMenu.INSTANCE.playerInfos.graveyard(new PlayedCard(card));
					else
						GameMenu.INSTANCE.adverseInfos.graveyard(new PlayedCard(card));
				break;
			case HAND:
				data.getHand().add(card);
				if (Utils.getSide() == Side.CLIENT)
				{
					if (player == card.getOwner())
						Platform.runLater(() -> GameMenu.INSTANCE.hand.getChildren().add(new CardShower(card)));
					else
						Platform.runLater(() -> GameMenu.INSTANCE.adverseHand.getChildren().add(new CardShower(card)));
				}
				break;
			case UP_LIBRARY:
				if (Utils.getSide() == Side.CLIENT)
				{
					if (player == card.getOwner())
						GameMenu.INSTANCE.playerInfos.addLibrary();
					else
						GameMenu.INSTANCE.adverseInfos.addLibrary();
				}
				else
					data.getLibrary().addCardUp(card);
				break;
			case DOWN_LIBRARY:
				if (Utils.getSide() == Side.CLIENT)
				{
					if (player == card.getOwner())
						GameMenu.INSTANCE.playerInfos.addLibrary();
					else
						GameMenu.INSTANCE.adverseInfos.addLibrary();
				}
				else
					data.getLibrary().addCard(card);
				break;
		}
		if (Utils.getSide() == Side.SERVER)
		{
			data.getLibrary().getSortedCards().remove(card);
			sendToParty();
		}
		else
		{
			if (player == card.getOwner())
				GameMenu.INSTANCE.playerInfos.removeLibrary();
			else
				GameMenu.INSTANCE.adverseInfos.removeLibrary();
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