package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.Player;
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
		for (Marker m : card.markers)
			m.onCardUnmarked(card);
		card.power.set(newPower);
		card.toughness.set(newToughness);
		for (Marker m : card.markers)
			m.onCardMarked(card);
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(card.controller.uuid, buf);
		buf.writeShort(player.runningParty.getData(card.controller).getPlayed().indexOf(card));
		buf.writeInt(newPower);
		buf.writeInt(newToughness);
	}
}