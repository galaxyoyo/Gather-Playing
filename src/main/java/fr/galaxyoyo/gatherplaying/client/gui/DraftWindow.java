package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketInSelectCard;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DraftWindow extends AbstractController
{
	public static CardDetailsShower cardShower;
	private static DraftWindow instance;
	@FXML
	private FlowPane pane;
	private Map<Card, ImageView> views = Maps.newHashMap();
	private Card selectedCard;

	public static void setCardShower(CardDetailsShower shower)
	{
		DraftWindow.cardShower = shower;
	}

	public static DraftWindow instance()
	{
		return instance;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		instance = this;
	}

	public void showCards(List<Card> booster)
	{
		Platform.runLater(() -> {
			pane.getChildren().clear();
			views.clear();
			selectedCard = null;
			for (Card card : booster)
			{
				Image image = CardImageManager.getImage(card);
				ImageView view = new ImageView(image);
				views.put(card, view);
				view.setCursor(Cursor.HAND);
				view.setOnMouseEntered(event -> cardShower.updateCard(card));
				view.setOnMouseClicked(event ->
				{
					if (selectedCard != null)
					{
						ImageView oldSelected = views.get(selectedCard);
						oldSelected.setStyle("");
					}
					selectedCard = card;
					view.setStyle("-fx-border-width: 2px; -fx-border-color: cyan;");

					PacketInSelectCard pkt = PacketManager.createPacket(PacketInSelectCard.class);
					pkt.selected = selectedCard;
					PacketManager.sendPacketToServer(pkt);
				});
				pane.getChildren().add(view);
			}
		});
	}
}
