package fr.galaxyoyo.gatherplaying.markers;

import fr.galaxyoyo.gatherplaying.PlayedCard;

public class MarkerPlus1Plus1 extends Marker
{
	@Override
	public void onCardMarked(PlayedCard card)
	{
		if (card.getPower() == Integer.MIN_VALUE)
			card.setPower(0);
		if (card.getToughness() == Integer.MIN_VALUE)
			card.setToughness(0);
		card.setPower(card.getPower() + 1);
		card.setToughness(card.getToughness() + 1);
	}

	@Override
	public void onCardUnmarked(PlayedCard card)
	{
		card.setPower(card.getPower() - 1);
		card.setToughness(card.getToughness() - 1);
	}

	@Override
	public MarkerType getType() { return MarkerType.PLUS1PLUS1; }
}