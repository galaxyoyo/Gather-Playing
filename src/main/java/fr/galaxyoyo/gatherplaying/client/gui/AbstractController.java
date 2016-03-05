package fr.galaxyoyo.gatherplaying.client.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;

public abstract class AbstractController implements Initializable
{
	@FXML
	private Parent parent;

	public <T extends Parent> T getParent()
	{
		return (T) parent;
	}

	public void onKeyReleased(KeyEvent event) {}
}
