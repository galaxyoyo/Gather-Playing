package fr.galaxyoyo.gatherplaying.markers;

import fr.galaxyoyo.gatherplaying.PlayedCard;

public abstract class Marker
{
	public abstract void onCardMarked(PlayedCard card);

	public abstract void onCardUnmarked(PlayedCard card);

	@Override
	public String toString() { return getType().getTranslatedName(); }

	public abstract MarkerType getType();
}