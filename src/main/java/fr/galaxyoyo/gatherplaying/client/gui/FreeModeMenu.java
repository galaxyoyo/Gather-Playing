package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class FreeModeMenu extends AbstractController implements Initializable
{
	@FXML
	private Label play, giant, god, conquest, deckEditor, back;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		EventHandler<MouseEvent> entered =  e -> ((Label) e.getSource()).setStyle("-fx-border-color: purple;\n-fx-border-width: 3px;\n-fx-border-radius: 10px;\n");
		EventHandler<MouseEvent> exited =  e -> ((Label) e.getSource()).setStyle("");
		play.setOnMouseEntered(entered);
		play.setOnMouseExited(exited);
		giant.setOnMouseEntered(entered);
		giant.setOnMouseExited(exited);
		god.setOnMouseEntered(entered);
		god.setOnMouseExited(exited);
		conquest.setOnMouseEntered(entered);
		conquest.setOnMouseExited(exited);
		deckEditor.setOnMouseEntered(entered);
		deckEditor.setOnMouseExited(exited);
		back.setOnMouseEntered(entered);
		back.setOnMouseExited(exited);

		play.setOnMouseReleased(e -> Client.show(SelectPartyMenu.class));
		deckEditor.setOnMouseReleased(e -> {
			if (Utils.isMobile())
				Utils.alert("Êtes-vous sûr ?", "Ne serait-il pas selon vous plus agréable de faire vos decks sur votre ordinateur ? Vous y gagnerez en confort et en ergonomie !",
						"", Alert.AlertType.CONFIRMATION).ifPresent(buttonType -> Client.show(DeckEditor.class));
			else
				Client.show(DeckEditor.class);
		});
		back.setOnMouseReleased(e -> Client.show(MainMenu.class));
	}
}
