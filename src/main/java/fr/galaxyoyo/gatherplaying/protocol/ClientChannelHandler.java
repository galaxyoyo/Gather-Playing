package fr.galaxyoyo.gatherplaying.protocol;

import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.LoginDialog;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ClientChannelHandler extends ChannelHandlerAdapter
{
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		cause.printStackTrace();
		Platform.runLater(() -> Utils.alert("Erreur de connexion", "Problème de connexion avec le serveur", "Un problème de connexion est survenu. Malheureusement, il n'y a pas " +
				"encore de système de reconnexion de géré, le jeu va désormais se fermer.", Alert.AlertType.WARNING).ifPresent(buttonType -> System.exit(-1)));
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