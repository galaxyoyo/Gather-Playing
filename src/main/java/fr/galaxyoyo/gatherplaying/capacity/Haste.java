package fr.galaxyoyo.gatherplaying.capacity;

import fr.galaxyoyo.gatherplaying.PlayedCard;

public class Haste extends Capacity
{
	public Haste(PlayedCard card)
	{
		super(card);
		card.setSummoningSickness(false);
	}
}