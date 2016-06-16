package fr.galaxyoyo.gatherplaying.protocol.packets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.Rules;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.DeckEditor;
import fr.galaxyoyo.gatherplaying.client.gui.DeckShower;
import fr.galaxyoyo.gatherplaying.client.gui.DraftWindow;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
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
			else
			{
				if (cards.stream().anyMatch(Card::isBasic))
				{
					Platform.runLater(() -> {
						DeckEditor editor = Client.show(DeckEditor.class);
						assert editor != null;
						DeckShower shower = DeckEditor.getDeckShower();
						shower.initForLimited();
						//noinspection unchecked
						ObservableList<Card> list =
								(ObservableList<Card>) ((SortedList<Card>) ((FilteredList<Card>) ((SortedList<Card>) DeckEditor.getEditor().table.getItems()).getSource()).getSource())
										.getSource();
						list.clear();
						list.addAll(cards);
					});
				}
				else
					DraftWindow.instance().showCards(cards);
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
