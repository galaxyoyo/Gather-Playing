package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.Phase;
import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixChat;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixSetPhase;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixUpdatePartyInfos;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class GameMenu extends AbstractController implements Initializable
{
	public static GameMenu INSTANCE;
	@FXML
	public HBox battlefield;
	@FXML
	public HBox adverseHand, adverseLands, adversePlayed, adverseCreatures, adverseEnchants, played, creatures, enchants, lands, hand;
	public PlayerInfos playerInfos, adverseInfos;
	@FXML
	public VBox infosPanel;
	@FXML
	private ImageView image;
	@FXML
	private WebView chat;
	private String content;
	@FXML
	private TextField chatBar;
	@FXML
	private VBox phases;

	public static void chat(String msg)
	{
		Platform.runLater(() -> INSTANCE.chat.getEngine().loadContent(INSTANCE.content = INSTANCE.content.replace("</body>", msg + "<br></body>")));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		INSTANCE = this;
		image.setImage(CardImageManager.getImage((Card) null));
		chat.getEngine()
				.loadContent(content = "<!doctype html><html><head><script>function a(){window.scrollTo(0, document.body.scrollHeight)}</script></head><body " + "onload=\"a()" +
									   "\"></body></html>");
		chatBar.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER)
			{
				PacketMixChat pkt = PacketManager.createPacket(PacketMixChat.class);
				pkt.party = Client.getRunningParty();
				pkt.message = Client.localPlayer.name + " : " + chatBar.getText();
				PacketManager.sendPacketToServer(pkt);
				chatBar.clear();
			}
		});

		if (Utils.isMobile())
			((VBox) chat.getParent()).getChildren().removeAll(chat, chatBar);

		for (Phase phase : Phase.values())
		{
			ImageView view = new ImageView();
			view.setOnMouseReleased(event -> {
				if (!Client.getRunningParty().isStarted())
					return;
				PacketMixSetPhase pkt = PacketManager.createPacket(PacketMixSetPhase.class);
				pkt.phase = phase;
				pkt.p = Client.localPlayer;
				PacketManager.sendPacketToServer(pkt);
			});
			phases.getChildren().add(view);
		}
	}

	public void setImage(Card card)
	{
		Platform.runLater(() -> image.setImage(CardImageManager.getImage(card)));
	}

	public void setImage(PlayedCard card)
	{
		Platform.runLater(() -> setImage(CardImageManager.getImage(card)));
	}

	public void setImage(Image image)
	{
		Platform.runLater(() -> this.image.setImage(image));
	}

	public void setPhase(Phase phase)
	{
		Platform.runLater(() -> {
			for (int i = 0; i < phases.getChildren().size(); i++)
			{
				Node child = phases.getChildren().get(i);
				ImageView view = (ImageView) child;
				Image img = new Image(getClass().getResource("/icons/" + Phase.values()[i].name().toLowerCase() + ".png").toString());
				if (phase.ordinal() == i)
					view.setImage(img);
				else
				{
					BufferedImage bimg = new BufferedImage(42, 42, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2D = bimg.createGraphics();
					g2D.drawImage(SwingFXUtils.fromFXImage(img, null), 0, 0, null);
					g2D.setColor(new Color(0, 0, 0, 42));
					g2D.fillRect(0, 0, 42, 42);
					g2D.dispose();
					view.setImage(SwingFXUtils.toFXImage(bimg, null));
				}
			}
			ObservableList<Node> nodes = Client.getStackPane().getChildren();
			nodes.removeAll(StreamSupport.stream(nodes).filter(node -> node instanceof Arrow).collect(Collectors.toList()));
		});
	}

	private VBox pauseMenu;
	@Override
	public void onKeyReleased(KeyEvent event)
	{
		if (event.getCode() == KeyCode.ESCAPE)
		{
			if (pauseMenu != null)
			{
				Client.getStackPane().getChildren().remove(pauseMenu);
				pauseMenu = null;
				return;
			}
			VBox box = new VBox();
			pauseMenu = box;
			box.setStyle("-fx-background-color: rgba(0, 0, 0, 0.42);");
			box.setAlignment(Pos.CENTER);
			box.prefWidthProperty().bind(Client.getStage().widthProperty());
			box.prefHeightProperty().bind(Client.getStage().heightProperty());

			EventHandler<MouseEvent> entered =
					e -> ((Label) e.getSource()).setStyle("-fx-font-size: 42px;\n-fx-border-color: purple;\n-fx-border-width: 3px;\n-fx-border-radius: 10px;\n");
			EventHandler<MouseEvent> exited = e -> ((Label) e.getSource()).setStyle("-fx-font-size: 42px;");

			Label backToGame = new Label("Continuer la partie");
			backToGame.setStyle("-fx-font-size: 42px;");
			backToGame.setAlignment(Pos.CENTER);
			backToGame.setOnMouseEntered(entered);
			backToGame.setOnMouseExited(exited);
			backToGame.setOnMouseReleased(event1 -> {
				Client.getStackPane().getChildren().remove(box);
				pauseMenu = null;
			});
			box.getChildren().add(backToGame);
			VBox.setVgrow(backToGame, Priority.ALWAYS);
			VBox.setMargin(backToGame, new Insets(15.0));

			Label quit = new Label("Quitter la partie");
			quit.setStyle("-fx-font-size: 42px;");
			quit.setAlignment(Pos.CENTER);
			quit.setOnMouseEntered(entered);
			quit.setOnMouseExited(exited);
			quit.setOnMouseReleased(event1 -> {
				PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(PacketMixUpdatePartyInfos.class);
				pkt.type = PacketMixUpdatePartyInfos.Type.LEAVE;
				pkt.party = Client.getRunningParty();
				PacketManager.sendPacketToServer(pkt);

				Client.show(SelectPartyMenu.class);
				pauseMenu = null;
			});
			box.getChildren().add(quit);
			VBox.setVgrow(quit, Priority.ALWAYS);
			VBox.setMargin(quit, new Insets(15.0));

			Client.getStackPane().getChildren().add(box);
		}
	}
}
