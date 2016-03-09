package fr.galaxyoyo.gatherplaying.client;

import com.gluonhq.charm.glisten.application.GlassPane;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.Swatch;
import fr.galaxyoyo.gatherplaying.Party;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.gui.AbstractController;
import fr.galaxyoyo.gatherplaying.client.gui.Loading;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixUpdatePartyInfos;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client extends Application
{
	public static Player localPlayer;
	private static Stage stage;
	private static AbstractController currentController;

	public static StackPane getStackPane()
	{
		if (Utils.isMobile())
			return MobileClient.getStackPane();
		return (StackPane) getStage().getScene().getRoot();
	}

	public static Stage getStage()
	{
		return stage;
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		Client.stage = stage;
		stage.setTitle("Gather Playing");
		stage.getIcons().add(new Image(getClass().getResource("/icons/icon.png").toString()));
		stage.setMaximized(true);
		stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		stage.setOnCloseRequest(event -> {
			event.consume();
			//noinspection ConstantConditions
			Utils.alert("Êtes-vous sûr ?", "Vous vous apprêtez à quitter le jeu", "Êtes-vous sûr de vraiment vouloir quitter ?", Alert.AlertType.CONFIRMATION)
					.filter(buttonType -> buttonType == ButtonType.OK).ifPresent(buttonType -> close());
		});
		show(Loading.class);
		stage.getScene().setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.F11)
				stage.setFullScreen(!stage.isFullScreen());
			else
				currentController.onKeyReleased(event);
		});
		stage.show();
		stage.toFront();
	}

	public static void close()
	{
		if (localPlayer != null && getRunningParty() != null)
		{
			PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(PacketMixUpdatePartyInfos.class);
			pkt.type = PacketMixUpdatePartyInfos.Type.LEAVE;
			PacketManager.sendPacketToServer(pkt);
		}
		stage.close();
		if (localPlayer != null)
			localPlayer.connection.close();
		Utils.getPlatform().finish();
		System.exit(0);
	}

	public static <T extends AbstractController> T show(Class<T> clazz)
	{
		if (Utils.isMobile())
		{
			MobileClient.show(clazz);
			return null;
		}

		try
		{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(clazz.getResource("/views/" + clazz.getSimpleName() + ".fxml"));
			Parent parent = loader.load();
			VBox box = new VBox(parent);
			box.setAlignment(Pos.CENTER);
			VBox.setVgrow(parent, Priority.ALWAYS);
			StackPane pane = new StackPane(box);
			pane.prefWidthProperty().bind(getStage().widthProperty());
			pane.prefHeightProperty().bind(getStage().heightProperty());
			pane.setAlignment(Pos.CENTER);
			if (stage.getScene() == null)
			{
				Scene scene = new Scene(pane);
				scene.getStylesheets().add("/default.css");
				stage.setScene(scene);
			} else
				stage.getScene().setRoot(pane);
			return (T) (currentController = loader.getController());
		} catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	public static Party getRunningParty() { return localPlayer.runningParty; }

	public static class MobileClient extends MobileApplication
	{
		private static StackPane getStackPane() { return (StackPane) getInstance().getView().getCenter(); }

		@Override
		public void postInit(Scene scene)
		{
			Swatch.GREY.assignTo(scene);
			scene.getStylesheets().add("/default.css");
			((GlassPane) scene.getRoot()).getChildren().remove(getInstance().getAppBar());
			Client.stage = (Stage) scene.getWindow();
			String[] classes =
					{"CardDetailsShower", "DeckEditor", "DeckEditorFilter", "DeckShower", "FreeModeMenu", "GameMenu", "Loading", "LoginDialog", "MainMenu", "PartyCreater",
							"PlayerInfos", "SelectPartyMenu"};
			for (String className : classes)
			{
				addViewFactory(className, () -> {
					try
					{
						View v = new View(className);
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("/views/" + className + ".fxml"));
						Parent parent = null;
						parent = loader.load();
						currentController = loader.getController();
						VBox box = new VBox(parent);
						box.setAlignment(Pos.CENTER);
						VBox.setVgrow(parent, Priority.ALWAYS);
						StackPane pane = new StackPane(box);
						pane.prefWidthProperty().bind(stage.widthProperty());
						pane.prefHeightProperty().bind(stage.widthProperty());
						pane.setAlignment(Pos.CENTER);
						v.setCenter(pane);
						return v;
					} catch (Throwable t)
					{
						t.printStackTrace();
						return new View(className);
					}
				});
			}
			show(Loading.class);
		}

		public static <T extends AbstractController> void show(Class<T> clazz)
		{
			getInstance().switchView(clazz.getSimpleName());
		}
	}
}