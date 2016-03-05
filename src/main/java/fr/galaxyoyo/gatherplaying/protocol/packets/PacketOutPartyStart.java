package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Phase;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.client.gui.PlayerInfos;
import io.netty.buffer.ByteBuf;

public class PacketOutPartyStart extends Packet
{
	public Player starter;

	@Override
	public void read(ByteBuf buf)
	{
		for (Player ignored : Client.getRunningParty().getOnlinePlayers())
		{
			Player p = Client.getRunningParty().getPlayer(readUUID(buf));
			Client.getRunningParty().getData(p).setLibrary(null);
			if (p != Client.localPlayer)
				GameMenu.INSTANCE.adverseInfos.setPlayer(p);
			PlayerInfos.getInfos(p).setLibrary(buf.readByte());
		}
		GameMenu.INSTANCE.setPhase(Phase.MAIN);
		starter = Client.getRunningParty().getPlayer(readUUID(buf));
		Client.getRunningParty().player = starter;
		Client.getRunningParty().start();
	}

	@Override
	public void write(ByteBuf buf)
	{
		for (Player p : starter.runningParty.getOnlinePlayers())
		{
			writeUUID(p.uuid, buf);
			buf.writeByte(player.runningParty.getData(p).getLibrary().getSortedCards().size());
		}
		writeUUID(starter.uuid, buf);
	}
}