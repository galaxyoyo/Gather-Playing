package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.SubType;
import fr.galaxyoyo.gatherplaying.Player;
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
		card.subtypes.clear();
		while (buf.isReadable())
			card.subtypes.add(SubType.valueOf(readUTF(buf)));
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(card.controller.uuid, buf);
		buf.writeShort(card.controller.runningParty.getData(card.controller).getPlayed().indexOf(card));
		for (SubType s : newSubtypes)
			writeUTF(s.name, buf);
	}
}