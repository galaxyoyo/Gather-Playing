package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.PlayerData;
import io.netty.buffer.ByteBuf;

public class PacketMixDestroyCard extends Packet
{
	public Destination dest = Destination.GRAVEYARD;
	public PlayedCard card;
	public int index = -1;

	@Override
	public void read(ByteBuf buf)
	{
		dest = Destination.values()[buf.readByte()];
		Player controller = player.runningParty.getPlayer(readUUID(buf));
		PlayerData data = player.runningParty.getData(controller);
		int index = buf.readInt();
		card = data.getPlayed().get(index);
		card.destroy(dest);
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(dest.ordinal());
		writeUUID(card.getController().uuid, buf);
		buf.writeInt(index >= 0 ? index : player.runningParty.getData(card.getController()).getPlayed().indexOf(card));
	}

	public enum Destination
	{
		GRAVEYARD, EXILE, HAND, UP_LIBRARY, DOWN_LIBRARY
	}
}