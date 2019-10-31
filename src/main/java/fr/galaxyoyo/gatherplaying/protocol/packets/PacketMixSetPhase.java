package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Phase;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import io.netty.buffer.ByteBuf;

public class PacketMixSetPhase extends Packet
{
	public Phase phase;
	public Player p;

	@Override
	public void read(ByteBuf buf)
	{
		phase = Phase.values()[buf.readByte()];
		p = player.runningParty.getPlayer(readUUID(buf));
		if (Utils.getSide() == Side.CLIENT)
			GameMenu.instance().setPhase(phase);
		else
			sendToParty();
		player.runningParty.setPlayer(p);
		player.runningParty.setCurrentPhase(phase);
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(phase.ordinal());
		writeUUID(p.uuid, buf);
	}
}