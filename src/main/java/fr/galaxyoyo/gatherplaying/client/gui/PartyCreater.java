package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.Config;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketInSelectDeck;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixUpdatePartyInfos;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Comparator;
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
		if (rules.getValue().isLimited())
		{
			List<Set> withBoosters = StreamSupport.stream(MySQL.getAllSets()).filter(set -> set.booster != null && set.booster.length > 0).collect(Collectors.toList());
			withBoosters.sort(((Comparator<Set>) Set::compareTo).reversed());
			List<Set> chosen = Lists.newArrayList(null, null, null, null, null, null);
			Dialog<Object> dialog = new Dialog<>();
			dialog.setTitle("Paramètres de la partie limitée");
			dialog.setHeaderText("Sélectionnez les boosters à jouer");
			GridPane pane = new GridPane();
			for (int i = 0; i < (rules.getValue() == Rules.DRAFT ? 3 : 6); ++i)
			{
				Label lbl = new Label("Booster " + (i + 1));
				pane.getChildren().add(lbl);
				GridPane.setColumnIndex(lbl, i);
				ChoiceBox<Set> booster = new ChoiceBox<>();
				booster.setConverter(new StringConverter<Set>()
				{
					@Override
					public String toString(Set object)
					{
						return object.getCode();
					}

					@Override
					public Set fromString(String string)
					{
						return MySQL.getSet(string);
					}
				});
				booster.getItems().setAll(withBoosters);
				int finalI = i;
				booster.valueProperty().addListener((observable, oldValue, newValue) -> chosen.set(finalI, newValue));
				booster.setValue(withBoosters.get(0));
				pane.getChildren().add(booster);
				GridPane.setConstraints(booster, i, 1);
			}
			dialog.getDialogPane().setContent(pane);
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
			dialog.showAndWait().ifPresent(o -> {
				Party party = new Party();
				party.setName(name.getText());
				party.setRules(rules.getValue());
				party.setSize(playerNumber.getValue());
				party.addPlayer(Client.localPlayer);
				Client.localPlayer.runningParty = party;
				PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(PacketMixUpdatePartyInfos.class);
				pkt.type = PacketMixUpdatePartyInfos.Type.CREATE;
				pkt.party = party;
				pkt.boosters = chosen;
				PacketManager.sendPacketToServer(pkt);
				if (rules.getValue() == Rules.SEALED)
				{
					DeckEditor editor = Client.show(DeckEditor.class);
					assert editor != null;
					DeckShower shower = DeckEditor.getDeckShower();
					shower.initForLimited();
					//noinspection unchecked
					((SortedList<Card>) ((FilteredList<Card>) ((SortedList<Card>) DeckEditor.getEditor().table.getItems()).getSource()).getSource()).getSource().clear();
				}
			});
		}
		else
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
			}
			else
			{
				com.gluonhq.charm.glisten.control.Dialog<Deck> deckSelector = new com.gluonhq.charm.glisten.control.Dialog<>("Sélectionnez votre deck à jouer");
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
}
