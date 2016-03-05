package fr.galaxyoyo.gatherplaying;

import io.netty.channel.Channel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.UUID;

public class Player
{
	public Channel connection;
	public String lastIp;
	public ObservableSet<OwnedCard> cards = FXCollections.observableSet();
	public ObservableSet<Deck> decks = FXCollections.observableSet();
	public UUID uuid;
	public String name;
	public String email;
	public String sha1Pwd;
	public int money;
	public Party runningParty;

	public Player(Channel channel)
	{
		this.connection = channel;
	}

	public Player() {}

	public PlayerData getData()
	{
		assert runningParty != null : new IllegalAccessException("The player isn't playing!");
		return runningParty.getData(this);
	}

	public void importFrom(Player db)
	{
		uuid = db.uuid;
		name = db.name;
		email = db.email;
		sha1Pwd = db.sha1Pwd;
		money = db.money;
		cards = db.cards;
	}

	@Override
	public String toString() { return name; }

	@Override
	public int hashCode()
	{
		return uuid.hashCode();
	}
}