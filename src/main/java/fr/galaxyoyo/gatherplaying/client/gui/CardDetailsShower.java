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
	private ImageView image, doubleFacedImage;

	@FXML
	private Label name_EN, name_TR, type, manaCost, power, set, rarity, nameTRLbl, manaCostLbl, powerLbl;

	@FXML
	private WebView desc;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		DeckEditor.setCardShower(this);
		DraftWindow.setCardShower(this);
		DeckShower.setShower(this);
		nameTRLbl.visibleProperty().bind(name_TR.visibleProperty());
		powerLbl.visibleProperty().bind(power.visibleProperty());
		getParent().prefWidthProperty().bind(Client.getStage().widthProperty().multiply(0.375));

		image.setSmooth(false);
		image.setPreserveRatio(true);
		doubleFacedImage.setSmooth(true);

		if (Utils.isMobile())
		{
			image.setFitWidth(74.0D);
			image.setFitHeight(103.0D);
		}
		else if (Config.getHqCards())
		{
			image.setFitWidth(720.0D / 2.0D);
		//	image.setFitHeight(1024.0D / 3.0D);
		}
	}

	public void updateCard(Card card)
	{
		if (card == null)
			return;
		Executors.newSingleThreadExecutor().submit(() -> Platform.runLater(() -> {
			if (card.getLayout() == Layout.DOUBLE_FACED || card.getLayout() == Layout.MELD)
			{
				int muid = card.getMuId("en") + 1;
				if (card.getLayout() == Layout.MELD)
					//noinspection ConstantConditions
					muid = Layout.MeldPair.getMeldPair(card).getResult().getMuId("en");
				Card doubleFaced = MySQL.getCard(muid);
				doubleFacedImage.setImage(CardImageManager.getImage(doubleFaced));
				if (Utils.isMobile())
				{
					doubleFacedImage.setFitWidth(74.0D);
					doubleFacedImage.setFitHeight(103.0D);
				}
				else if (Config.getHqCards())
				{
					doubleFacedImage.setFitWidth(720.0D / 3.0D);
					doubleFacedImage.setFitHeight(1024.0D / 3.0D);
				}
				else
				{
					doubleFacedImage.setFitWidth(223.0D);
					doubleFacedImage.setFitHeight(310.0D);
				}
			}
			else
			{
				doubleFacedImage.setImage(null);
				doubleFacedImage.setFitWidth(0.0D);
				doubleFacedImage.setFitHeight(0.0D);
			}
			image.setImage(CardImageManager.getImage(card));
		}));
		name_EN.setText(card.getName().get("en"));
		if (!Objects.equals(card.getTranslatedName().get(), card.getName().get("en")))
		{
			name_TR.setVisible(true);
			name_TR.textProperty().bind(card.getTranslatedName());
		}
		else
			name_TR.setVisible(false);
		type.setText(card.getType().toString());
		HBox manas = new HBox();
		if (!card.getType().is(CardType.LAND))
		{
			for (ManaColor color : MoreObjects.firstNonNull(card.getManaCost(), new ManaColor[]{ManaColor.NEUTRAL_0}))
				manas.getChildren().add(new ImageView(CardImageManager.getIcon(color)));
		}
		manaCost.setGraphic(manas);

		if (card.getType().is(CardType.CREATURE))
		{
			power.setVisible(true);
			powerLbl.setText("Force / Endurance :");
			power.setText(card.getPower() + "/" + card.getToughness());
		}
		else if (card.getType().is(CardType.PLANESWALKER))
		{
			power.setVisible(true);
			powerLbl.setText("Loyauté :");
			power.setText(Integer.toString(card.getLoyalty()));
		}
		else
			power.setVisible(false);

		set.setText(card.getSet().getTranslatedName() + " (" + card.getSet().getCode() + ")");
		rarity.setText(card.getRarity().toString());

		String html = "<div style=\"\">";
		if (card.getAbilityMap().get("en") != null)
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

		if (card.getLayout() == Layout.DOUBLE_FACED || card.getLayout() == Layout.MELD)
		{
			int muid = card.getMuId("en") + 1;
			if (card.getLayout() == Layout.MELD)
				//noinspection ConstantConditions
				muid = Layout.MeldPair.getMeldPair(card).getResult().getMuId("en");
			Card doubleFaced = MySQL.getCard(muid);
			html += "<br /><hr /><br /><div style=\"\">";
			if (doubleFaced.getAbilityMap().get("en") != null)
			{
				for (String line : doubleFaced.getAbility().split("£|\n"))
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

			flavor = doubleFaced.getFlavor();
			if (flavor != null)
				html += "<br /><i>" + flavor.replaceAll("#|_", "").replaceAll("\\n|£", "<br/>") + "</i>";
			html += "</div>";
		}

		desc.getEngine().loadContent(html);
	}

	@SuppressWarnings("unchecked")
	@Override
	public VBox getParent()
	{
		return (VBox) super.getParent();
	}
}
