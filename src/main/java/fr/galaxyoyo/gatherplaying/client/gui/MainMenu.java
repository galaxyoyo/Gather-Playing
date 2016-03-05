package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMenu extends AbstractController
{
	@FXML
	private Label play, /*freeMode, */settings, deckEditor, quit;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		EventHandler<MouseEvent> entered = e -> ((Label) e.getSource()).setStyle("-fx-border-color: purple;\n-fx-border-width: 3px;\n-fx-border-radius: 10px;\n");
		EventHandler<MouseEvent> exited = e -> ((Label) e.getSource()).setStyle("");
		play.setOnMouseEntered(entered);
		play.setOnMouseExited(exited);
		//freeMode.setOnMouseEntered(entered);
		//freeMode.setOnMouseExited(exited);
		deckEditor.setOnMouseEntered(entered);
		deckEditor.setOnMouseExited(exited);
		settings.setOnMouseEntered(entered);
		settings.setOnMouseExited(exited);
		quit.setOnMouseEntered(entered);
		quit.setOnMouseExited(exited);
		if (Utils.isMobile())
			quit.setText("Retour à l'écran d'accueil");

		play.setOnMouseReleased(e -> Client.show(SelectPartyMenu.class));
		//freeMode.setOnMouseReleased(e -> Client.show(FreeModeMenu.class));
		deckEditor.setOnMouseReleased(e -> Client.show(DeckEditor.class));
		settings.setOnMouseReleased(e -> Client.show(HelpAndSettings.class));

		quit.setOnMouseReleased(e -> {
			if (Utils.isDesktop())
			{
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Êtes-vous sûr ?");
				alert.setHeaderText("Vous vous apprêtez à fermer le jeu");
				alert.setContentText("Êtes-vous sûr de vouloir quitter ?");
				alert.showAndWait().ifPresent(button -> {
					if (button == ButtonType.OK)
						Client.close();
				});
			}
			else
			{
				com.gluonhq.charm.glisten.control.Alert alert = new com.gluonhq.charm.glisten.control.Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitleText("Vous vous apprêtez à fermer le jeu");
				alert.setContentText("Êtes-vous sûr de vouloir quitter ?");
				alert.showAndWait().filter(o -> o == ButtonType.OK).ifPresent(button -> Client.close());
			}
		});
	}
}
