package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class Loading extends AbstractController implements Initializable
{
	private static Loading INSTANCE;

	@FXML
	private Label label;

	public static void setLabel(String text)
	{
		if (Utils.getSide() == Side.CLIENT && INSTANCE != null)
			Platform.runLater(() -> INSTANCE.label.setText(text));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		INSTANCE = this;
	}
}
