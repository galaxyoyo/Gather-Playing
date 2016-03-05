package fr.galaxyoyo.gatherplaying;

import fr.galaxyoyo.gatherplaying.client.gui.PlayerInfos;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PlayerData
{
	private final Player player;
	private final IntegerProperty hp = new SimpleIntegerProperty(20);
	private final ObservableList<OwnedCard> hand = FXCollections.observableArrayList();
	private final ObservableList<PlayedCard> played = FXCollections.observableArrayList();
	private final ObservableList<OwnedCard> graveyard = FXCollections.observableArrayList();
	private final ObservableList<OwnedCard> exile = FXCollections.observableArrayList();
	private byte mulligan = 7;
	private Library library;

	public PlayerData(Player player)
	{
		this.player = player;
		hp.addListener((observable, oldValue, newValue) -> PlayerInfos.getInfos(player).updateLife());
	}

	public Player getPlayer()
	{
		return player;
	}

	public int getHp()
	{
		return hp.get();
	}

	public void setHp(int hp)
	{
		this.hp.set(hp);
	}

	public IntegerProperty hpProperty()
	{
		return hp;
	}

	public ObservableList<OwnedCard> getHand()
	{
		return hand;
	}

	public ObservableList<PlayedCard> getPlayed()
	{
		return played;
	}

	public ObservableList<OwnedCard> getGraveyard()
	{
		return graveyard;
	}

	public ObservableList<OwnedCard> getExile()
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
}
