package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
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
		card.setType(newType);
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(card.getController().uuid, buf);
		buf.writeShort(card.getController().runningParty.getData(card.getController()).getPlayed().indexOf(card));
		buf.writeByte(newType.ordinal());
	}
}