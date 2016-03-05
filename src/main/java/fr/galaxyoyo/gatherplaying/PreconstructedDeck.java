package fr.galaxyoyo.gatherplaying;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PreconstructedDeck
{
	private transient Set set;
	private String name;
	private ManaColor[] colors;
	private List<Card> cards;

	public Set getSet() { return set; }

	public String getName() { return name; }

	public ManaColor[] getColors() { return colors; }

	public List<Card> getCards() { return cards; }

	public Deck buy(Player player)
	{
		Deck deck = new Deck();
		deck.owner = player;
		boolean alreadyFoiled = false;
		for (Card card : cards)
		{
			boolean foil = !alreadyFoiled && Utils.RANDOM.nextInt(60) == 42;
			alreadyFoiled |= foil;
			deck.cards.add(new OwnedCard(card, player, foil));
		}
		deck.calculateColors();
		deck.calculateLegalities();
		player.cards.addAll(StreamSupport.stream(deck.cards).collect(Collectors.toList()));
		MySQL.savePlayer(player);
		MySQL.saveDeck(deck);
		return deck;
	}

	public static void loadAll()
	{
		Gson gson = new GsonBuilder().registerTypeAdapter(Card.class, CardSerializer.CARD).registerTypeAdapter(ManaColor.class, CardSerializer.MANACOLOR).create();
		try
		{
			Map<String, String[]> names =
					gson.fromJson(IOUtils.toString(PreconstructedDeck.class.getResourceAsStream("/preconstructeds/preconstructeds.json"), StandardCharsets.UTF_8),
							new TypeToken<HashMap<String, String[]>>() {}.getType());
			for (Entry<String, String[]> entry : names.entrySet())
			{
				Set set = MySQL.getSet(entry.getKey());
				for (String name : entry.getValue())
				{
					PreconstructedDeck deck =
							gson.fromJson(IOUtils.toString(PreconstructedDeck.class.getResourceAsStream("/preconstructeds/" + name + ".json"), StandardCharsets.UTF_8),
									PreconstructedDeck.class);
					deck.set = set;
					set.preconstructeds.add(deck);
					System.out.println(deck.cards.size());
				}
			}
		}
		catch (JsonSyntaxException | IOException ex)
		{
			ex.printStackTrace();
		}
	}
}