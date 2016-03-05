package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.markers.MarkerType;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;

public class PacketMixAddMarker extends Packet
{
	public MarkerType type;
	public PlayedCard card;

	@Override
	public void read(ByteBuf buf)
	{
		type = MarkerType.values()[buf.readByte()];
		Player controller = player.runningParty.getPlayer(readUUID(buf));
		card = player.runningParty.getData(controller).getPlayed().get(buf.readInt());
		Marker marker = type.newInstance();
		marker.onCardMarked(card);
		card.markers.add(marker);
		if (Utils.getSide() == Side.SERVER)
		{
			sendToParty();
			Server.sendChat(player.runningParty, "chat.addmarker", null, marker.getType().getTranslatedName(), "<i>" + card.getTranslatedName().get() + "</i>");
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(type.ordinal());
		writeUUID(card.controller.uuid, buf);
		buf.writeInt(player.runningParty.getData(card.controller).getPlayed().indexOf(card));
	}
}