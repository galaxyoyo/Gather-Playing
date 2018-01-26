package fr.galaxyoyo.gatherplaying;

import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.Config;
import fr.galaxyoyo.gatherplaying.client.I18n;
import fr.galaxyoyo.gatherplaying.client.gui.CardImageManager;
import fr.galaxyoyo.gatherplaying.rendering.CardRenderer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
			Executors.newSingleThreadExecutor().submit(() ->
			{
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
		Utils.startNetty();

	/*	String text = "// NAME: Eternal Masters\n// \n// This deck file wasn't generated.\n// \n";
		Set set = MySQL.getSet("EMA");
		for (Card card : set.getCards())
			text += "\t1 [EMA] " + card.getName().get("en") + "\n";
		FileUtils.write(new File("C:\\MTG\\Cardgen\\cardgen-9.0.14", "EMA.mwDeck"), text);

		text = "";
		for (Card card : set.getCards())
		{
			if (card.getName().get("en").contains(","))
				text += "\"";
			text += card.getName().get("en");
			if (card.getName().get("en").contains(","))
				text += "\"";
			text += ",EMA,";
			ManaColor[] colors = card.getColors();
			if (colors.length != 1)
				text += "Gld,";
			else if (card.getType() == CardType.LAND)
				text += "Land,";
			else if (colors[0] == ManaColor.COLORLESS)
				text += "Art,";
			else
				text += colors[0].getAbbreviate() + ",";
			if (card.isLegendary())
				text += "Legendary ";
			if (card.isBasic())
				text += "Basic ";
			text += I18n.entr("type." + card.getType().name().toLowerCase());
			SubType[] types = card.getSubtypes();
			if (types.length != 0)
			{
				text += " — ";
				for (SubType type : types)
					text += I18n.entr("subtype." + type.name.toLowerCase()) + " ";
				text = text.substring(0, text.length() - 1);
			}
			text += ",";
			if (card.getType().is(CardType.CREATURE))
				text += card.getPower() + "\\" + card.getToughness();
			if (card.getType().is(CardType.PLANESWALKER))
				text += "\\" + card.getLoyalty();
			text += ",";
			if (card.getFlavor() != null)
			{
				if (card.getFlavor().contains("\n") || card.getFlavor().contains(","))
					text += "\"";
				String flavor = card.getFlavor();
				while (flavor.contains("\""))
					flavor = flavor.replaceFirst("\"", "“").replaceFirst("\"", "”");
				text += flavor;
				if (card.getFlavor().contains("\n") || card.getFlavor().contains(","))
					text += "\"";
			}
			text += "," + (card.getRarity() == Rarity.BASIC_LAND ? 'L' : card.getRarity().name().charAt(0)) + ",";
			ManaColor[] cost = card.getManaCost();
			if (cost != null)
			{
				for (ManaColor color : cost)
					text += "{" + color.getAbbreviate().replace("/", "") + "}";
			}
			text += ",";
			if (card.getAbility() != null)
			{
				if (card.getAbility().contains("\n") || card.getAbility().contains(","))
					text += "\"";
				String ability = card.getAbility();
				while (ability.contains("\""))
					ability = ability.replaceFirst("\"", "“").replaceFirst("\"", "”");
				text += ability;
				if (card.getAbility().contains("\n") || card.getAbility().contains(","))
					text += "\"";
			}
			text += ",," + card.getArtist() + "," + card.getNumber() + "\\249\n";
		}
		FileUtils.write(new File("C:\\MTG\\Cardgen\\cardgen-9.0.14", "data.csv"), text);*/

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
		//	Utils.startNetty();

	/*	Platform.runLater(() -> {
			Set set = MySQL.getSet("SOI");
			List<Card> cards = set.getCards().stream().filter(card -> card.getRarity() == Rarity.RARE || card.getRarity() == Rarity.MYTHIC).collect(Collectors.toList());
			cards.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getMuId("en"), o2.getMuId("en")));

			CardDetailsShower shower = null;
			try
			{
				FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/CardDetailsShower.fxml"));
				loader.load();
				shower = loader.getController();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return;
			}

		//	AtomicDouble total = new AtomicDouble(0.0D);

			Stage showerStage = new Stage();
			ImageView view = new ImageView();
			view.setFitWidth(446);
			view.setFitHeight(620);
			showerStage.setScene(new Scene(new Group(view)));
			showerStage.show();

			TextField cost = new TextField("0.0 € / 0.0 € (0.0 €)");
			cost.setStyle("-fx-display-caret: false;");
			cost.setFont(new Font(42));

			Stage costStage = new Stage();
			costStage.setTitle("Prix");
			costStage.setAlwaysOnTop(true);
			VBox box = new VBox(cost);
			costStage.setScene(new Scene(box, 600, cost.getPrefHeight()));
			VBox.setVgrow(cost, Priority.ALWAYS);
			costStage.getScene().setFill(Color.TRANSPARENT);
			costStage.show();

			Stage selector = new Stage();
			selector.setTitle("Rares & mythiques de " + set.getTranslatedName());
			FlowPane pane = new FlowPane();
			pane.setHgap(4.0D);
			pane.setVgap(4.0D);
			pane.setPrefWrapLength(920);
			for (Card card : cards)
			{
				if (card.getRarity() != Rarity.RARE && card.getRarity() != Rarity.MYTHIC)
					continue;

				card.setCost(21.0D);
				card.setFoilCost(42.0D);

				ImageView image = new ImageView(CardImageManager.getImage(card));
				CardDetailsShower finalShower = shower;
				image.setOnMouseClicked(event -> {
					finalShower.updateCard(card);
					view.setImage(CardImageManager.getImage(card));
					showerStage.setTitle(card.getTranslatedName().get());
				});
				pane.getChildren().add(image);
			}
			selector.setScene(new Scene(new ScrollPane(pane), 920, 600));
			selector.show();
		});*/

	/*	ObservableList<Card> stackedCards = FXCollections.observableArrayList();
		ObservableSet<String> added = FXCollections.observableSet();
		StreamSupport.stream(MySQL.getAllCards()).filter(card -> !added.contains(card.getName().get("en"))).forEach(card -> {
			stackedCards.add(card);
			added.add(card.getName().get("en"));
		});
		ObservableList<Card> cards = new SortedList<>(stackedCards);

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);

		Element root = doc.createElement("cockatrice_carddatabase");
		root.setAttribute("version", "3");
		doc.appendChild(root);

		Element setsElem = doc.createElement("sets");
		for (Set set : MySQL.getAllSets())
		{
			Element setElem = doc.createElement("set");

			Element name = doc.createElement("name");
			name.setTextContent(set.getCode());
			setElem.appendChild(name);

			Element longname = doc.createElement("longname");
			longname.setTextContent(set.getTranslatedName());
			setElem.appendChild(longname);

			Element settype = doc.createElement("settype");
			settype.setTextContent(set.getType());
			setElem.appendChild(settype);

			Element releaseDate = doc.createElement("releasedate");
			releaseDate.setTextContent(new SimpleDateFormat("yyyy-MM-dd").format(set.getReleaseDate()));
			setElem.appendChild(releaseDate);

			setsElem.appendChild(setElem);
		}
		root.appendChild(setsElem);

		Element cardsElem = doc.createElement("cards");
		for (Card card : cards)
		{
			System.out.println(card.getTranslatedName().get() + " (" + cardsElem.getChildNodes().getLength() + ")");

			Element cardElem = doc.createElement("card");

			Element name = doc.createElement("name");
			name.setTextContent(card.getName().get("en").replace("Æ", "AE"));
			cardElem.appendChild(name);

			List<Card> matching = StreamSupport.stream(MySQL.getAllCards()).filter(c -> c.getName().get("en").equals(card.getName().get("en"))).collect(Collectors.toList());
			for (Card c : matching)
			{
				Element setElem = doc.createElement("set");
				setElem.setAttribute("muId", c.getPreferredMuID());
				setElem.setTextContent(c.getSet().getCode());
				cardElem.appendChild(setElem);
			}

			for (ManaColor mc : card.getColors())
			{
				Element color = doc.createElement("color");
				color.setTextContent(mc.getAbbreviate());
				cardElem.appendChild(color);
			}

			if (card.getManaCost() != null)
			{
				Element cost = doc.createElement("manacost");
				for (ManaColor mc : card.getManaCost())
					cost.setTextContent(cost.getTextContent() + mc.getAbbreviate());
				cardElem.appendChild(cost);
			}

			Element cmc = doc.createElement("cmc");
			cmc.setTextContent(Integer.toString((int) card.getCmc()));
			cardElem.appendChild(cmc);

			Element type = doc.createElement("type");
			String fullType = card.getType().getTranslatedName().get();
			if (card.isBasic())
				fullType += " de base";
			else if (card.isLegendary())
				fullType += " légendaire";
			else if (card.isWorld())
				fullType += " du monde";
			else if (card.isOngoing())
				fullType += " en avant";
			if (card.getSubtypes().length > 0)
				fullType += " — ";
			for (int i = 0; i < card.getSubtypes().length; ++i)
			{
				fullType += card.getSubtypes()[i].toString().toLowerCase();
				if (i != card.getSubtypes().length - 1)
					fullType += " et ";
			}
			type.setTextContent(fullType);
			cardElem.appendChild(type);

			if (card.getType().is(CardType.CREATURE))
			{
				Element pt = doc.createElement("pt");
				pt.setTextContent(card.getPower() + "/" + card.getToughness());
				cardElem.appendChild(pt);
			}
			else if (card.getType().is(CardType.PLANESWALKER))
			{
				Element loyalty = doc.createElement("loyalty");
				loyalty.setTextContent(Integer.toString(card.getLoyalty()));
				cardElem.appendChild(loyalty);
			}

			Element tablerow = doc.createElement("tablerow");
			tablerow.setTextContent("1");
			if (card.getType().is(CardType.CREATURE))
				tablerow.setTextContent("2");
			if (!card.getType().isPermanent())
				tablerow.setTextContent("3");
			if (card.getType().is(CardType.ARTIFACT) || card.getType().is(CardType.LAND))
				tablerow.setTextContent("0");
			cardElem.appendChild(tablerow);

			Element text = doc.createElement("text");
			String textStr = "";
			if (!card.getTranslatedName(true).get().isEmpty())
				textStr = "Nom français : " + card.getTranslatedName().get().replace("Æ", "AE");
			if (card.getAbility() != null)
			{
				if (!textStr.isEmpty())
					textStr += "\n\n";
				textStr += card.getAbility();
			}
			if (card.getFlavor() != null)
			{
				if (!textStr.isEmpty())
					textStr += "\n\n";
				textStr += card.getFlavor();
			}
			text.setTextContent(textStr);
			cardElem.appendChild(text);

			cardsElem.appendChild(cardElem);
		}
		root.appendChild(cardsElem);

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.transform(new DOMSource(doc), new StreamResult(new File("cards.xml")));

		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);

		root = doc.createElement("cockatrice_database");
		doc.appendChild(root);

		cardsElem = doc.createElement("cards");
		root.appendChild(cardsElem);

		List<Token> done = Lists.newArrayList();

		for (Token token : Token.values())
		{
			if (done.contains(token))
				continue;

			Element elem = doc.createElement("card");
			cardsElem.appendChild(elem);

			Element name = doc.createElement("name");
			name.setTextContent(token.getTranslatedName().get());
			elem.appendChild(name);

			List<Token> matching = RefStreams.of(Token.values()).filter(t -> t.getAbility_EN().equals(token.getAbility_EN()) && t.getEnglishName().equals(token.getEnglishName())
					&& t.getPower() == token.getPower() && t.getToughness() == token.getToughness() && Arrays.equals(t.getSubtypes(), token.getSubtypes()) && Arrays.equals(t
					.getColor(), token.getColor()) && t.isLegendary() == token.isLegendary()).collect(Collectors.toList());

			for (Token t : matching)
			{
				done.add(t);
				Element setElem = doc.createElement("set");
				String url = "http://gp.arathia.fr/tokens/fr/" + t.getSet().getCode().toLowerCase() + "/" + t.name().toLowerCase() + ".png";
				javafx.scene.image.Image img = new javafx.scene.image.Image(url);
				if (img.isError())
					url = "http://gp.arathia.fr/tokens/en/" + t.getSet().getCode().toLowerCase() + "/" + t.name().toLowerCase() + ".png";
				setElem.setAttribute("picURL", url);
				setElem.setTextContent(t.getSet().getCode());
				elem.appendChild(setElem);
			}

			for (ManaColor mc : token.getColor())
			{
				Element color = doc.createElement("color");
				color.setTextContent(mc.getAbbreviate());
				elem.appendChild(color);
			}

			Element type = doc.createElement("type");
			String fullType = token.getType().getTranslatedName().get();
			if (token.isLegendary())
				fullType = fullType.replace("-jeton", " légendaire-jeton");
			if (token.getSubtypes().length > 0)
				fullType += " — ";
			for (int i = 0; i < token.getSubtypes().length; ++i)
			{
				String subtype = token.getSubtypes()[i].toString();
				if (token.getType() != CardType.EMBLEM)
					subtype = subtype.toLowerCase();
				fullType += subtype;
				if (i != token.getSubtypes().length - 1)
					fullType += " et ";
			}
			type.setTextContent(fullType);
			elem.appendChild(type);

			if (token.getType().is(CardType.CREATURE))
			{
				Element pt = doc.createElement("pt");
				pt.setTextContent(token.getPower() + "/" + token.getToughness());
				elem.appendChild(pt);
			}

			Element tablerow = doc.createElement("tablerow");
			tablerow.setTextContent("1");
			if (token.getType().is(CardType.CREATURE))
				tablerow.setTextContent("2");
			if (!token.getType().isPermanent())
				tablerow.setTextContent("3");
			if (token.getType().is(CardType.ARTIFACT) || token.getType().is(CardType.LAND))
				tablerow.setTextContent("0");
			elem.appendChild(tablerow);

			Element text = doc.createElement("text");
			text.setTextContent(token.getAbility_FR());
			elem.appendChild(text);

			Element tokenElem = doc.createElement("token");
			tokenElem.setTextContent("1");
			elem.appendChild(tokenElem);
		}

		tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.transform(new DOMSource(doc), new StreamResult(new File("tokens.xml")));

		System.exit(0);*/
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
		}
		catch (ClassNotFoundException e)
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
			}
			catch (IOException ex)
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
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	@SuppressWarnings("unsued")
	private static void cropImages(String setCode) throws IOException, InterruptedException
	{
		File dir = new File(CardRenderer.DIR + "/Pictures/Main", setCode);
		File localDir = new File("pics", setCode);
		//noinspection ConstantConditions
		for (File local : localDir.listFiles())
		{
			File file = new File(dir, local.getName());
			if (file.exists())
				continue;
			System.out.println("Croping " + local.getName().replace(".png", ""));
			BufferedImage img = ImageIO.read(local);
			BufferedImage crop = new BufferedImage(188, 137, BufferedImage.TYPE_INT_RGB);
			crop.createGraphics().drawImage(img.getSubimage(17, 35, 188, 137), 0, 0, Color.BLACK, null);
			ImageIO.write(crop, "JPG", file);
		}
	}

	@SuppressWarnings("unused")
	private static void displayFolderImages(File dir)
	{
		Platform.runLater(() ->
		{
			Stage showerStage = new Stage();
			ImageView view = new ImageView();
			view.setFitWidth(446);
			view.setFitHeight(620);
			showerStage.setScene(new Scene(new Group(view)));
			showerStage.show();

			Stage selector = new Stage();
			selector.setTitle("Images de " + dir.getName());
			FlowPane pane = new FlowPane();
			pane.setHgap(4.0D);
			pane.setVgap(4.0D);
			pane.setPrefWrapLength(920);
			File[] files = dir.listFiles();
			assert files != null;
			Arrays.sort(files, (o1, o2) -> Long.compare(o1.lastModified(), o2.lastModified()));
			for (File file : files)
			{
				ImageView image = null;
				try
				{
					image = new ImageView(new Image(file.toURI().toURL().toString()));
					image.setPreserveRatio(true);
					image.setFitWidth(200);
				}
				catch (MalformedURLException ex)
				{
					ex.printStackTrace();
					return;
				}
				ImageView finalImage = image;
				image.setOnMouseClicked(event -> view.setImage(finalImage.getImage()));
				pane.getChildren().add(image);
			}
			selector.setScene(new Scene(new ScrollPane(pane), 920, 600));
			selector.show();
		});
	}

	@SuppressWarnings("unused")
	private static void generateDraft(int players) throws IOException
	{
		//	StreamSupport.stream(MySQL.getAllCards()).filter(card -> "Lande".equals(card.name.get("fr"))).forEach(card -> card.rarity = Rarity.COMMON);
		for (int i = 0; i < players * 3; ++i)
		{
			BufferedImage img = new BufferedImage(4320, 3060, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			Card[] booster = MySQL.getSet("RIX").generateBooster();
			int x = 0, y = 0;
			for (int j = 0; j < booster.length; ++j)
			{
				Card card = booster[j];
				if (card == null)
					continue;
				BufferedImage subimg = SwingFXUtils.fromFXImage(CardImageManager.getImage(card), null);
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