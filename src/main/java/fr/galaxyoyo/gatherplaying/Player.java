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
		assert runningParty != null;
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
	public int hashCode()
	{
		int result = uuid.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Player)) return false;

		Player player = (Player) o;

		if (!uuid.equals(player.uuid)) return false;
		return name.equals(player.name);

	}

	@Override
	public String toString() { return name; }
}