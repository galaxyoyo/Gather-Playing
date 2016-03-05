package fr.galaxyoyo.gatherplaying.capacity;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.SubType;

public class Changeling extends Capacity
{
	public Changeling(PlayedCard card)
	{
		super(card);
		card.subtypes.addAll(Lists.newArrayList(SubType.values()));
	}
}