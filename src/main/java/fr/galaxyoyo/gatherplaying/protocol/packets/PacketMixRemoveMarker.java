package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;

public class PacketMixRemoveMarker extends Packet
{
	public PlayedCard card;
	public short index;

	@Override
	public void read(ByteBuf buf)
	{
		Player controller = player.runningParty.getPlayer(readUUID(buf));
		card = player.runningParty.getData(controller).getPlayed().get(buf.readShort());
		index = buf.readShort();
		Marker marker = card.getMarkers().remove(index);
		marker.onCardUnmarked(card);
		if (Utils.getSide() == Side.SERVER)
		{
			sendToParty();
			Server.sendChat(player.runningParty, "chat.removemarker", null, marker.getType().getTranslatedName(), "<i>" + card.getTranslatedName().get() + "</i>");
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(card.getController().uuid, buf);
		buf.writeShort(player.runningParty.getData(card.getController()).getPlayed().indexOf(card));
		buf.writeShort(index);
	}
}