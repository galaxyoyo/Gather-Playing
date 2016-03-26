package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.Deck;
import fr.galaxyoyo.gatherplaying.Library;
import fr.galaxyoyo.gatherplaying.Party;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketInSelectDeck;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixUpdatePartyInfos;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SelectPartyMenu extends AbstractController implements Initializable
{
	private static final ObservableList<Party> PARTIES = FXCollections.observableArrayList();

	@FXML
	private TableColumn<Party, String> name, format, players;

	@FXML
	private TableView<Party> parties;

	public static ObservableList<Party> getParties()
	{
		return PARTIES;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(PacketMixUpdatePartyInfos.class);
		pkt.type = PacketMixUpdatePartyInfos.Type.GET;
		PacketManager.sendPacketToServer(pkt);

		name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
		format.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRules().toString()));
		players.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getOnlinePlayers().size() + " / " + param.getValue().getSize()));

		parties.setItems(PARTIES);
	}

	@FXML
	private void create()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PartyCreater.fxml"));
			Parent p = loader.load();
			PartyCreater creater = loader.getController();
			HBox box = new HBox(p);
			HBox.setHgrow(p, Priority.ALWAYS);
			if (Utils.isDesktop())
			{
				Dialog<ButtonType> dialog = new Dialog<>();
				dialog.setTitle("Création d'une partie");
				dialog.setHeaderText("Paramètres de la partie");
				dialog.getDialogPane().setContent(box);
				ButtonType create = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
				dialog.getDialogPane().getButtonTypes().addAll(create, ButtonType.CANCEL);
				dialog.initOwner(Client.getStage());
				dialog.showAndWait().filter(buttonType -> buttonType == create).ifPresent(buttonType -> creater.create());
			}
			else
			{
				com.gluonhq.charm.glisten.control.Dialog<Button> dialog = new com.gluonhq.charm.glisten.control.Dialog<>("Paramètres de la partie :");
				Button create = new Button("Créer");
				create.setOnAction(event -> {
					dialog.setResult(create);
					dialog.hide();
				});
				Button cancel = new Button("Annuler");
				cancel.setOnAction(event -> dialog.hide());
				dialog.getButtons().addAll(create, cancel);
				dialog.setContent(p);
				dialog.showAndWait().filter(button -> button == create).ifPresent(button -> creater.create());
			}

		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	@FXML
	private void join()
	{
		Party party = parties.getSelectionModel().getSelectedItem();
		if (party == null)
			return;
		List<Deck> decks = StreamSupport.stream(Client.localPlayer.decks).filter(deck -> deck.getLegalities().contains(Client.getRunningParty().getRules())).collect(Collectors.toList());
		if (decks.isEmpty())
		{
			Utils.alert("Pas de deck", "Aucun deck à jouer", "Vous ne possédez aucun deck légal dans le format " + party.getRules(), Alert.AlertType.WARNING);
			return;
		}
		Client.localPlayer.runningParty = party;
		PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(PacketMixUpdatePartyInfos.class);
		pkt.party = party;
		pkt.type = PacketMixUpdatePartyInfos.Type.JOIN;
		PacketManager.sendPacketToServer(pkt);
		Client.show(GameMenu.class);
		ChoiceDialog<Deck> deckSelector = new ChoiceDialog<>();
		deckSelector.setTitle("Sélecteur de deck");
		deckSelector.setHeaderText("Sélectionnez votre deck à jouer");
		deckSelector.getItems().setAll(decks);
		deckSelector.setSelectedItem(deckSelector.getItems().get(0));
		deckSelector.showAndWait().ifPresent(deck -> {
			PacketInSelectDeck p = PacketManager.createPacket(PacketInSelectDeck.class);
			p.library = new Library(deck);
			PacketManager.sendPacketToServer(p);
		});
	}

	@FXML
	private void back()
	{
		Client.show(MainMenu.class);
		//Client.show(FreeModeMenu.class);
	}
}
