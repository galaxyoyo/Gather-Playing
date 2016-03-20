package fr.galaxyoyo.gatherplaying;

import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.Config;
import fr.galaxyoyo.gatherplaying.client.I18n;
import fr.galaxyoyo.gatherplaying.client.gui.CardImageManager;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class Main
{
	public static void main(String... args) throws Throwable
	{
		List<String> locales = Arrays.asList("en", "de", "fr", "it", "es", "pt", "ru", "zh", "ja", "ko");
		if (!locales.contains(String.valueOf(Config.getLocale().getLanguage()).toLowerCase()))
		{
			if (!locales.contains(Config.getLocale().getLanguage()))
				Locale.setDefault(Locale.US);
		}
		switch (Locale.getDefault().getLanguage())
		{
			case "en":
			case "de":
			case "fr":
			case "it":
			case "es":
			case "ja":
			case "ko":
			case "ru":
				Locale.setDefault(new Locale(Locale.getDefault().getLanguage()));
				break;
			case "pt":
				Locale.setDefault(new Locale("pt", "BR"));
				break;
			case "zh":
				if (Locale.getDefault().getCountry() == null)
					Locale.setDefault(Locale.PRC);
				break;
		}
		System.setProperty("monocle.stackSize", "128000");
		System.setProperty("file.encoding", "UTF-8");
		detectDependencies();
		I18n.reloadTranslations();
		Utils.setup(args);

		if (Utils.getSide() == Side.CLIENT)
		{
			Executors.newSingleThreadExecutor().submit(() -> {
				try
				{
					if (Utils.isDesktop())
						Client.launch(Client.class, args);
					else
						Client.MobileClient.launch(Client.MobileClient.class, args);
				}
				catch (Throwable t)
				{
					t.printStackTrace();
					throw new RuntimeException(t);
				}
			});
		}
		MySQL.init();

	/*	String content = "";
		List<Token> tokens = Lists.newArrayList(Token.values());
		tokens.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getEnglishName(), o2.getEnglishName()));
		for (Token token : tokens)
		{
		//	if (token) == CardType.EMBLEM)
		//		continue;
			content += "\"" + token.getEnglishName() + "\",\"" + token.getTranslatedName().get() + "\"," + token.set.code + ",\"";
			String type = token).toString();
			if (token.legendary)
				type = type.replace("-jeton", " légendaire-jeton");
			if (token.subtypes.length > 0)
				type += " : " + Joiner.on(" et ").join(token.subtypes).toLowerCase();
			content += type + "\",\"" + token.ability_FR.replace("_", "").replace("\n", "\\n") + "\",\n";
		}
		System.out.println(content);
		FileUtils.writeStringToFile(new File("tokens.txt"), content);
		System.exit(0);*/
		//	PreconstructedDeck.loadAll();
		Utils.startNetty();
	}

	private static void detectDependencies()
	{
		File dir = new File("libs");
		detectDependency("java8.lang.FunctionalInterface", new File(dir, "streamsupport-1.4.2.jar"),
				"http://central.maven.org/maven2/net/sourceforge/streamsupport/streamsupport/1.4.2/streamsupport-1.4.2.jar");
		detectDependency("com.gluonhq.charm.glisten.Glisten", new File(dir, "charm-2.0.0.jar"),
				"http://nexus.gluonhq.com/nexus/content/repositories/releases/com/gluonhq/charm/2.0.0/charm-2.0.0.jar");
		detectDependency("com.gluonhq.charm.down.desktop.DesktopPlatform", new File(dir, "charm-desktop-2.0.0.jar"),
				"http://nexus.gluonhq.com/nexus/content/repositories/releases/com/gluonhq/charm-desktop/2.0.0/charm-desktop-2.0.0.jar");
		detectDependency("org.sqlite.JDBC", new File(dir, "sqlite-jdbc-3.8.11.2.jar"), "http://central.maven.org/maven2/org/xerial/sqlite-jdbc/3.8.11.2/sqlite-jdbc-3.8.11.2.jar");
		detectDependency("com.google.gson.Gson", new File(dir, "gson-2.5.jar"), "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.5/gson-2.5.jar");
		detectDependency("com.google.common.base.Joiner", new File(dir, "guava-19.0-rc3.jar"), "https://repo1.maven.org/maven2/com/google/guava/guava/19.0-rc3/guava-19.0-rc3.jar");
		detectDependency("org.apache.commons.io.IOUtils", new File(dir, "commons-io-2.4.jar"), "https://repo1.maven.org/maven2/commons-io/commons-io/2.4/commons-io-2.4.jar");
		detectDependency("org.apache.commons.lang3.StringUtils", new File(dir, "commons-lang3-2.4.jar"),
				" https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar");
		detectDependency("io.netty.bootstrap.Bootstrap", new File(dir, "netty-all-5.0.0.Alpha2.jar"),
				"https://repo1.maven.org/maven2/io/netty/netty-all/5.0.0.Alpha2/netty-all-5.0.0.Alpha2.jar");
		detectDependency("com.google.protobuf.Any", new File(dir, "protobuf-java-3.0.0-beta-1.jar"),
				"https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/3.0.0-beta-1/protobuf-java-3.0.0-beta-1.jar");
	}

	private static void detectDependency(String clazz, File file, String urlDL)
	{
		try
		{
			Class.forName(clazz);
		} catch (ClassNotFoundException e)
		{
			try
			{
				if (!file.getParentFile().exists())
					//noinspection ResultOfMethodCallIgnored
					file.getParentFile().mkdirs();
				if (!file.exists())
				{
					System.out.println(file);
					URL url = new URL(urlDL);
					HttpURLConnection co = (HttpURLConnection) url.openConnection();
					BufferedInputStream bis = new BufferedInputStream(co.getInputStream());
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
					int count;
					byte[] buffer = new byte[512 * 1024];
					while ((count = bis.read(buffer)) != -1)
						bos.write(buffer, 0, count);
					bis.close();
					bos.close();
				}
				addURL(file.toURI().toURL());
			} catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	private static void addURL(URL url)
	{
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		try
		{
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			method.invoke(sysloader, url);
		} catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void generateDraft(int players) throws IOException
	{
		//	StreamSupport.stream(MySQL.getAllCards()).filter(card -> "Lande".equals(card.name.get("fr"))).forEach(card -> card.rarity = Rarity.COMMON);
		for (int i = 0; i < players * 3; ++i)
		{
			BufferedImage img = new BufferedImage(4320, 3060, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			Card[] booster = MySQL.getSet("OGW").generateBooster();
			int x = 0, y = 0;
			for (int j = 0; j < booster.length; ++j)
			{
				Card card = booster[j];
				if (card == null)
					continue;
				BufferedImage subimg = ImageIO.read(new File("pics\\OGW", card.getMuId().get("fr") + ".png"));
				g.drawImage(subimg, x, y, 720, 1020, null);
				if (booster.length - j > 14)
					g.drawImage(SwingFXUtils.fromFXImage(CardImageManager.getFoilCover(), null), x, y, 720, 1020, null);
				x += 720;
				if (x >= 4320)
				{
					x = 0;
					y += 1020;
				}
				System.out.println(x + ", " + y);
			}
			g.dispose();
			ImageIO.write(img, "JPG", new File("draft", (i + 1) + ".jpg"));
		}
		System.exit(0);
	}
}