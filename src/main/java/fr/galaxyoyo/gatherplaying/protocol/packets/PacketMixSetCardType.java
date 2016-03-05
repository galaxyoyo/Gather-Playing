package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.CardType;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Player;
import io.netty.buffer.ByteBuf;

public class PacketMixSetCardType extends Packet
{
	public PlayedCard card;
	public CardType newType;

	@Override
	public void read(ByteBuf buf)
	{
		Player controller = player.runningParty.getPlayer(readUUID(buf));
		card = player.runningParty.getData(controller).getPlayed().get(buf.readShort());
		newType = CardType.values()[buf.readByte()];
		card.type = newType;
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(card.controller.uuid, buf);
		buf.writeShort(card.controller.runningParty.getData(card.controller).getPlayed().indexOf(card));
		buf.writeByte(newType.ordinal());
	}
}