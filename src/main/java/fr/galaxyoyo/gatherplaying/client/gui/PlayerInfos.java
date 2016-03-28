package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.protocol.packets.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class PlayerInfos extends AbstractController implements Initializable
{
	private static Map<Player, PlayerInfos> infosByPlayer = Maps.newHashMap();
	private Player player;
	private boolean editable;

	@FXML
	private Label name;

	@FXML
	private Label life;

	@FXML
	private Button minus1Life, minus5Life, plus1Life, plus5Life;

	@FXML
	private Label library;

	@FXML
	private ImageView graveyard;

	@FXML
	private ImageView exile;

	public static PlayerInfos getInfos(Player player)
	{
		return infosByPlayer.get(player);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		((ImageView) library.getGraphic()).setImage(CardImageManager.getImage((Card) null));

		Border border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
		name.setBorder(border);
		life.setBorder(border);

		life.setOnMouseReleased(event -> {
			if (!editable || event.getButton() != MouseButton.SECONDARY)
				return;

			ContextMenu menu = new ContextMenu();

			MenuItem define = new MenuItem("Définir");
			define.setStyle("-fx-font-weight: normal; -fx-font-size: 12px;");
			define.setOnAction(e -> {
				if (Utils.isDesktop())
				{
					Dialog<Integer> dialog = new Dialog<>();
					dialog.setTitle("Définition des points de vie");
					dialog.setHeaderText("Définissez vos points de vie :");
					Spinner<Integer> spinner = new Spinner<>(0, 9999, player.getData().getHp());
					dialog.getDialogPane().setContent(spinner);
					dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
					dialog.setResultConverter(param -> param == ButtonType.CANCEL ? null : spinner.getValue());

					dialog.showAndWait().filter(integer -> integer != null && player.getData().getHp() != integer).ifPresent(integer -> {
						PacketMixEditLife pkt = PacketManager.createPacket(PacketMixEditLife.class);
						pkt.p = player;
						pkt.newLife = spinner.getValue();
						PacketManager.sendPacketToServer(pkt);

					});
				}
				else
				{
					com.gluonhq.charm.glisten.control.Dialog<Integer> dialog = new com.gluonhq.charm.glisten.control.Dialog<>("Définition des points de vie");
					Spinner<Integer> spinner = new Spinner<>(0, 9999, player.getData().getHp());
					dialog.setContent(spinner);
					Button apply = new Button("Appliquer");
					apply.setOnAction(event1 -> {
						dialog.setResult(spinner.getValue());
						dialog.hide();
					});
					Button cancel = new Button("Fermer");
					cancel.setOnAction(event1 -> dialog.hide());
					dialog.getButtons().addAll(apply, cancel);

					dialog.showAndWait().filter(integer -> player.getData().getHp() == integer).ifPresent(integer -> {
						PacketMixEditLife pkt = PacketManager.createPacket(PacketMixEditLife.class);
						pkt.p = player;
						pkt.newLife = spinner.getValue();
						PacketManager.sendPacketToServer(pkt);

					});
				}
			});
			menu.getItems().add(define);

			menu.show(life, event.getScreenX(), event.getScreenY());
		});
		life.prefWidthProperty().bind(minus1Life.widthProperty().multiply(2));
		minus1Life.setOnAction(event -> {
			if (!Client.getRunningParty().isStarted())
				return;
			PacketMixEditLife pkt = PacketManager.createPacket(PacketMixEditLife.class);
			pkt.p = player;
			pkt.newLife = player.getData().getHp() - 1;
			PacketManager.sendPacketToServer(pkt);
		});
		minus5Life.setOnAction(event -> {
			if (!Client.getRunningParty().isStarted())
				return;
			PacketMixEditLife pkt = PacketManager.createPacket(PacketMixEditLife.class);
			pkt.p = player;
			pkt.newLife = player.getData().getHp() - 5;
			PacketManager.sendPacketToServer(pkt);
		});
		plus1Life.setOnAction(event -> {
			if (!Client.getRunningParty().isStarted())
				return;
			PacketMixEditLife pkt = PacketManager.createPacket(PacketMixEditLife.class);
			pkt.p = player;
			pkt.newLife = player.getData().getHp() + 1;
			PacketManager.sendPacketToServer(pkt);
		});
		plus5Life.setOnAction(event -> {
			if (!Client.getRunningParty().isStarted())
				return;
			PacketMixEditLife pkt = PacketManager.createPacket(PacketMixEditLife.class);
			pkt.p = player;
			pkt.newLife = player.getData().getHp() + 5;
			PacketManager.sendPacketToServer(pkt);
		});
		minus1Life.setPadding(new Insets(3));
		minus5Life.setPadding(new Insets(3));
		plus1Life.setPadding(new Insets(3));
		plus5Life.setPadding(new Insets(3));
		name.prefWidthProperty().bind(((VBox) name.getParent()).widthProperty());
		life.prefWidthProperty().bind(name.widthProperty().divide(2));
		life.prefHeightProperty().bind(minus1Life.heightProperty().multiply(2));
		minus1Life.prefWidthProperty().bind(life.widthProperty().divide(2));
		minus5Life.prefWidthProperty().bind(minus1Life.widthProperty());
		plus1Life.prefWidthProperty().bind(minus1Life.widthProperty());
		plus5Life.prefWidthProperty().bind(minus1Life.widthProperty());
		library.setPrefWidth(74.0D);

		library.setOnMouseReleased(event -> {
			if (!editable || event.getButton() != MouseButton.SECONDARY || !Client.getRunningParty().isStarted())
				return;

			ContextMenu menu = new ContextMenu();

			MenuItem draw = new MenuItem("Piocher ...");
			draw.setStyle("-fx-font-weight: normal; -fx-font-size: 12px;");
			draw.setOnAction(e -> {
				if (Utils.isDesktop())
				{
					Dialog<Integer> dialog = new Dialog<>();
					dialog.setTitle("Piocher des cartes");
					dialog.setHeaderText("Choisissez combien de cartes vous voulez piocher");
					Spinner<Integer> spinner = new Spinner<>(1, 1, getLibrary());
					dialog.getDialogPane().setContent(spinner);
					dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
					dialog.setResultConverter(param -> param == ButtonType.OK ? spinner.getValue() : null);
					dialog.showAndWait().filter(integer -> integer != null).ifPresent(integer -> {
						PacketMixDrawCard pkt = PacketManager.createPacket(PacketMixDrawCard.class);
						pkt.count = integer;
						PacketManager.sendPacketToServer(pkt);
					});
				}
				else
				{
					com.gluonhq.charm.glisten.control.Dialog<Integer> dialog = new com.gluonhq.charm.glisten.control.Dialog<>("Piocher des cartes");
					Spinner<Integer> spinner = new Spinner<>(1, 1, getLibrary());
					dialog.setContent(spinner);
					Button ok = new Button("Ok");
					ok.setOnAction(e1 -> {
						dialog.setResult(spinner.getValue());
						dialog.hide();
					});
					Button cancel = new Button("Annuler");
					cancel.setOnAction(e1 -> dialog.hide());
					dialog.showAndWait().filter(integer -> integer != null).ifPresent(integer -> {
						PacketMixDrawCard pkt = PacketManager.createPacket(PacketMixDrawCard.class);
						pkt.count = integer;
						PacketManager.sendPacketToServer(pkt);
					});
				}
			});
			menu.getItems().add(draw);

			MenuItem shuffle = new MenuItem("Mélanger");
			shuffle.setStyle("-fx-font-weight: normal; -fx-font-size: 12px;");
			shuffle.setOnAction(e -> PacketManager.sendPacketToServer(PacketManager.createPacket(PacketInShuffle.class)));
			menu.getItems().add(shuffle);

			MenuItem mulligan = new MenuItem("Mulligan");
			mulligan.setStyle("-fx-font-weight: normal; -fx-font-size: 12px;");
			mulligan.setOnAction(e -> {
				PacketMixDrawCard pkt = PacketManager.createPacket(PacketMixDrawCard.class);
				pkt.action = PacketMixDrawCard.Action.MULLIGAN;
				PacketManager.sendPacketToServer(pkt);
			});
			if (player.getData().getMulligan() > 0)
				menu.getItems().add(mulligan);

			Menu scry = new Menu("Regard");
			scry.setStyle("-fx-font-weight: normal; -fx-font-size: 12px;");
			for (int i = 1; i <= 5; ++i)
			{
				final int index = i;
				MenuItem item = new MenuItem("Regard " + i);
				item.setStyle("-fx-font-weight: normal; -fx-font-size: 12px;");
				item.setOnAction(e -> {
					PacketMixScry pkt = PacketManager.createPacket(PacketMixScry.class);
					pkt.numCards = (byte) index;
					PacketManager.sendPacketToServer(pkt);
				});
				scry.getItems().add(item);
			}

			menu.getItems().add(scry);

			MenuItem findCard = new MenuItem("Chercher une carte ...");
			findCard.setStyle("-fx-font-weight: normal; -fx-font-size: 12px;");
			findCard.setOnAction(e -> {
				GridPane pane = new GridPane();
				Label filterLbl = new Label("Filtre :");
				pane.getChildren().add(filterLbl);
				GridPane.setRowIndex(filterLbl, 1);
				ComboBox<PacketMixFindCard.FilterType> filterType = new ComboBox<>();
				ComboBox<Object> filter = new ComboBox<>();
				filterType.getSelectionModel().selectedItemProperty().addListener(c -> {
					filter.getItems().clear();
					PacketMixFindCard.FilterType type = filterType.getSelectionModel().getSelectedItem();
					try
					{
						Object[] objs = (Object[]) type.filter.getDeclaredMethod("values").invoke(null);
						for (Object obj : objs)
							filter.getItems().add(obj);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				});
				filterType.getItems().addAll(PacketMixFindCard.FilterType.values());
				filterType.getSelectionModel().select(PacketMixFindCard.FilterType.TYPE);
				pane.getChildren().add(filterType);
				GridPane.setColumnIndex(filterType, 1);
				pane.getChildren().add(filter);
				GridPane.setRowIndex(filter, 1);
				GridPane.setColumnIndex(filter, 1);

				if (Utils.isDesktop())
				{
					Dialog<ButtonType> dialog = new Dialog<>();
					dialog.setTitle("Chercher une carte");
					dialog.setHeaderText("Sélectionnez vos filtres");
					dialog.getDialogPane().setContent(pane);
					dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
					pane.getChildren().add(new Label("Type de filtre :"));
					dialog.showAndWait().filter(buttonType -> buttonType == ButtonType.APPLY).ifPresent(buttonType -> {
						PacketMixFindCard pkt = PacketManager.createPacket(PacketMixFindCard.class);
						pkt.type = filterType.getValue();
						pkt.filter = filter.getValue();
						PacketManager.sendPacketToServer(pkt);
					});
				}
				else
				{
					com.gluonhq.charm.glisten.control.Dialog<ButtonType> dialog = new com.gluonhq.charm.glisten.control.Dialog<>("Chercher une carte");
					dialog.setContent(pane);
					Button apply = new Button("Appliquer");
					apply.setOnAction(event1 -> dialog.setResult(ButtonType.APPLY));
					Button cancel = new Button("Annuler");
					cancel.setOnAction(event1 -> dialog.setResult(ButtonType.CANCEL));
					dialog.showAndWait().filter(buttonType -> buttonType == ButtonType.APPLY).ifPresent(buttonType -> {
						PacketMixFindCard pkt = PacketManager.createPacket(PacketMixFindCard.class);
						pkt.type = filterType.getValue();
						pkt.filter = filter.getValue();
						PacketManager.sendPacketToServer(pkt);
					});
				}
			});
			menu.getItems().add(findCard);

			menu.show(library, event.getScreenX(), event.getScreenY());
		});

		graveyard.setOnMouseEntered(event -> {
			if (graveyard.getImage() != null)
				GameMenu.instance().setImage(graveyard.getImage());
		});
		graveyard.setOnMouseReleased(event -> {
			if (event.getButton() != MouseButton.SECONDARY || !Client.getRunningParty().isStarted())
				return;

			ContextMenu menu = new ContextMenu();

			MenuItem show = new MenuItem("Afficher le cimetière");
			show.setOnAction(e -> {
				HBox box = new HBox();
				for (OwnedCard c : player.getData().getGraveyard())
				{
					DestroyedCardShower shower = new DestroyedCardShower(c, false);
					box.getChildren().add(shower);
					HBox.setHgrow(shower, Priority.ALWAYS);
					HBox.setMargin(shower, new Insets(5.0D));
				}
				ScrollPane pane = new ScrollPane(box);
				pane.setPrefHeight(355.0D);
				pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				pane.setStyle("-fx-background-color: null;");
				if (Utils.isDesktop())
				{
					Dialog<Object> dialog = new Dialog<>();
					dialog.setTitle("Visualisation du cimetière");
					dialog.getDialogPane().setContent(pane);
					dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
					dialog.showAndWait();
				}
				else
				{
					com.gluonhq.charm.glisten.control.Dialog<Object> dialog = new com.gluonhq.charm.glisten.control.Dialog<>("Visualisation du cimetière");
					dialog.setContent(box);
					Button close = new Button("Fermer");
					close.setOnAction(event1 -> dialog.hide());
					dialog.showAndWait();
				}
			});
			menu.getItems().add(show);

			menu.show(graveyard, event.getScreenX(), event.getScreenY());
		});

		exile.setOnMouseEntered(event -> {
			if (exile.getImage() != null)
				GameMenu.instance().setImage(exile.getImage());
		});
		exile.setOnMouseReleased(event -> {
			if (event.getButton() != MouseButton.SECONDARY || !Client.getRunningParty().isStarted())
				return;

			ContextMenu menu = new ContextMenu();

			MenuItem show = new MenuItem("Afficher l'exil");
			show.setOnAction(e -> {
				Dialog<Object> dialog = new Dialog<>();
				dialog.setTitle("Affichage de l'exil");
				HBox box = new HBox();
				for (OwnedCard c : player.getData().getExile())
				{
					DestroyedCardShower shower = new DestroyedCardShower(c, true);
					box.getChildren().add(shower);
					HBox.setHgrow(shower, Priority.ALWAYS);
					HBox.setMargin(shower, new Insets(5.0D));
				}
				ScrollPane pane = new ScrollPane(box);
				pane.setPrefHeight(355.0D);
				pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				pane.setStyle("-fx-background-color: null;");
				dialog.getDialogPane().setContent(pane);
				dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
				dialog.showAndWait();
			});
			menu.getItems().add(show);

			menu.show(exile, event.getScreenX(), event.getScreenY());
		});
		ColorAdjust ca = new ColorAdjust();
		ca.setSaturation(-1);
		exile.setEffect(ca);

		Platform.runLater(() -> {
			if (GameMenu.instance().adverseInfos == null)
			{
				GameMenu.instance().adverseInfos = this;
				life.prefWidthProperty().unbind();
				life.prefWidthProperty().bind(name.widthProperty());
				GridPane.setRowIndex(life, 0);
				GridPane.setColumnIndex(life, 0);
				GridPane.setRowSpan(life, 1);
				GridPane.setColumnSpan(life, 1);
				GridPane pane = (GridPane) life.getParent();
				pane.getChildren().clear();
				pane.getChildren().add(life);
				VBox box = (VBox) name.getParent();
				FXCollections.reverse(box.getChildren());
			}
			else
			{
				GameMenu.instance().playerInfos = this;
				setPlayer(Client.localPlayer);
				ScrollPane scrollPane = (ScrollPane) library.getParent().getParent().getParent().getParent();
				scrollPane.prefWidthProperty().bind(Bindings.max(library.widthProperty(), GameMenu.instance().adverseInfos.library.widthProperty()).add(20));
			}
		});
	}

	public int getLibrary()
	{
		//noinspection StatementWithEmptyBody
		while (library.getText().isEmpty()) ;
		return Integer.parseInt(library.getText());
	}

	public void setPlayer(Player player)
	{
		this.player = player;
		infosByPlayer.put(player, this);
		editable = player == Client.localPlayer;
		Platform.runLater(() -> name.setText(player.name));
	}

	public void setLibrary(int size)
	{
		Platform.runLater(() -> library.setText(String.valueOf(size)));
	}

	public void updateLife()
	{
		Platform.runLater(() -> {
			if (!life.isVisible())
			{
				life.setVisible(true);
				minus1Life.setVisible(true);
				minus5Life.setVisible(true);
				plus1Life.setVisible(true);
				plus5Life.setVisible(true);
			}
			this.life.textProperty().bind(player.getData().hpProperty().asString());
		});
	}

	public void addLibrary()
	{
		addLibrary(1);
	}

	public void addLibrary(int count)
	{
		setLibrary(getLibrary() + count);
	}

	public void removeLibrary()
	{
		setLibrary(getLibrary() - 1);
	}

	public void graveyard(PlayedCard card)
	{
		Platform.runLater(() -> graveyard.setImage(card == null ? null : CardImageManager.getImage(card)));
	}

	public void exile(PlayedCard card)
	{
		Platform.runLater(() -> exile.setImage(card == null ? null : CardImageManager.getImage(card)));
	}
}
