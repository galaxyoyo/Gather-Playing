package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder
{
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> obj) throws Exception
	{
		if (buf instanceof EmptyByteBuf)
			return;
		byte dis = buf.readByte();
		Packet pkt = PacketManager.createPacket(dis);
		if (Utils.getSide() == Side.SERVER)
			pkt.player = Server.getPlayer(ctx.channel());
		else
			pkt.player = Client.localPlayer;
		try
		{
			pkt.read(buf);
		} catch (Throwable t)
		{
			t.printStackTrace();
		}
		obj.add(pkt);
		if (buf.isReadable())
			System.out.println("Warning: " + buf.readableBytes() + " bytes remains");
		buf.clear();
		ctx.flush();
	}
}