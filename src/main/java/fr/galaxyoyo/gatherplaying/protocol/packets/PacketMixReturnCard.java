package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.markers.MarkerType;
import io.netty.buffer.ByteBuf;

public class PacketMixReturnCard extends Packet
{
	public short index;
	public Player p;

	@Override
	public void read(ByteBuf buf)
	{
		index = buf.readShort();
		p = player.runningParty.getPlayer(readUUID(buf));
		PlayedCard card = player.runningParty.getData(p).getPlayed().get(index);
		Card oldCard = card.getCard();
		if (card.getCard().muId.get("en").contains("74358"))
		{
			String number = card.getCard().muId.get("en").replaceAll("[^\\d]", "");
			String letter = card.getCard().muId.get("en").replace(number, "");
			switch (letter)
			{
				case "a":
					card.card.set(MySQL.getCard(number + "b"));
					break;
				case "b":
					card.card.set(MySQL.getCard(number + "c"));
					break;
				case "c":
					card.card.set(MySQL.getCard(number + "d"));
					break;
				case "d":
					card.card.set(MySQL.getCard(number + "e"));
					break;
				default:
					card.card.set(MySQL.getCard(number + "a"));
					break;
			}
			card.relatedCard = oldCard;
		}
		else
		{
			card.card.set(card.relatedCard);
			card.relatedCard = oldCard;
		}
		for (Marker m : card.markers)
			m.onCardUnmarked(card);
		if (card.getCard().type.is(CardType.CREATURE))
		{
			try
			{
				card.power.set(Integer.parseInt(card.getCard().power));
			}
			catch (NumberFormatException ex)
			{
				card.power.set(0);
			}
			try
			{
				card.toughness.set(Integer.parseInt(card.getCard().toughness));
			}
			catch (NumberFormatException ex)
			{
				card.toughness.set(0);
			}
		}
		else if (card.getCard().type.is(CardType.PLANESWALKER))
		{
			card.loyalty.set(0);
			for (int i = 0; i < card.getCard().loyalty; ++i)
			{
				Marker m = MarkerType.LOYALTY.newInstance();
				m.onCardMarked(card);
				card.markers.add(m);
			}
		}
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeShort(index);
		writeUUID(p.uuid, buf);
	}
}