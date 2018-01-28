package fr.galaxyoyo.gatherplaying;

import com.google.common.base.Joiner;
import fr.galaxyoyo.gatherplaying.protocol.ClientChannelInitializer;
import fr.galaxyoyo.gatherplaying.protocol.ServerChannelInitializer;
import fr.galaxyoyo.gatherplaying.web.WebChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Utils
{
	public static final Random RANDOM = new Random();
	private static final com.gluonhq.charm.down.Platform platform = com.gluonhq.charm.down.Platform.getCurrent();
	public static boolean DEBUG = false;
	private static Side side = Side.CLIENT;

	public static String toSHA1(String str) { return toSHA1(str.getBytes(StandardCharsets.UTF_8)); }

	private static String toSHA1(byte[] array)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			digest.reset();
			digest.update(array);
			return toHexString(digest.digest());
		}
		catch (NoSuchAlgorithmException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	private static String toHexString(byte[] array)
	{
		Formatter formatter = new Formatter();
		for (byte b : array)
			formatter.format("%02x", b);
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	public static void startNetty() throws InterruptedException
	{
		if (getSide() == Side.CLIENT)
		{
			EventLoopGroup group = new NioEventLoopGroup();
			Bootstrap boot = new Bootstrap().group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.SO_RCVBUF, 0x42666).option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(0x42666))
					.option(ChannelOption.SO_REUSEADDR, false).handler(new ClientChannelInitializer());
			ChannelFuture f = boot.connect(DEBUG ? "localhost" : "galaxyoyo.com", 42666);
			try
			{
				f.get(1, TimeUnit.SECONDS);
				f.awaitUninterruptibly();
			}
			catch (ExecutionException | TimeoutException | InterruptedException ex)
			{
				Platform.runLater(() -> alert("Problème de connexion", "Connexion au serveur impossible",
						"Il semblerait qu'un problème de connexion ait lieu avec le serveur. Veuillez réessayer.\nSi le problème persiste, vérifiez votre connection et" +
								" le twitter, et prévenez galaxyoyo en cas de problème", Alert.AlertType.ERROR).ifPresent(buttonType -> System.exit(-1)));
			}
		}
		else
		{
			ServerBootstrap boot =
					new ServerBootstrap().group(new NioEventLoopGroup(1), new NioEventLoopGroup(1)).channel(NioServerSocketChannel.class)//.option(ChannelOption.SO_KEEPALIVE, true)
							.option(ChannelOption.SO_BACKLOG, 100).childOption(ChannelOption.SO_RCVBUF, 0x42666).childOption(ChannelOption.TCP_NODELAY, true)
							.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(0x42666)).childHandler(new ServerChannelInitializer());
			ChannelFuture f = boot.bind(42666);
			f.awaitUninterruptibly();

			ServerBootstrap webBoot =
					new ServerBootstrap().group(new NioEventLoopGroup(1), new NioEventLoopGroup(1)).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.DEBUG))
							.childHandler(new WebChannelInitializer());
			webBoot.bind(42000).sync();

			new Thread("Keep-Alive Thread")
			{
				@Override
				public void run()
				{
					//noinspection InfiniteLoopStatement
					while (true)
					{
						try
						{
							sleep(Long.MAX_VALUE);
						}
						catch (InterruptedException ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}.start();
		}
	}

	public static Side getSide() { return side; }

	public static Optional<ButtonType> alert(String title, String header, String content, Alert.AlertType type)
	{
		if (!Platform.isFxApplicationThread())
		{
			Platform.runLater(() ->
			{
				if (isDesktop())
				{
					Alert alert = new Alert(type);
					alert.setTitle(title);
					alert.setHeaderText(header);
					alert.setContentText(content);
					alert.showAndWait();
				}
				else
				{
					com.gluonhq.charm.glisten.control.Alert alert = new com.gluonhq.charm.glisten.control.Alert(Alert.AlertType.CONFIRMATION);
					alert.setTitleText(header);
					alert.setContentText(content);
					alert.showAndWait();
				}
			});
			return Optional.empty();
		}
		if (isDesktop())
		{
			Alert alert = new Alert(type);
			alert.setTitle(title);
			alert.setHeaderText(header);
			alert.setContentText(content);
			return alert.showAndWait();
		}
		else
			return Optional.empty();

	}

	public static boolean isDesktop()
	{
		return platform.equals(com.gluonhq.charm.down.Platform.DESKTOP);
	}

	public static void alert(String title, String header, String content)
	{
		alert(title, header, content, Alert.AlertType.INFORMATION);
	}

	public static void setup(String... args) throws FileNotFoundException
	{
		System.setErr(new PrintStream(new TeeOutputStream(System.err, new FileOutputStream(newFile("err.log"), true))));
		String joinedArgs = Joiner.on(' ').join(args);
		if (joinedArgs.contains("--server"))
			side = Side.SERVER;
		if (joinedArgs.contains("--debug"))
			DEBUG = true;
	}

	public static File newFile(String path)
	{
		switch (getPlatform())
		{
			case DESKTOP:
				return new File(path);
			case ANDROID:
				String DIR = System.getenv("EXTERNAL_STORAGE");
				if (DIR == null || DIR.isEmpty())
					DIR = "/storage/sdcard0";
				DIR += "/Gather Playing";
				return new File(DIR, path);
			default:
				throw new RuntimeException("OS not recognized: " + platform);
		}
	}

	public static com.gluonhq.charm.down.Platform getPlatform()
	{
		return platform;
	}

	@SuppressWarnings("unused")
	public static boolean isAndroid()
	{
		return platform == com.gluonhq.charm.down.Platform.ANDROID;
	}

	@SuppressWarnings("unused")
	public static boolean isIOS()
	{
		return platform == com.gluonhq.charm.down.Platform.IOS;
	}

	public static boolean isMobile()
	{
		return !isDesktop();
	}
}