package fr.galaxyoyo.gatherplaying.markers;

import fr.galaxyoyo.gatherplaying.PlayedCard;
import org.jetbrains.annotations.NotNull;

public abstract class Marker
{
	public abstract void onCardMarked(PlayedCard card);

	public abstract void onCardUnmarked(PlayedCard card);

	@Override
	public String toString() { return getType().getTranslatedName(); }

	public abstract MarkerType getType();

	@Override
	public int hashCode()
	{
		return getType().hashCode();
	}

	@Override
	public boolean equals(@NotNull Object o)
	{
		return o == this || o.getClass() == getClass() && getType() == ((Marker) o).getType();
	}
}