package fr.galaxyoyo.gatherplaying.markers;

import fr.galaxyoyo.gatherplaying.PlayedCard;

public abstract class Marker
{
	public abstract void onCardMarked(PlayedCard card);

	public abstract void onCardUnmarked(PlayedCard card);

	public abstract MarkerType getType();

	@Override
	public String toString() { return getType().getTranslatedName(); }
}