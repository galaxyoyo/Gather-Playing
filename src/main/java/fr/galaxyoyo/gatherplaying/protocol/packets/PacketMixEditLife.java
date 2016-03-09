package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Party;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;

public class PacketMixEditLife extends Packet
{
	public int newLife;
	public Player p;

	@Override
	public void read(ByteBuf buf)
	{
		if (Utils.getSide() == Side.SERVER)
		{
			Party party = Server.getParty(buf.readInt());
			p = party.getPlayer(readUUID(buf));
			newLife = buf.readInt();
			int diff = newLife - party.getData(p).getHp();
			party.getData(p).setHp(newLife);
			PacketMixEditLife pkt = createPacket();
			pkt.newLife = newLife;
			pkt.p = p;
			PacketManager.sendPacketToParty(party, pkt);
			if (diff > 0)
				Server.sendChat(party, p.name + " gagne " + diff + " point" + (diff > 1 ? "s" : "") + " de vie", "color: green;");
			else
				Server.sendChat(party, p.name + " perd " + -diff + " point" + (diff < -1 ? "s" : "") + " de vie", "color: green;");
		} else
		{
			buf.readInt();
			Party party = Client.getRunningParty();
			p = party.getPlayer(readUUID(buf));
			newLife = buf.readInt();
			party.getData(p).setHp(newLife);
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeInt(p.runningParty.getId());
		writeUUID(p.uuid, buf);
		buf.writeInt(newLife);
	}
}