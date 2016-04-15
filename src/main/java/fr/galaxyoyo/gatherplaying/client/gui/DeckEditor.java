package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.base.Strings;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.Config;
import java8.util.stream.RefStreams;
import java8.util.stream.StreamSupport;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class DeckEditor extends AbstractController implements Initializable
{
	private static DeckEditorFilter filters;
	private static CardDetailsShower cardShower;
	private static DeckShower deckShower;
	private static DeckEditor editor;
	@FXML
	public TableView<Card> table;
	@FXML
	private TableColumn<Card, String> name_EN;
	@FXML
	private TableColumn<Card, String> name_FR;
	@FXML
	private TableColumn<Card, Set> set;
	@FXML
	private TableColumn<Card, ManaColor[]> manaCost;
	@FXML
	private Label cardsCount;

	public static void setCardShower(CardDetailsShower cardShower)
	{
		DeckEditor.cardShower = cardShower;
	}

	public static DeckShower getDeckShower()
	{
		return deckShower;
	}

	public static void setDeckShower(DeckShower deckShower)
	{
		DeckEditor.deckShower = deckShower;
	}

	public static DeckEditorFilter getFilters()
	{
		return filters;
	}

	public static void setFilters(DeckEditorFilter filters)
	{
		DeckEditor.filters = filters;
	}

	public static DeckEditor getEditor()
	{
		return editor;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		editor = this;

		table.prefWidthProperty().bind(Client.getStage().widthProperty().divide(2));

		ObservableList<Card> allCards = FXCollections.observableArrayList(MySQL.getAllCards());
		allCards.sort(Card::compareTo);
		ObservableList<Card> cards;
		if (Config.getStackCards())
		{
			ObservableList<Card> stackedCards = FXCollections.observableArrayList();
			ObservableSet<String> added = FXCollections.observableSet();
			StreamSupport.stream(allCards).filter(card -> !added.contains(card.getName().get("en"))).forEach(card -> {
				stackedCards.add(card);
				added.add(card.getName().get("en"));
			});
			cards = new SortedList<>(stackedCards);
		}
		else
			cards = new SortedList<>(allCards);
		FilteredList<Card> filtered = new FilteredList<>(cards, card -> {
			if (card == null)
				return false;
			if (card.getLayout() == Layout.TOKEN)
				return false;
			if ((card.getLayout() == Layout.DOUBLE_FACED || card.getLayout() == Layout.FLIP) && card.getManaCost() == null)
				return false;
			if (filters.getRarity().getSelectionModel().isEmpty() || filters.getColor().getSelectionModel().isEmpty() || filters.getCmc().getSelectionModel().isEmpty() ||
					filters.getType().getSelectionModel().isEmpty() || filters.getSubtypes().getSelectionModel().isEmpty() || filters.getSet().getSelectionModel().isEmpty())
				return false;
			if (!filters.getName().getText().isEmpty() && !card.getName().get("en").replace("Æ", "AE").toLowerCase().contains(filters.getName().getText().toLowerCase()) &&
					!card.getTranslatedName().get().replace("Æ", "AE").toLowerCase().contains(filters.getName().getText().toLowerCase()))
				return false;
			if (!filters.getAbility().getText().isEmpty())
			{
				if (card.getAbilityMap().get("en") == null || card.getAbilityMap().get("en").isEmpty())
					return false;
				Pattern pattern = Pattern.compile(filters.getAbility().getText());

				if (!pattern.matcher(card.getAbilityMap().get("en")).find() && !pattern.matcher(card.getAbility()).find())
					return false;
			}
			List<ManaColor> colors = filters.getColor().getSelectionModel().getSelectedItems();
			if (RefStreams.of(card.getColors()).noneMatch(colors::contains))
				return false;
			if (!filters.getCmc().getSelectionModel().getSelectedItems().contains((int) card.getCmc()))
				return false;
			if (!filters.getPower().getSelectionModel().getSelectedItems().contains(Strings.nullToEmpty(card.getPower())))
				return false;
			if (!filters.getToughness().getSelectionModel().getSelectedItems().contains(Strings.nullToEmpty(card.getToughness())))
				return false;
			if (!filters.getRarity().getSelectionModel().getSelectedItems().contains(card.getRarity()))
				return false;
			if (!filters.getType().getSelectionModel().getSelectedItems().contains(card.getType()))
				return false;
			List<SubType> subtypes = filters.getSubtypes().getSelectionModel().getSelectedItems();
			if (card.getSubtypes().length == 0)
			{
				if (subtypes.get(0) != null)
					return false;
			}
			else if (RefStreams.of(card.getSubtypes()).noneMatch(subtypes::contains))
				return false;
			return !(!Utils.DEBUG && card.getSet().getReleaseDate().getTime() > System.currentTimeMillis()) &&
					filters.getSet().getSelectionModel().getSelectedItems().contains(card.getSet()) && card.isLegal(filters.getRules().getSelectionModel().getSelectedItem());
		});
		name_EN.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName().get("en")));
		name_FR.setCellValueFactory(param -> param.getValue().getTranslatedName());
		set.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSet()));
		set.setCellFactory(param -> new TableCell<Card, Set>()
		{
			@Override
			protected void updateItem(Set item, boolean empty)
			{
				super.updateItem(item, empty);
				setAlignment(Pos.CENTER);
				if (item != null)
					setText(item.getCode());
				else
					setText(null);
			}
		});
		manaCost.setCellValueFactory(param -> new SimpleObjectProperty<>(
				param.getValue().getManaCost() == null ? param.getValue().getType().is(CardType.LAND) ? null : new ManaColor[]{ManaColor.NEUTRAL_0} : param.getValue().getManaCost()));
		manaCost.setCellFactory(param -> new TableCell<Card, ManaColor[]>()
		{
			@Override
			protected void updateItem(ManaColor[] item, boolean empty)
			{
				super.updateItem(item, empty);
				HBox box = new HBox();
				setGraphic(box);
				if (item == null)
					return;
				for (ManaColor mc : item)
					box.getChildren().add(new ImageView(CardImageManager.getIcon(mc)));
			}
		});
		manaCost.setComparator((o1, o2) -> {
			int cmc1 = 0, cmc2 = 0;
			if (o1 == null)
				o1 = new ManaColor[0];
			if (o2 == null)
				o2 = new ManaColor[0];
			for (ManaColor mc : o1)
			{
				if (mc == ManaColor.NEUTRAL_X)
					continue;
				if (mc.name().contains("NEUTRAL"))
					cmc1 += Integer.parseInt(mc.getAbbreviate());
				else if (mc.name().contains("2"))
					cmc1 += 2;
				else
					++cmc1;
			}
			for (ManaColor mc : o2)
			{
				if (mc == ManaColor.NEUTRAL_X)
					continue;
				if (mc.name().contains("NEUTRAL"))
					cmc2 += Integer.parseInt(mc.getAbbreviate());
				else if (mc.name().contains("2"))
					cmc2 += 2;
				else
					++cmc2;
			}
			int ret = Integer.compare(cmc1, cmc2);
			if (ret == 0)
				ret = 0;
			return ret;
		});
		SortedList<Card> sorted = new SortedList<>(filtered);
		sorted.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sorted);
		IntegerBinding size = Bindings.createIntegerBinding(filtered::size, filtered.predicateProperty());
		cardsCount.textProperty().bind(size.asString()
				.concat(Bindings.when(Bindings.createBooleanBinding(() -> filtered.size() > 1, filtered.predicateProperty())).then(" cartes trouvées")
						.otherwise(" carte " + "trouvée")));
		set.setSortType(TableColumn.SortType.DESCENDING);
		//noinspection unchecked
		table.getSortOrder().setAll(set);

		ChangeListener<Object> l = (observable, oldValue, newValue) -> Platform.runLater(() -> {
			try
			{
				Method m = ObjectPropertyBase.class.getDeclaredMethod("markInvalid");
				m.setAccessible(true);
				m.invoke(filtered.predicateProperty());
				SortedList<Card> s = new SortedList<>(filtered);
				s.comparatorProperty().bind(table.comparatorProperty());
				table.setItems(s);
			}
			catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored)
			{
			}
		});

		table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> cardShower.updateCard(newValue)));

		filters.getName().textProperty().addListener(l);
		filters.getAbility().textProperty().addListener(l);
		filters.getRarity().getSelectionModel().selectedItemProperty().addListener(l);
		filters.getColor().getSelectionModel().selectedItemProperty().addListener(l);
		filters.getCmc().getSelectionModel().selectedItemProperty().addListener(l);
		filters.getPower().getSelectionModel().selectedItemProperty().addListener(l);
		filters.getToughness().getSelectionModel().selectedItemProperty().addListener(l);
		filters.getType().getSelectionModel().selectedItemProperty().addListener(l);
		filters.getSubtypes().getSelectionModel().selectedItemProperty().addListener(l);
		filters.getSet().getSelectionModel().selectedItemProperty().addListener(l);
		filters.getRules().getSelectionModel().selectedItemProperty().addListener(l);

		DeckShower.setTable(table);
	}

	@Override
	public void onKeyReleased(KeyEvent event)
	{
		System.out.println(event.getCode() + ", " + event.getText());
		deckShower.onKeyReleased(event);
	}
}
