package fr.galaxyoyo.gatherplaying.client.gui;

import com.gluonhq.charm.glisten.control.Dialog;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketInSelectDeck;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixUpdatePartyInfos;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
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
		rules.getSelectionModel().select(Rules.LEGACY);
	}

	public void create()
	{
		Party party = new Party();
		party.name = name.getText();
		party.rules = rules.getValue();
		party.size = playerNumber.getValue();
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
			deckSelector.getItems().setAll(Client.localPlayer.decks);
			deckSelector.setSelectedItem(deckSelector.getItems().get(0));
			deckSelector.showAndWait().ifPresent(deck -> {
				PacketInSelectDeck p = PacketManager.createPacket(PacketInSelectDeck.class);
				p.library = new Library(deck);
				PacketManager.sendPacketToServer(p);
			});
		}
		else
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
