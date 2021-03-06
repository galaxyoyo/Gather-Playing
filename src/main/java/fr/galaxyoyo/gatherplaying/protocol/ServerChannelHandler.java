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
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		if (cause instanceof IOException)
		{
			System.err.println(cause.getClass().getName() + " : " + cause.getMessage());
			Server.disconnect(ctx.channel());
		} else
			cause.printStackTrace();
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx)
	{
		ctx.channel().config().setMaxMessagesPerRead(1);
		Player player = new Player(ctx.channel());
		player.lastIp = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName();
		Server.addTempPlayer(player);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx)
	{
		Server.disconnect(ctx.channel());
	}

/*	@Override
	public void channelReadComplete(ChannelHandlerContext ctx)
	{
		ctx.flush();
	}*/
}