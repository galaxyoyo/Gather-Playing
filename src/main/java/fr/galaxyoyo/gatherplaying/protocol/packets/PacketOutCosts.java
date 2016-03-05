package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.MySQL;
import fr.galaxyoyo.gatherplaying.Rarity;
import fr.galaxyoyo.gatherplaying.Set;
import io.netty.buffer.ByteBuf;

public class PacketOutCosts extends Packet
{
	public Set set;

	@Override
	public void read(ByteBuf buf)
	{
		Set set = null;
		while (buf.readByte() != -1)
		{
			String muId = readUTF(buf);
			Card card = MySQL.getCard(muId);
			set = card.set;
			card.cost = buf.readDouble();
			card.foilCost = buf.readDouble();
			card.rarity = Rarity.values()[buf.readByte()];
		}
		if (set != null)
			System.out.println(set.geName() + " (" + set.code + ") : " + set.cards.size() + " cartes");
	}

	@Override
	public void write(ByteBuf buf)
	{
		for (Card card : set.cards)
		{
			if (card.cost == 0.0D)
				continue;
			buf.writeByte(0);
			writeUTF(card.muId.get("en"), buf);
			buf.writeDouble(card.cost);
			buf.writeDouble(card.foilCost);
			buf.writeByte(card.rarity.ordinal());
		}
		buf.writeByte(-1);
	}
}