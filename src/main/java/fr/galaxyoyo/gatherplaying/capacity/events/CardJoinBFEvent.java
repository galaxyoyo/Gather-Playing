package fr.galaxyoyo.gatherplaying.capacity.events;

import fr.galaxyoyo.gatherplaying.PlayedCard;

public class CardJoinBFEvent extends Event
{
	private PlayedCard joining;

	public CardJoinBFEvent(PlayedCard card)
	{
		this.joining = card;
	}

	public PlayedCard getJoiningCard()
	{
		return joining;
	}
}