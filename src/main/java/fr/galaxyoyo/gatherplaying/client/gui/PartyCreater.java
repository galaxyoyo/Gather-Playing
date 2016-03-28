package fr.galaxyoyo.gatherplaying.client.gui;

import com.gluonhq.charm.glisten.control.Dialog;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.Config;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketInSelectDeck;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixUpdatePartyInfos;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PartyCreater extends AbstractController implements Initializable
{
	@FXML
	private TextField name;

	@FXML
	private Spinner<Integer> playerNumber;

	@FXML
	private ComboBox<Rules> rules;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		playerNumber.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 2, 2));

		rules.getItems().addAll(Rules.values());
		rules.getSelectionModel().select(Config.getFormat());
	}

	public void create()
	{
		List<Deck> decks = StreamSupport.stream(Client.localPlayer.decks).filter(deck -> deck.getLegalities().contains(rules.getValue())).collect(Collectors.toList());
		if (decks.isEmpty())
		{
			Utils.alert("Pas de deck", "Aucun deck à jouer", "Vous ne possédez aucun deck légal dans le format " + rules.getValue(), Alert.AlertType.WARNING);
			return;
		}
		Party party = new Party();
		party.setName(name.getText());
		party.setRules(rules.getValue());
		party.setSize(playerNumber.getValue());
		party.addPlayer(Client.localPlayer);
		Client.localPlayer.runningParty = party;
		PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(PacketMixUpdatePartyInfos.class);
		pkt.type = PacketMixUpdatePartyInfos.Type.CREATE;
		pkt.party = party;
		PacketManager.sendPacketToServer(pkt);
		Client.show(GameMenu.class);

		if (Utils.isDesktop())
		{
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
		} else
		{
			Dialog<Deck> deckSelector = new Dialog<>("Sélectionnez votre deck à jouer");
			ComboBox<Deck> box = new ComboBox<>(FXCollections.observableArrayList(Client.localPlayer.decks));
			deckSelector.setContent(box);
			Button play = new Button("Jouer");
			play.setOnAction(event -> {
				deckSelector.setResult(box.getValue());
				deckSelector.hide();
			});
			Button back = new Button("Retour");
			back.setOnAction(event -> {
				deckSelector.hide();
				Client.show(SelectPartyMenu.class);
			});
			deckSelector.getButtons().addAll(play, back);
			deckSelector.showAndWait().ifPresent(deck -> {
				PacketInSelectDeck p = PacketManager.createPacket(PacketInSelectDeck.class);
				p.library = new Library(deck);
				PacketManager.sendPacketToServer(p);
			});
		}
	}
}
