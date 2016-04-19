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
			set = card.getSet();
			card.setCost(buf.readDouble());
			card.setFoilCost(buf.readDouble());
			card.setRarity(Rarity.values()[buf.readByte()]);
		}
		if (set != null)
			System.out.println(set.getTranslatedName() + " (" + set.getCode() + ") : " + set.getCards().size() + " cartes");
	}

	@Override
	public void write(ByteBuf buf)
	{
		for (Card card : set.getCards())
		{
			if (card.getCost() == 0.0D)
				continue;
			buf.writeByte(0);
			writeUTF(card.getMuId("en"), buf);
			buf.writeDouble(card.getCost());
			buf.writeDouble(card.getFoilCost());
			buf.writeByte(card.getRarity().ordinal());
		}
		buf.writeByte(-1);
	}
}