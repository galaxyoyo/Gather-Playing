package fr.galaxyoyo.gatherplaying.protocol;

import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerChannelHandler extends ChannelHandlerAdapter
{
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		ctx.channel().config().setMaxMessagesPerRead(1);
		Player player = new Player(ctx.channel());
		player.lastIp = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName();
		Server.addTempPlayer(player);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception { Server.disconnect(ctx.channel()); }

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception { ctx.flush(); }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		if (cause instanceof IOException)
		{
			System.err.println(cause.getClass().getName() + " : " + cause.getMessage());
			Server.disconnect(ctx.channel());
		}
		else
			cause.printStackTrace();
	}
}