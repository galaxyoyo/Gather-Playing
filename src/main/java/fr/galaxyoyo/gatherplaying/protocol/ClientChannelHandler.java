package fr.galaxyoyo.gatherplaying.protocol;

import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.LoginDialog;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;

import java.io.IOException;

public class ClientChannelHandler extends ChannelHandlerAdapter
{
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		if (cause instanceof IOException)
			System.err.println(cause.getClass().getName() + " : " + cause.getMessage());
		else
			cause.printStackTrace();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		ctx.channel().config().setMaxMessagesPerRead(1);
		Client.localPlayer = new Player(ctx.channel());
		Platform.runLater(() -> Client.show(LoginDialog.class));
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception { ctx.flush(); }
}