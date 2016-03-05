package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.*;
import java8.util.stream.Collectors;
import java8.util.stream.RefStreams;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class DeckEditorFilter extends AbstractController implements Initializable
{
	@FXML
	public TextField name;

	@FXML
	public TextArea ability;

	@FXML
	public ListView<ManaColor> color;

	@FXML
	public ListView<Rarity> rarity;

	@FXML
	public ListView<Integer> cmc;

	@FXML
	public ListView<CardType> type;

	@FXML
	public ListView<SubType> subtypes;

	@FXML
	public ListView<Set> set;

	@FXML
	public ListView<Rules> rules;

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

		type.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		type.getItems().addAll(RefStreams.of(CardType.values())
				.filter(type -> !type.name().contains("TOKEN") && type != CardType.EMBLEM && type != CardType.PHENOMENON && type != CardType.PLANE && type != CardType.SCHEME &&
								type != CardType.PLAYER && type != CardType.CREATURE_PLANESWALKER).collect(Collectors.toList()));
		type.getSelectionModel().selectAll();

		subtypes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		subtypes.getItems().add(null);
		subtypes.getItems().addAll(RefStreams.of(SubType.values()).filter(subType -> subType.canApplicate(type.getItems())).collect(Collectors.toList()));
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
				setText(item.code);
			}
		});

		rules.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		rules.getItems().addAll(Rules.values());
		rules.getSelectionModel().select(Rules.LEGACY);
		DeckShower.rulesProp = rules.getSelectionModel().selectedItemProperty();

		DeckEditor.filters = this;
	}
}
