package fr.galaxyoyo.gatherplaying.protocol.packets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;

import java.util.List;

public class PacketMixDrawCard extends Packet
{
	public Action action = Action.DRAW;
	public int count = 1;
	public List<OwnedCard> cards;

	@Override
	public void read(ByteBuf buf)
	{
		PlayerData data = player.getData();
		action = Action.values()[buf.readByte()];
		count = buf.readInt();
		cards = Lists.newArrayList();
		if (Utils.getSide() == Side.SERVER)
		{
			if (action == Action.MULLIGAN)
			{
				count = data.getMulligan() - 1;
				if (count <= 0)
					return;
				data.setMulligan((byte) count);
				for (OwnedCard card : data.getHand())
					data.getLibrary().addCard(card);
				data.getHand().clear();
				data.getLibrary().shuffle();
				Server.sendChat(player.runningParty, "chat.mulligan", "color: green;", player.name, Integer.toString(count), count > 1 ? "text.card" : "text.cards");
			} else
				Server.sendChat(player.runningParty, "chat.draw", "color: green;", player.name, Integer.toString(count), count > 1 ? "text.card" : "text.cards");
			if (data.getLibrary().getSortedCards().size() < count)
				return;
			for (int i = 0; i < count; ++i)
			{
				OwnedCard card = data.getLibrary().drawCard();
				data.getHand().add(card);
				cards.add(card);
			}
			sendToParty();
		} else
		{
			Player p = Client.getRunningParty().getPlayer(readUUID(buf));
			data = Client.getRunningParty().getData(p);
			if (action == Action.MULLIGAN)
			{
				data.setMulligan((byte) (count - 1));
				if (p == player)
				{
					Platform.runLater(() -> GameMenu.instance().hand.getChildren().clear());
					GameMenu.instance().playerInfos.addLibrary(data.getHand().size());
				} else
				{
					Platform.runLater(() -> GameMenu.instance().adverseHand.getChildren().clear());
					GameMenu.instance().adverseInfos.addLibrary(data.getHand().size());
				}
				data.getHand().clear();
			}
			for (int i = 0; i < count; ++i)
				cards.add(new OwnedCard(readCard(buf), p, buf.readBoolean()));
			data.getHand().addAll(cards);
			if (p == player)
			{
				GameMenu.instance().playerInfos.addLibrary(-count);
				for (OwnedCard card : cards)
					Platform.runLater(() -> GameMenu.instance().hand.getChildren().add(new CardShower(card)));
			} else
			{
				GameMenu.instance().adverseInfos.addLibrary(-count);
				for (OwnedCard card : cards)
					Platform.runLater(() -> GameMenu.instance().adverseHand.getChildren().add(new CardShower(card)));
			}
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(action.ordinal());
		buf.writeInt(count);
		if (Utils.getSide() == Side.SERVER)
		{
			writeUUID(cards.get(0).getOwner().uuid, buf);
			for (OwnedCard card : cards)
			{
				writeCard(card.getCard(), buf);
				buf.writeBoolean(card.isFoiled());
			}
		}
	}

	public enum Action
	{
		DRAW, MULLIGAN
	}
}