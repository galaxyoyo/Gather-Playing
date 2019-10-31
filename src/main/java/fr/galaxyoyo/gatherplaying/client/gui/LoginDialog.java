package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.MySQL;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.Config;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketInConnect;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketOutConnectResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginDialog extends AbstractController implements Initializable
{
	private static LoginDialog INSTANCE;

	@FXML
	private TextField username;

	@FXML
	private TextField email;

	@FXML
	private PasswordField password;

	@FXML
	private PasswordField confirmPassword;

	@FXML
	private RadioButton login;

	@FXML
	private RadioButton register;

	@FXML
	private Label usernameLabel, emailLabel, cfPswdLabel;

	@FXML
	private CheckBox stayLogged;

	@FXML
	private Button submit;

	public static void connectResponse(PacketOutConnectResponse resp)
	{
		Platform.runLater(() -> {
			if (resp.isErrored())
			{
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Erreur de connexion");
				alert.setHeaderText("Erreur lors de la connexion au serveur");
				alert.setContentText(resp.getErrorMessage());
				alert.showAndWait();
				INSTANCE.email.setEditable(true);
				return;
			}
			Config.stayLoggedProperty().unbind();
			MySQL.setConfig("lastuser", INSTANCE.email.getText());
			MySQL.setConfig("lastpassword", INSTANCE.password.getText());
			Client.show(MainMenu.class);
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		INSTANCE = this;
		BorderPane parent = getParent();
		parent.maxWidthProperty().bind(Client.getStage().getScene().widthProperty().divide(2));
		parent.maxHeightProperty().bind(Client.getStage().getScene().heightProperty().divide(2));

		ToggleGroup group = new ToggleGroup();
		register.setToggleGroup(group);
		emailLabel.prefWidthProperty().bind(email.prefWidthProperty());
		submit.prefWidthProperty().bind(parent.prefWidthProperty());
		email.prefWidthProperty().bind(Client.getStage().widthProperty().divide(4));
		usernameLabel.visibleProperty().bind(username.visibleProperty());
		confirmPassword.visibleProperty().bind(username.visibleProperty());
		username.editableProperty().bind(email.visibleProperty());
		password.editableProperty().bind(email.visibleProperty());
		cfPswdLabel.visibleProperty().bind(username.visibleProperty());
		confirmPassword.editableProperty().bind(username.visibleProperty());
		register.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue)
			{
				username.setVisible(false);
				emailLabel.setText("Email / Pseudo :");
				submit.setText("Se connecter");
			} else
			{
				username.setVisible(true);
				emailLabel.setText("Email :");
				submit.setText("S'inscrire");
			}
		});
		login.setToggleGroup(group);
		login.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue)
			{
				username.setVisible(false);
				emailLabel.setText("Email / Pseudo :");
				submit.setText("Se connecter");
			} else
			{
				username.setVisible(true);
				emailLabel.setText("Email :");
				submit.setText("S'inscrire");
			}
		});

		stayLogged.setSelected(MySQL.getBooleanConfig("stay-logged", false));
		Config.stayLoggedProperty().bind(stayLogged.selectedProperty());

		if (Config.getStayLogged() && MySQL.getConfig("lastuser", null) != null && MySQL.getConfig("lastpassword", null) != null)
		{
			email.setText(MySQL.getConfig("lastuser", null));
			password.setText(MySQL.getConfig("lastpassword", null));
			submit();
		}
	}

	@FXML
	private void submit()
	{
		while (true)
		{
			if (Client.localPlayer != null)
				break;
		}
		email.setEditable(false);

		Client.localPlayer.name = username.getText();
		Client.localPlayer.email = email.getText();
		Client.localPlayer.sha1Pwd = Utils.toSHA1(password.getText());

		PacketInConnect pkt = PacketManager.createPacket(PacketInConnect.class);
		pkt.type = login.selectedProperty().get() ? PacketInConnect.Type.LOGGING : PacketInConnect.Type.REGISTERING;
		PacketManager.sendPacketToServer(pkt);
	}

	@Override
	public void onKeyReleased(KeyEvent event)
	{
		if (event.getCode() == KeyCode.ENTER)
		{
			if (stayLogged.isFocused())
				stayLogged.setSelected(!stayLogged.isSelected());
			else if (login.isFocused())
				login.setSelected(true);
			else if (register.isFocused())
				register.setSelected(true);
			else
				submit();
		}
	}
}
