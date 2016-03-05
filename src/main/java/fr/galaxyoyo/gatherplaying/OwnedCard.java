package fr.galaxyoyo.gatherplaying;

import javafx.beans.binding.StringBinding;

public class OwnedCard implements Cloneable
{
	private final Card card;
	private final Player owner;
	private final boolean foil;

	public OwnedCard(Card card, Player owner, boolean foil)
	{
		this.card = card;
		this.owner = owner;
		this.foil = foil;
	}

	public final Card getCard() { return card; }

	public final Player getOwner() { return owner; }

	public final boolean isFoiled() { return foil; }

	@Override
	public boolean equals(Object obj)
	{
		return this == obj || obj instanceof OwnedCard && owner == ((OwnedCard) obj).owner && card == ((OwnedCard) obj).card && foil == ((OwnedCard) obj).foil;
	}

	@Override
	public OwnedCard clone()
	{
		try
		{
			return (OwnedCard) super.clone();
		}
		catch (CloneNotSupportedException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public StringBinding getTranslatedName()
	{
		return card.getTranslatedName();
	}

	@Override
	public String toString()
	{
		return getTranslatedName().get();
	}
}