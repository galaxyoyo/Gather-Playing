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
		if (card.getCard().getMuId("en") == 74358)
		{
		/*	String number = card.getCard().getNumber().replaceAll("[^\\d]", "");
			String letter = card.getCard().getNumber().replace(number, "");
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
			}*/
			card.setRelatedCard(oldCard);
		}
		else
		{
			card.card.set(card.getRelatedCard());
			card.setRelatedCard(oldCard);
		}
		for (Marker m : card.getMarkers())
			m.onCardUnmarked(card);
		if (card.getCard().getType().is(CardType.CREATURE))
		{
			try
			{
				card.setPower(Integer.parseInt(card.getCard().getPower()));
			}
			catch (NumberFormatException ex)
			{
				card.setPower(0);
			}
			try
			{
				card.setToughness(Integer.parseInt(card.getCard().getToughness()));
			}
			catch (NumberFormatException ex)
			{
				card.setToughness(0);
			}
		}
		else if (card.getCard().getType().is(CardType.PLANESWALKER))
		{
			card.setLoyalty(0);
			for (int i = 0; i < card.getCard().getLoyalty(); ++i)
			{
				Marker m = MarkerType.LOYALTY.newInstance();
				m.onCardMarked(card);
				card.getMarkers().add(m);
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