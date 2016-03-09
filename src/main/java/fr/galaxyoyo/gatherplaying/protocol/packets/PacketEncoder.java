package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<Packet>
{
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet pkt, ByteBuf buf) throws Exception
	{
		if (Utils.getSide() == Side.SERVER)
			pkt.player = Server.getPlayer(ctx.channel());
		else
			pkt.player = Client.localPlayer;
		buf.writeByte(PacketManager.getPacketId(pkt));
		pkt.write(buf);
	}
}