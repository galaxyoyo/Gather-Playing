package fr.galaxyoyo.gatherplaying.markers;

import fr.galaxyoyo.gatherplaying.PlayedCard;

public class MarkerPlus1Plus1 extends Marker
{
	@Override
	public void onCardMarked(PlayedCard card)
	{
		if (card.power.get() == Integer.MIN_VALUE)
			card.power.set(0);
		if (card.toughness.get() == Integer.MIN_VALUE)
			card.toughness.set(0);
		card.power.set(card.power.get() + 1);
		card.toughness.set(card.toughness.get() + 1);
	}

	@Override
	public void onCardUnmarked(PlayedCard card)
	{
		card.power.set(card.power.get() - 1);
		card.toughness.set(card.toughness.get() - 1);
	}

	@Override
	public MarkerType getType() { return MarkerType.PLUS1PLUS1; }
}