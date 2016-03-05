package fr.galaxyoyo.gatherplaying.capacity;

import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.capacity.events.CardJoinBFEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class Capacity
{
	private final PlayedCard card;

	public Capacity(PlayedCard card)
	{
		this.card = card;
	}

	public static ObservableList<Capacity> guessCapacities(PlayedCard card)
	{
		String enAbility = card.getCard().ability.get("en");
		if (enAbility == null || enAbility.isEmpty())
			return FXCollections.emptyObservableList();
		ObservableList<Capacity> capacities = FXCollections.emptyObservableList();;
		for (String ability : enAbility.split("£|\n"))
		{
			if (ability.startsWith("Haste"))
				capacities.add(new Haste(card));
		}
		return capacities;
	}

	public void onCardJoinBattlefield(CardJoinBFEvent event) {}

	public PlayedCard getCard()
	{
		return card;
	}
}