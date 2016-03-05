package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixDeck;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DeckShower extends AbstractController implements Initializable
{
	static TableView<Card> table;
	static ReadOnlyObjectProperty<Rules> rulesProp;
	public static CardDetailsShower shower;

	@FXML
	private Button add, sideboard, remove;

	@FXML
	private TextField name;

	@FXML
	private ListView<Object> cards;

	private Deck deck;
	private boolean newDeck;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		DeckEditor.deckShower = this;

		add.setOnAction(event -> add(table.getSelectionModel().getSelectedItem(), false));
		add.setGraphic(new ImageView(getClass().getResource("/icons/add.png").toString()));
		sideboard.setOnAction(event -> add(table.getSelectionModel().getSelectedItem(), true));
		sideboard.setGraphic(new ImageView(getClass().getResource("/icons/add2.png").toString()));
		remove.setOnAction(event -> remove(cards.getSelectionModel().getSelectedItem()));
		remove.setGraphic(new ImageView(getClass().getResource("/icons/remove.png").toString()));

		deck = new Deck();
		deck.free = true;
		deck.uuid = UUID.randomUUID();
		deck.owner = Client.localPlayer;
		deck.name.bind(name.textProperty());
		deck.cards.addListener((SetChangeListener<? super OwnedCard>) change -> updateDeckView());
		deck.sideboard.addListener((SetChangeListener<? super OwnedCard>) change -> updateDeckView());
		newDeck = true;

		cards.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue instanceof CardLabel)
				shower.updateCard(((CardLabel) newValue).card);
			else
				cards.getSelectionModel().select(oldValue);
		});

		rulesProp.addListener((observable, oldValue, newValue) -> {
			deck.calculateLegalities();
			if (!deck.legalities.contains(newValue))
			{
				Utils.alert("Changement de format", "Impossible de changer le filtre de format",
						"Votre deck contient une ou plusieurs cartes illégales dans le format " + "choisi. Merci de les retirer avant d'effectuer le changement.");
				((SelectionModel<Rules>) rulesProp.getBean()).select(oldValue);
			}
		});
	}

	@FXML
	private void load()
	{
		if (Utils.isDesktop())
		{
			ChoiceDialog<Deck> dialog = new ChoiceDialog<>();
			dialog.getItems().addAll(Client.localPlayer.decks);
			if (!Client.localPlayer.decks.isEmpty())
				dialog.setSelectedItem(dialog.getItems().get(0));
			dialog.setTitle("Charger deck");
			dialog.setHeaderText("Sélectionnez un deck à modifier :");
			dialog.initOwner(Client.getStage());
			dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, new ButtonType("Effacer"), ButtonType.CANCEL);
			dialog.setResultConverter(param -> {
				if (param.getText().equals("Effacer"))
				{
					Client.localPlayer.decks.remove(dialog.getSelectedItem());
					PacketMixDeck pkt = PacketManager.createPacket(PacketMixDeck.class);
					pkt.deck = dialog.getSelectedItem();
					pkt.type = PacketMixDeck.Type.DELETING;
					PacketManager.sendPacketToServer(pkt);
					dialog.hide();
				} else if (param == ButtonType.OK)
					return dialog.getSelectedItem();
				return null;
			});
			dialog.showAndWait().filter(deck -> deck != null).ifPresent(d -> {
				newDeck = false;
				deck = d;
				if (deck.name.isBound())
					deck.name.unbind();
				name.setText(deck.name.getValue());
				deck.name.bind(name.textProperty());
				deck.cards.addListener((SetChangeListener<? super OwnedCard>) change -> updateDeckView());
				deck.sideboard.addListener((SetChangeListener<? super OwnedCard>) change -> updateDeckView());
				updateDeckView();
			});
		}
	}

	@FXML
	private void save()
	{
		if (deck.name.getValue().isEmpty())
		{
			Utils.alert("Nom mmanquant", "Veuillez spécifier un nom à votre deck", "Cela vous sera utile afin de dissocier vos decks", Alert.AlertType.WARNING);
			return;
		}

		deck.calculateColors();
		deck.calculateLegalities();
		PacketMixDeck pkt = PacketManager.createPacket(PacketMixDeck.class);
		pkt.deck = deck;
		pkt.type = newDeck ? PacketMixDeck.Type.CREATING : PacketMixDeck.Type.EDITING;
		PacketManager.sendPacketToServer(pkt);
		if (newDeck)
			Client.localPlayer.decks.add(deck);
		newDeck = false;

		Utils.alert("Deck sauvegardé !", "Votre deck a bien été sauvegardé !", "Votre deck a été sauvegardé avec succès dans la base de données du serveur");
	}

	@FXML
	private void importDeck()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Importer deck");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Decks Cockatrice", "*.cod"));
		File file = chooser.showOpenDialog(Client.getStage());
		if (file == null)
			return;
		try
		{
			deck = new Deck();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			Element root = doc.getDocumentElement();
			if (root.getAttribute("version").equals("1"))
			{
				for (int i = 0; i < root.getChildNodes().getLength(); ++i)
				{
					Node node = root.getChildNodes().item(i);
					if (node.getNodeName().equals("deckname"))
						deck.name.set(node.getTextContent());
					else if (node.getNodeName().equals("comments"))
						deck.desc = node.getTextContent();
					else if (node.getNodeName().equals("zone"))
					{
						Element element = (Element) node;
						if (element.getAttribute("name").equals("main"))
						{
							for (int j = 0; j < element.getElementsByTagName("card").getLength(); ++j)
							{
								Element cardElem = (Element) element.getElementsByTagName("card").item(j);
								List<Card> matches = StreamSupport.stream(MySQL.getAllCards()).filter(card -> card.name.get("en").equals(cardElem.getAttribute("name")))
										.collect(Collectors.toList());
								Collections.sort(matches);
								for (int k = 0; k < Integer.parseInt(cardElem.getAttribute("number")); ++k)
									deck.cards.add(new OwnedCard(matches.get(0), Client.localPlayer, false));
							}
						} else if (element.getAttribute("name").equals("side"))
						{
							for (int j = 0; j < element.getElementsByTagName("card").getLength(); ++j)
							{
								Element cardElem = (Element) element.getElementsByTagName("card").item(j);
								List<Card> matches = StreamSupport.stream(MySQL.getAllCards()).filter(card -> card.name.get("en").equals(cardElem.getAttribute("name")))
										.collect(Collectors.toList());
								Collections.sort(matches);
								Collections.reverse(matches);
								for (int k = 0; k < Integer.parseInt(cardElem.getAttribute("number")); ++k)
									deck.sideboard.add(new OwnedCard(matches.get(0), Client.localPlayer, false));
							}
						}
					}
				}
			}
			newDeck = true;
			name.setText(deck.name.getValue());
			deck.uuid = UUID.randomUUID();
			deck.owner = Client.localPlayer;
			deck.name.bind(name.textProperty());
			deck.cards.addListener((SetChangeListener<? super OwnedCard>) change -> updateDeckView());
			deck.sideboard.addListener((SetChangeListener<? super OwnedCard>) change -> updateDeckView());
			updateDeckView();
		} catch (SAXException | IOException | ParserConfigurationException ex)
		{
			ex.printStackTrace();
		}
	}

	@FXML
	private void exportDeck()
	{
		try
		{
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Exporter deck");
			FileChooser.ExtensionFilter codFilter = new FileChooser.ExtensionFilter("Decks Cockatrice", "*.cod");
			FileChooser.ExtensionFilter tableTopSimFilter = new FileChooser.ExtensionFilter("Spoiler Tabletop Simulator", "*.jpg", "*.jpeg");
			chooser.getExtensionFilters().addAll(codFilter, tableTopSimFilter);
			File file = chooser.showSaveDialog(Client.getStage());
			if (file == null)
				return;

			if (chooser.getSelectedExtensionFilter() == codFilter)
			{
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				doc.setXmlVersion("1.0");
				Element root = doc.createElement("cockatrice_deck");
				root.setAttribute("version", "1");
				doc.appendChild(root);
				Element deckname = doc.createElement("deckname");
				deckname.setTextContent(deck.name.get());
				root.appendChild(deckname);
				Element comments = doc.createElement("comments");
				comments.setTextContent(deck.desc);
				root.appendChild(comments);
				Element mainZone = doc.createElement("zone");
				mainZone.setAttribute("name", "main");
				root.appendChild(mainZone);
				for (Map.Entry<String, Integer> entry : deck.stackedCardsByName().entrySet())
				{
					Element card = doc.createElement("card");
					card.setAttribute("number", Integer.toString(entry.getValue()));
					card.setAttribute("price", "0");
					card.setAttribute("name", entry.getKey());
					mainZone.appendChild(card);
				}
				if (!deck.sideboard.isEmpty())
				{
					Element sideZone = doc.createElement("zone");
					sideZone.setAttribute("name", "side");
					root.appendChild(sideZone);
					for (Map.Entry<String, Integer> entry : deck.stackedSideboardByName().entrySet())
					{
						Element card = doc.createElement("card");
						card.setAttribute("number", Integer.toString(entry.getValue()));
						card.setAttribute("price", "0");
						card.setAttribute("name", entry.getKey());
						sideZone.appendChild(card);
					}
				}
				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(file));
			} else if (chooser.getSelectedExtensionFilter() == tableTopSimFilter)
			{
				int numImages = (int) Math.ceil(deck.cards.size() / 60.0D);
				for (int i = 0; i < numImages; ++i)
				{
					BufferedImage img = new BufferedImage(10 * 223, 7 * 310, BufferedImage.TYPE_INT_RGB);
					Graphics2D g = img.createGraphics();
					int x = 0, y = 0;
					List<OwnedCard> cards = Lists.newArrayList(deck.cards);
					for (OwnedCard card : cards.subList(i * 70, Math.min(cards.size(), (i + 1) * 70)))
					{
						BufferedImage preview = SwingFXUtils.fromFXImage(CardImageManager.getImage(card.getCard()), null);
						g.drawImage(preview, x, y, 223, 310, null);
						x += 223;
						if (x >= img.getWidth())
						{
							x = 0;
							y += 310;
						}
					}
					g.dispose();
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(img, "JPG", os);
					URL url = new URL("http://gatherplaying.arathia.fr/tts/uploadimage.php");
					HttpURLConnection co = (HttpURLConnection) url.openConnection();
					co.setRequestMethod("POST");
					co.setDoOutput(true);
					co.getOutputStream().write(os.toByteArray());
					String code = new String(IOUtils.toByteArray(co));
					String imgUrl = "http://gatherplaying.arathia.fr/tts/" + code + ".jpg";
					System.out.println(imgUrl);
					StringSelection clipboard = new StringSelection(imgUrl);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboard, clipboard);
					Utils.alert("Image copiée !", "Image uploadée avec succès", "Le spoiler de l'image est disponible à l'adresse " + imgUrl + ", copiée dans votre " +
							"presse-papiers" + (numImages == 1 ? "" : " (" + (i + 1) + "/" + numImages + ")") + "\nVoulez-vous" +
							" enregistrer l'image malgré tout ?", Alert.AlertType.CONFIRMATION).filter(buttonType -> buttonType == ButtonType.OK).ifPresent(buttonType -> {
						try
						{
							ImageIO.write(img, "JPG", file);
						} catch (IOException e)
						{
							e.printStackTrace();
						}
					});
				}
			}
		} catch (ParserConfigurationException | TransformerException | IOException ex)
		{
			ex.printStackTrace();
		}
	}

	@FXML
	private void quit()
	{
		Client.show(MainMenu.class);
		//Client.show(FreeModeMenu.class);
	}

	private void add(Card card, boolean sideboard)
	{
		if (card == null)
			return;

		Rules rules = rulesProp.getValue();
		if (rules != Rules.FREEFORM)
		{
			if (sideboard && deck.sideboard.size() >= 15)
				return;
			int count = 0;
			for (OwnedCard c : deck.cards)
			{
				if (c.getCard().name.get("en").equals(card.name.get("en")))
					++count;
			}
			for (OwnedCard c : deck.sideboard)
			{
				if (c.getCard().name.get("en").equals(card.name.get("en")))
					++count;
			}

			if (card.isRestricted(rules) && count >= 1)
				return;
			else if (count >= 4 && !card.basic)
				return;
		}

		if (sideboard)
			deck.sideboard.add(new OwnedCard(card, Client.localPlayer, false));
		else
			deck.cards.add(new OwnedCard(card, Client.localPlayer, false));
	}

	private void remove(Object obj)
	{
		if (obj == null)
			return;
		int index = cards.getSelectionModel().getSelectedIndex();
		CardLabel label = (CardLabel) obj;
		if (label.sideboard)
		{
			for (OwnedCard card : Sets.newHashSet(deck.sideboard))
			{
				if (card.getCard() == label.card)
				{
					deck.sideboard.remove(card);
					break;
				}
			}
		} else
		{
			for (OwnedCard card : Sets.newHashSet(deck.cards))
			{
				if (card.getCard() == label.card)
				{
					deck.cards.remove(card);
					break;
				}
			}
		}

		updateDeckView();
		if (label.count - 1 > 0)
			cards.getSelectionModel().select(index);
	}

	private void updateDeckView()
	{
		cards.getItems().clear();
		Map<CardType, Map<Card, AtomicInteger>> map = deck.cardsByType();
		if (!map.isEmpty())
		{
			Label deck = new Label("Deck (" + this.deck.cards.size() + ")");
			deck.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-font-size: 14pt;");
			cards.getItems().add(deck);
			for (Map.Entry<CardType, Map<Card, AtomicInteger>> entry : map.entrySet())
			{
				AtomicInteger size = new AtomicInteger(0);
				Label label = new Label();
				label.setStyle("-fx-font-weight: bold; -fx-font-size: 12pt;");
				cards.getItems().add(label);
				for (Map.Entry<Card, AtomicInteger> entry2 : entry.getValue().entrySet())
				{
					size.getAndAdd(entry2.getValue().get());
					cards.getItems().add(new CardLabel(entry2.getKey(), entry2.getValue().get(), false));
				}
				label.setText(entry.getKey().toString() + (size.get() > 1 ? " (" + size.get() + ")" : ""));
			}
		}
		map = deck.sideboardByType();
		if (!map.isEmpty())
		{
			Label sideboard = new Label("Réserve (" + this.deck.sideboard.size() + ")");
			sideboard.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-font-size: 14pt;");
			cards.getItems().add(sideboard);
			for (Map.Entry<CardType, Map<Card, AtomicInteger>> entry : map.entrySet())
			{
				AtomicInteger size = new AtomicInteger(0);
				Label label = new Label();
				label.setStyle("-fx-font-weight: bold; -fx-font-size: 12pt;");
				cards.getItems().add(label);
				for (Map.Entry<Card, AtomicInteger> entry2 : entry.getValue().entrySet())
				{
					size.addAndGet(entry2.getValue().get());
					cards.getItems().add(new CardLabel(entry2.getKey(), entry2.getValue().get(), true));
				}
				label.setText(entry.getKey().toString() + (size.get() > 1 ? " (" + size.get() + ")" : ""));
			}
		}
	}

	@FXML
	private void showstats()
	{
		try
		{
			Dialog<ButtonType> dialog = new Dialog();
			dialog.setTitle("Statistiques");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DeckStats.fxml"));
			dialog.getDialogPane().setContent(loader.load());
			((DeckStats) loader.getController()).setDeck(deck);
			dialog.showAndWait();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void onKeyReleased(KeyEvent event)
	{
		if (event.getCode() == KeyCode.ADD)
			add.fire();
		else if (event.getCode() == KeyCode.SUBTRACT)
			remove.fire();
	}

	private class CardLabel extends Label
	{
		private final Card card;
		private final int count;
		private final boolean sideboard;

		CardLabel(Card card, int count, boolean sideboard)
		{
			textProperty().bind(card.getTranslatedName().concat((count > 1 ? " (" + count + ")" : "")));
			this.card = card;
			this.count = count;
			this.sideboard = sideboard;
		}
	}
}
