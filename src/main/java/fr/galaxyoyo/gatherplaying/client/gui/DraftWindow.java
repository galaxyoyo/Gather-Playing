package fr.galaxyoyo.gatherplaying.client.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DraftWindow extends AbstractController
{
	@FXML
	private TableView table;

	@FXML
	private TableColumn name_EN;

	@FXML
	private TableColumn set;

	@FXML
	private TableColumn name_FR;

	@FXML
	private TableColumn manaCost;

	@FXML
	private Label cardsCount;

	public static CardDetailsShower cardShower;

	public static DeckShower deckShower;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}
}
