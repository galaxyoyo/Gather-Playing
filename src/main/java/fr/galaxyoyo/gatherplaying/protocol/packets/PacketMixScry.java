package fr.galaxyoyo.gatherplaying.protocol.packets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.I18n;
import fr.galaxyoyo.gatherplaying.client.gui.FoundCardShower;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;

import java.util.List;

public class PacketMixScry extends Packet
{
	public byte numCards;
	public List<OwnedCard> cards;

	@Override
	public void read(ByteBuf buf)
	{
		numCards = buf.readByte();
		if (Utils.getSide() == Side.SERVER)
		{
			Library lib = player.getData().getLibrary();
			List<OwnedCard> list = Lists.newArrayList();
			for (int i = 0; i < numCards; ++i)
				list.add(lib.getSortedCards().get(i));
			PacketMixScry pkt = createPacket();
			pkt.numCards = numCards;
			pkt.cards = list;
			PacketManager.sendPacketToPlayer(player, pkt);
			Server.sendChat(player.runningParty, "chat.scry", null, player.name, (numCards > 1 ? I18n.strTr("text.thecards", Integer.toString(numCards)) : "text.thecard"));
		}
		else
		{
			List<OwnedCard> allCards = Lists.newArrayList();
			while (buf.isReadable())
				allCards.add(new OwnedCard(readCard(buf), player, buf.readBoolean()));
			Platform.runLater(() -> {
				Dialog<Object> dialog = new Dialog<>();
				HBox box = new HBox();
				for (OwnedCard c : allCards)
					box.getChildren().add(new FoundCardShower(c));
				dialog.getDialogPane().setContent(box);
				dialog.showAndWait();
			});
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(numCards);
		if (Utils.getSide() == Side.SERVER)
		{
			for (OwnedCard card : cards)
			{
				writeCard(card.getCard(), buf);
				buf.writeBoolean(card.isFoiled());
			}
		}
	}
}