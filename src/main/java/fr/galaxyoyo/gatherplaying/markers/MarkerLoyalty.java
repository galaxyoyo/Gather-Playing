package fr.galaxyoyo.gatherplaying.markers;

import fr.galaxyoyo.gatherplaying.PlayedCard;

public class MarkerLoyalty extends Marker
{
	@Override
	public void onCardMarked(PlayedCard card) { card.loyalty.set(card.loyalty.get() + 1); }

	@Override
	public void onCardUnmarked(PlayedCard card) { card.loyalty.set(card.loyalty.get() - 1); }

	@Override
	public MarkerType getType() { return MarkerType.LOYALTY; }
}