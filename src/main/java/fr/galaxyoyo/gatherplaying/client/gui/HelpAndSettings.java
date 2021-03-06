package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.Rules;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.Config;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HelpAndSettings extends AbstractController
{
	@FXML
	private Label localeLbl;

	@FXML
	private ChoiceBox<Locale> locale;

	@FXML
	private Label formatLbl;

	@FXML
	private ChoiceBox<Rules> preferredFormat;

	@FXML
	private CheckBox stayLogged, hqCards, stackCards;

	@FXML
	private Button save, cancel;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		locale.getItems().addAll(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH, Locale.ITALIAN, new Locale("es"), new Locale("pt", "BR"), new Locale("ru"), Locale.PRC,
				Locale.TAIWAN, Locale.JAPANESE, Locale.KOREAN);
		locale.setValue(Config.getLocale());
		preferredFormat.getItems().setAll(Arrays.stream(Rules.values()).filter(rules -> !rules.isLimited()).collect(Collectors.toList()));
		preferredFormat.setValue(Config.getFormat());
		stayLogged.setSelected(Config.getStayLogged());
		stackCards.setSelected(Config.getStackCards());

		((GridPane) getParent()).maxWidthProperty().bind(Client.getStage().widthProperty().divide(2));
		((GridPane) getParent()).maxHeightProperty().bind(Client.getStage().heightProperty().divide(2));
	}

	@FXML
	private void save()
	{
		if (!Config.getLocale().equals(locale.getValue()))
			Utils.alert("Changement de langue", "Redémarrage nécessaire", "Un redémarrage de l'application est nécessaire pour l'actualisation de certains textes dans votre " +
					"langue, comme les boutons des boîtes de dialogue");
		Config.localeProperty().set(locale.getValue());
		Config.formatProperty().set(preferredFormat.getValue());
		Config.stayLoggedProperty().set(stayLogged.isSelected());
		Config.stackCardsProperty().set(stackCards.isSelected());
		Client.show(MainMenu.class);
	}

	@FXML
	private void cancel()
	{
		Client.show(MainMenu.class);
	}
}
