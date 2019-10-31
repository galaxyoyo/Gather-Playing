package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Party;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;

public class PacketMixSelectStarter extends Packet
{
	public Player starter;

	@Override
	public void read(ByteBuf buf)
	{
		Party party = player.runningParty;
		starter = party.getPlayer(readUUID(buf));
		party.setPlayer(starter);
		party.start();
		if (Utils.getSide() == Side.SERVER)
		{
			Server.sendChat(player.runningParty, starter.name + " commence", "color: green;");
			sendToParty();
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(starter.uuid, buf);
	}
}
