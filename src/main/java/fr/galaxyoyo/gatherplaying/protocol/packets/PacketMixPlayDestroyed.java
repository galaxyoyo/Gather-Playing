package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import io.netty.buffer.ByteBuf;
import java8.util.stream.StreamSupport;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

import java.util.List;

public class PacketMixPlayDestroyed extends Packet
{
	public boolean exiled;
	public int index;
	public Destination dest;
	public Player p;

	@Override
	public void read(ByteBuf buf)
	{
		p = player.runningParty.getPlayer(readUUID(buf));
		PlayerData data = player.runningParty.getData(p);
		exiled = buf.readBoolean();
		dest = Destination.values()[buf.readByte()];
		index = buf.readInt();
		PlayedCard c;
		if (exiled)
			c = data.getExile().remove(index);
		else
			c = data.getGraveyard().remove(index);
		if (dest == Destination.HAND)
		{
			data.getHand().add(c);
			if (Utils.getSide() == Side.CLIENT)
				if (p == player)
					Platform.runLater(() -> GameMenu.instance().hand.getChildren().add(new CardShower(c)));
				else
					Platform.runLater(() -> GameMenu.instance().adverseHand.getChildren().add(new CardShower(c)));
		} else if (dest == Destination.GRAVEYARD)
		{
			data.getGraveyard().add(c);
			if (Utils.getSide() == Side.CLIENT)
				if (p == player)
					GameMenu.instance().playerInfos.graveyard(c.duplicate());
				else
					GameMenu.instance().adverseInfos.graveyard(c.duplicate());
		} else if (dest == Destination.EXILE)
		{
			data.getExile().add(c);
			if (Utils.getSide() == Side.CLIENT)
				if (p == player)
					GameMenu.instance().playerInfos.exile(c.duplicate());
				else
					GameMenu.instance().adverseInfos.exile(c.duplicate());
		} else if (dest == Destination.UP_LIBRARY)
		{
			if (Utils.getSide() == Side.SERVER)
				data.getLibrary().addCardUp(c.toOwnedCard());
			else if (p == player)
				GameMenu.instance().playerInfos.addLibrary();
			else
				GameMenu.instance().adverseInfos.addLibrary();
		} else if (dest == Destination.DOWN_LIBRARY)
		{
			if (Utils.getSide() == Side.SERVER)
				data.getLibrary().addCard(c.toOwnedCard());
			else if (p == player)
				GameMenu.instance().playerInfos.addLibrary();
			else
				GameMenu.instance().adverseInfos.addLibrary();
		} else if (dest == Destination.BATTLEFIELD)
		{
			PlayedCard card = c.duplicate();
			data.getPlayed().add(card);
			if (Utils.getSide() == Side.CLIENT)
			{
				Platform.runLater(() -> {
					CardShower shower = new CardShower(card);
					if (card.getType().is(CardType.LAND))
					{
						if (card.getOwner() == Client.localPlayer)
							GameMenu.instance().lands.getChildren().add(shower);
						else
							GameMenu.instance().adverseLands.getChildren().add(shower);
					} else if ((card.getType().is(CardType.ENCHANTMENT) && !card.getSubtypes().contains(SubType.valueOf("Aura"))) || card.getType().is(CardType.ARTIFACT) ||
							card.getType().is(CardType.PLANESWALKER))
					{
						if (card.getOwner() == Client.localPlayer)
							GameMenu.instance().enchants.getChildren().add(shower);
						else
							GameMenu.instance().adverseEnchants.getChildren().add(shower);
					} else
					{
						if (card.getOwner() == Client.localPlayer)
							GameMenu.instance().creatures.getChildren().add(shower);
						else
							GameMenu.instance().adverseCreatures.getChildren().add(shower);
					}
					HBox.setMargin(shower, new Insets(0.0D, 10.5D, 0.0D, 10.5D));
				});
			}
		} else if (dest == Destination.OTHER_BATTLEFIELD)
		{
			PlayedCard card = c.duplicate();
			java.util.Set<Player> onlinePlayers = player.runningParty.getOnlinePlayers();
			card.setController(StreamSupport.stream(onlinePlayers).filter(pl -> !pl.uuid.equals(p.uuid)).findAny().get());
			data.getPlayed().add(card);
			if (card.getType().is(CardType.LAND))
			{
				if (card.getOwner() == Client.localPlayer)
					Platform.runLater(() -> GameMenu.instance().lands.getChildren().add(new CardShower(card)));
				else
					Platform.runLater(() -> GameMenu.instance().adverseLands.getChildren().add(new CardShower(card)));
			} else if ((card.getType().is(CardType.ENCHANTMENT) && !card.getSubtypes().contains(SubType.valueOf("Aura"))) || card.getType().is(CardType.ARTIFACT) ||
					card.getType().is(CardType.PLANESWALKER))
			{
				if (card.getOwner() == Client.localPlayer)
					Platform.runLater(() -> GameMenu.instance().enchants.getChildren().add(new CardShower(card)));
				else
					Platform.runLater(() -> GameMenu.instance().adverseEnchants.getChildren().add(new CardShower(card)));
			} else
			{
				if (card.getOwner() == Client.localPlayer)
					Platform.runLater(() -> GameMenu.instance().creatures.getChildren().add(new CardShower(card)));
				else
					Platform.runLater(() -> GameMenu.instance().adverseCreatures.getChildren().add(new CardShower(card)));
			}
		}
		if (Utils.getSide() == Side.SERVER)
		{
			PacketMixPlayDestroyed pkt = createPacket();
			pkt.dest = dest;
			pkt.exiled = exiled;
			pkt.index = index;
			pkt.p = p;
			PacketManager.sendPacketToParty(player.runningParty, pkt);
		} else
		{
			if (exiled)
			{
				List<PlayedCard> cards = data.getExile();
				PlayedCard last = cards.isEmpty() ? null : cards.get(cards.size() - 1);
				if (player == p)
					GameMenu.instance().playerInfos.exile(last == null ? null : last.duplicate());
				else
					GameMenu.instance().adverseInfos.exile(last == null ? null : last.duplicate());
			} else
			{
				List<PlayedCard> cards = data.getGraveyard();
				PlayedCard last = cards.isEmpty() ? null : cards.get(cards.size() - 1);
				if (player == p)
					GameMenu.instance().playerInfos.graveyard(last == null ? null : last.duplicate());
				else
					GameMenu.instance().adverseInfos.graveyard(last == null ? null : last.duplicate());
			}
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(p.uuid, buf);
		buf.writeBoolean(exiled);
		buf.writeByte(dest.ordinal());
		buf.writeInt(index);
	}

	public enum Destination
	{
		HAND, GRAVEYARD, EXILE, BATTLEFIELD, OTHER_BATTLEFIELD, UP_LIBRARY, DOWN_LIBRARY
	}
}