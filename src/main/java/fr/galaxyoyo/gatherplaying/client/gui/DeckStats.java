package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.*;
import java8.util.stream.StreamSupport;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class DeckStats extends AbstractController
{
	@FXML
	private PieChart colors, types, rarity, cmc;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	public void setDeck(Deck deck)
	{
		ManaColor[] allColors = {ManaColor.RED, ManaColor.BLACK, ManaColor.GREEN, ManaColor.BLUE, ManaColor.WHITE, ManaColor.COLORLESS};
		Map<ManaColor, PieChart.Data> colorData = Maps.newHashMap();
		int size = deck.getAllCards().size();
		IntegerProperty sizeWithoutColors = new SimpleIntegerProperty(size);
		for (ManaColor color : allColors)
		{
			PieChart.Data data = new PieChart.Data("", 0);
			data.nameProperty()
					.bind(color.getTranslatedName().concat(" ").concat(data.pieValueProperty().divide(sizeWithoutColors).multiply(100.0D).asString("%.1f")).concat(" " + "%"));
			colors.getData().add(data);
			colorData.put(color, data);
			data.getNode().setStyle("-fx-pie-color: " + color.name().toLowerCase() + ";");
			if (color == ManaColor.COLORLESS)
				data.getNode().setStyle("-fx-pie-color: gray;");
		}
		PieChart.Data multicolor = new PieChart.Data("", 0.0D);
		multicolor.nameProperty()
				.bind(new SimpleStringProperty("Multicolore ").concat(multicolor.pieValueProperty().divide(sizeWithoutColors).multiply(100.0D).asString("%.1f")).concat(" %"));
		colors.getData().add(multicolor);
		multicolor.getNode().setStyle("-fx-pie-color: gold;");

		for (OwnedCard card : deck.getAllCards())
		{
			if (card.getCard().type.is(CardType.LAND))
			{
				sizeWithoutColors.set(sizeWithoutColors.get() - 1);
				continue;
			}

			if (card.getCard().colors.length > 1)
				multicolor.setPieValue(multicolor.getPieValue() + 1);
			else
			{
				PieChart.Data data = colorData.get(card.getCard().colors[0]);
				data.setPieValue(data.getPieValue() + 1);
			}
		}

		StreamSupport.stream(Lists.newArrayList(colors.getData())).filter(data -> data.getPieValue() == 0).forEach(data -> colors.getData().remove(data));

		for (Map.Entry<CardType, Map<Card, AtomicInteger>> entry : deck.cardsByType().entrySet())
		{
			PieChart.Data data = new PieChart.Data("", 0);
			data.nameProperty().bind(entry.getKey().getTranslatedName().concat(" ").concat(data.pieValueProperty().divide(size).multiply(100.0D).asString("%.1f")).concat(" %"));
			for (Map.Entry<Card, AtomicInteger> entry2 : entry.getValue().entrySet())
				data.setPieValue(data.getPieValue() + entry2.getValue().get());
			types.getData().add(data);
		}

		Map<Rarity, PieChart.Data> rarities = Maps.newHashMap();
		for (Rarity r : new Rarity[] {Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.MYTHIC, Rarity.BASIC_LAND})
		{
			PieChart.Data data = new PieChart.Data("", 0);
			data.nameProperty().bind(r.getTranslatedName().concat(" ").concat(data.pieValueProperty().divide(size).multiply(100.0D).asString("%.1f")).concat(" %"));
			rarities.put(r, data);
			rarity.getData().add(data);
		}

		for (OwnedCard card : deck.getAllCards())
			rarities.get(card.getCard().rarity).setPieValue(rarities.get(card.getCard().rarity).getPieValue() + 1);

		//noinspection MismatchedQueryAndUpdateOfCollection
		Map<Integer, DoubleProperty> cmcData = new DefaultTreeMap<>(Integer::compare, SimpleDoubleProperty::new);
		for (OwnedCard card : deck.getAllCards())
		{
			if (card.getCard().type.is(CardType.LAND))
				continue;
			cmcData.get((int) card.getCard().cmc).set(cmcData.get((int) card.getCard().cmc).get() + 1);
		}

		StreamSupport.stream(cmcData.entrySet()).forEach(entry -> cmc.getData()
				.add(new PieChart.Data(entry.getKey() + " : " + entry.getValue().divide(sizeWithoutColors).multiply(100).asString("%.1f").get() + " %", entry.getValue().get())));
	}
}
