package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Config;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DeckEditorFilter extends AbstractController implements Initializable
{
	@FXML
	private TextField name;

	@FXML
	private TextArea ability;

	@FXML
	private ListView<ManaColor> color;

	@FXML
	private ListView<Rarity> rarity;

	@FXML
	private ListView<Integer> cmc;

	@FXML
	private ListView<String> power;

	@FXML
	private ListView<String> toughness;

	@FXML
	private ListView<CardType> type;

	@FXML
	private ListView<SubType> subtypes;

	@FXML
	private ListView<Set> set;

	@FXML
	private ListView<Rules> rules;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		name.setPromptText("Nom d'une carte ... (ex : " + Lists.newArrayList(MySQL.getAllCards()).get(Utils.RANDOM.nextInt(MySQL.getAllCards().size())).getTranslatedName().get()
				+ ")");

		color.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		color.getItems().addAll(ManaColor.RED, ManaColor.WHITE, ManaColor.BLUE, ManaColor.GREEN, ManaColor.BLACK, ManaColor.COLORLESS);
		color.getSelectionModel().selectAll();
		color.setCellFactory(param -> new ListCell<ManaColor>()
		{
			@Override
			protected void updateItem(ManaColor item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item == null)
					return;
				setGraphic(new ImageView(CardImageManager.getIcon(item)));
			}
		});

		rarity.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		rarity.getItems().addAll(Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.MYTHIC, Rarity.BASIC_LAND);
		rarity.getSelectionModel().selectAll();

		cmc.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		cmc.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 1000000);
		cmc.setCellFactory(param -> new ListCell<Integer>()
		{
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null)
					setGraphic(new ImageView(CardImageManager.getIcon(item.toString(), true)));
			}
		});
		cmc.getSelectionModel().selectAll();

		//noinspection unchecked
		for (ListView<String> view : new ListView[] {power, toughness})
		{
			view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			ObservableList<String> list = view.getItems();
			list.addAll("", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "99", "*", "1+*", "2+*", "*²");
			view.getSelectionModel().selectAll();
		}

		type.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		type.getItems().addAll(Arrays.stream(CardType.values())
				.filter(type -> !type.name().contains("TOKEN") && type != CardType.EMBLEM && type != CardType.MONARCH && type != CardType.PHENOMENON && type != CardType.PLANE && type !=
						CardType.SCHEME &&
						type != CardType.PLAYER && type != CardType.CREATURE_PLANESWALKER).collect(Collectors.toList()));
		type.getSelectionModel().selectAll();

		subtypes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		subtypes.getItems().add(null);
		subtypes.getItems().addAll(Arrays.stream(SubType.values()).filter(subType -> subType.canApplicate(type.getItems())).collect(Collectors.toList()));
		subtypes.setCellFactory(param -> new ListCell<SubType>()
		{
			@Override
			protected void updateItem(SubType item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item == null)
					setText("");
				else
					setText(item.toString());
			}
		});
		subtypes.getSelectionModel().selectAll();

		set.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		List<Set> sets = Lists.newArrayList(MySQL.getAllSets());
		Collections.sort(sets);
		set.getItems().addAll(sets);
		set.getSelectionModel().selectAll();
		set.setCellFactory(param -> new ListCell<Set>()
		{
			@Override
			protected void updateItem(Set item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item == null)
					return;
				setText(item.getCode());
			}
		});

		rules.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		rules.getItems().addAll(Arrays.stream(Rules.values()).filter(rules -> !rules.isLimited()).collect(Collectors.toList()));
		rules.getSelectionModel().select(Config.getFormat());
		DeckShower.setRulesProp(rules.getSelectionModel().selectedItemProperty());

		DeckEditor.setFilters(this);
	}

	public TextField getName()
	{
		return name;
	}

	public TextArea getAbility()
	{
		return ability;
	}

	public ListView<ManaColor> getColor()
	{
		return color;
	}

	public ListView<Rarity> getRarity()
	{
		return rarity;
	}

	public ListView<Integer> getCmc()
	{
		return cmc;
	}

	public ListView<CardType> getType()
	{
		return type;
	}

	public ListView<SubType> getSubtypes()
	{
		return subtypes;
	}

	public ListView<Set> getSet()
	{
		return set;
	}

	public ListView<Rules> getRules()
	{
		return rules;
	}

	public ListView<String> getPower()
	{
		return power;
	}

	public ListView<String> getToughness()
	{
		return toughness;
	}
}
