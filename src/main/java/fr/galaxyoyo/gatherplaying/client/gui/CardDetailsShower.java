package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.base.MoreObjects;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.Config;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

public class CardDetailsShower extends AbstractController implements Initializable
{
	@FXML
	private VBox box;

	@FXML
	private ImageView image;

	@FXML
	private Label name_EN, name_TR, type, manaCost, power, set, rarity, nameTRLbl, manaCostLbl, powerLbl;

	@FXML
	private WebView desc;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		DeckEditor.cardShower = DeckShower.shower = this;
		nameTRLbl.visibleProperty().bind(name_TR.visibleProperty());
		powerLbl.visibleProperty().bind(power.visibleProperty());
		box.prefWidthProperty().bind(Client.getStage().widthProperty().multiply(0.375));

		if (Utils.isMobile())
		{
			image.setFitWidth(74.0D);
			image.setFitHeight(103.0D);
		}
		else if (Config.getHqCards())
		{
			image.setFitWidth(360.0D);
			image.setFitHeight(510.0D);
		}
	}

	public void updateCard(Card card)
	{
		if (card == null)
			return;
		Executors.newSingleThreadExecutor().submit(() -> Platform.runLater(() -> image.setImage(CardImageManager.getImage(card))));
		name_EN.setText(card.name.get("en"));
		if (!Objects.equals(card.getTranslatedName().get(), card.name.get("en")))
		{
			name_TR.setVisible(true);
			name_TR.textProperty().bind(card.getTranslatedName());
		}
		else
			name_TR.setVisible(false);
		type.setText(card.type.toString());
		HBox manas = new HBox();
		if (!card.type.is(CardType.LAND))
		{
			for (ManaColor color : MoreObjects.firstNonNull(card.manaCost, new ManaColor[] {ManaColor.NEUTRAL_0}))
				manas.getChildren().add(new ImageView(CardImageManager.getIcon(color)));
		}
		manaCost.setGraphic(manas);

		if (card.type.is(CardType.CREATURE))
		{
			power.setVisible(true);
			powerLbl.setText("Force / Endurance :");
			power.setText(card.power + "/" + card.toughness);
		}
		else if (card.type.is(CardType.PLANESWALKER))
		{
			power.setVisible(true);
			powerLbl.setText("Loyauté :");
			power.setText(Integer.toString(card.loyalty));
		}
		else
			power.setVisible(false);

		set.setText(card.set.geName() + " (" + card.set.code + ")");
		rarity.setText(card.rarity.toString());

		String html = "<div style=\"\">";
		if (card.ability.get("en") != null)
		{
			for (String line : card.getAbility().split("£|\n"))
			{
				line = line.replaceAll("#|_", "");
				Matcher m = CardAdapter.MANA_COST.matcher(line);
				while (m.find())
				{
					String icon = m.group().substring(1, m.group().length() - 1).replace("∞", "infinity").replace("/", "");
					CardImageManager.getIcon(icon, true);
					line = line.replace(m.group(), "<img src=\"file:///" + CardImageManager.getIconFile(icon, true).getAbsolutePath().replace('\\', '/') + "\" />");
				}
				html += line.replace("(", "<i>(").replace(")", ")</i>") + "<br />";
			}
		}

		String flavor = card.getFlavor();
		if (flavor != null)
			html += "<br /><i>" + flavor.replaceAll("#|_", "").replaceAll("\\n|£", "<br/>") + "</i>";
		html += "</div>";

		desc.getEngine().loadContent(html);
	}
}
