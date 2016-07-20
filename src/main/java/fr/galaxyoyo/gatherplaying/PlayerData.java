package fr.galaxyoyo.gatherplaying;

import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class PlayerData implements Targetable
{
	private final Player player;
	private final IntegerProperty hp = new SimpleIntegerProperty(20);
	private final ObservableList<PlayedCard> hand = FXCollections.observableArrayList();
	private final ObservableList<PlayedCard> played = FXCollections.observableArrayList();
	private final ObservableList<PlayedCard> graveyard = FXCollections.observableArrayList();
	private final ObservableList<PlayedCard> exile = FXCollections.observableArrayList();
	private byte mulligan = 7;
	private Library library;

	public PlayerData(Player player)
	{
		this.player = player;
	}

	public Player getPlayer()
	{
		return player;
	}

	public IntegerProperty hpProperty()
	{
		return hp;
	}

	public ObservableList<PlayedCard> getHand()
	{
		return hand;
	}

	public ObservableList<PlayedCard> getPlayed()
	{
		return played;
	}

	public ObservableList<PlayedCard> getGraveyard()
	{
		return graveyard;
	}

	public ObservableList<PlayedCard> getExile()
	{
		return exile;
	}

	public byte getMulligan()
	{
		return mulligan;
	}

	public void setMulligan(byte mulligan)
	{
		this.mulligan = mulligan;
	}

	public Library getLibrary()
	{
		return library;
	}

	public void setLibrary(Library library)
	{
		this.library = library;
	}

	@Override
	public int getDamages()
	{
		return 0;
	}

	@Override
	public void sendDamages(int damages)
	{
		setHp(getHp() - damages);
	}

	public int getHp()
	{
		return hp.get();
	}

	public void setHp(int hp)
	{
		this.hp.set(hp);
	}

	@Override
	public void resetDamages()
	{
	}

	@Override
	public Node getVisible()
	{
		return Client.localPlayer == player ? GameMenu.instance().playerInfos.getLibraryField() : GameMenu.instance().adverseInfos.getLibraryField();
	}
}
