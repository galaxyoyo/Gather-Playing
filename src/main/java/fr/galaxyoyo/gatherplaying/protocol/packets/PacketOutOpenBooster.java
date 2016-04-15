package fr.galaxyoyo.gatherplaying.protocol.packets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.Rules;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.DeckEditor;
import io.netty.buffer.ByteBuf;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.util.List;

public class PacketOutOpenBooster extends Packet
{
	public List<Card> cards;

	@Override
	public void read(ByteBuf buf)
	{
		cards = Lists.newArrayList();
		while (buf.isReadable())
			cards.add(readCard(buf));

		if (Client.getRunningParty() != null)
		{
			if (Client.getRunningParty().getRules() == Rules.SEALED)
			{
				//noinspection unchecked
				ObservableList<Card> list =
						(ObservableList<Card>) ((SortedList<Card>) ((FilteredList<Card>) ((SortedList<Card>) DeckEditor.getEditor().table.getItems()).getSource()).getSource())
								.getSource();
				list.addAll(cards);
			}
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		for (Card card : cards)
			writeCard(card, buf);
	}
}
