package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import io.netty.buffer.ByteBuf;

import java.util.Set;

public class PacketMixSetCardSubtypes extends Packet
{
	public PlayedCard card;
	public Set<SubType> newSubtypes;

	@Override
	public void read(ByteBuf buf)
	{
		Player controller = player.runningParty.getPlayer(readUUID(buf));
		card = player.runningParty.getData(controller).getPlayed().get(buf.readShort());
		card.getSubtypes().clear();
		while (buf.isReadable())
			card.getSubtypes().add(SubType.valueOf(readUTF(buf)));
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(card.getController().uuid, buf);
		buf.writeShort(card.getController().runningParty.getData(card.getController()).getPlayed().indexOf(card));
		for (SubType s : newSubtypes)
			writeUTF(s.name, buf);
	}
}