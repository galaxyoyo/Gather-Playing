package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import io.netty.buffer.ByteBuf;

public class PacketMixSetLife extends Packet
{
	public PlayedCard card;
	public int newPower;
	public int newToughness;

	@Override
	public void read(ByteBuf buf)
	{
		Player controller = player.runningParty.getPlayer(readUUID(buf));
		card = player.runningParty.getData(controller).getPlayed().get(buf.readShort());
		newPower = buf.readInt();
		newToughness = buf.readInt();
		for (Marker m : card.getMarkers())
			m.onCardUnmarked(card);
		card.setPower(newPower);
		card.setToughness(newToughness);
		for (Marker m : card.getMarkers())
			m.onCardMarked(card);
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(card.getController().uuid, buf);
		buf.writeShort(player.runningParty.getData(card.getController()).getPlayed().indexOf(card));
		buf.writeInt(newPower);
		buf.writeInt(newToughness);
	}
}