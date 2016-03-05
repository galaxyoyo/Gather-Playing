package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Library;
import fr.galaxyoyo.gatherplaying.OwnedCard;
import io.netty.buffer.ByteBuf;

public class PacketInSelectDeck extends Packet
{
	public Library library;

	@Override
	public void read(ByteBuf buf)
	{
		Library library = new Library(player);
		while (buf.isReadable())
			library.addCard(new OwnedCard(readCard(buf), player, buf.readBoolean()));
		library.shuffle();
		player.getData().setLibrary(library);
		if (player.runningParty.getOnlinePlayers().size() == player.runningParty.size)
			player.runningParty.start();
	}

	@Override
	public void write(ByteBuf buf)
	{
		for (OwnedCard card : library.getSortedCards())
		{
			writeCard(card.getCard(), buf);
			buf.writeBoolean(card.isFoiled());
		}
	}
}