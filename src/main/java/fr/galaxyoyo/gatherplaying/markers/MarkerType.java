package fr.galaxyoyo.gatherplaying.markers;

import fr.galaxyoyo.gatherplaying.CardType;

public enum MarkerType
{
	PLUS1PLUS1(MarkerPlus1Plus1.class, "+1/+1", "+1/+1", "+1/+1", "+1/+1", CardType.CREATURE),
	MINUS1MINUS1(MarkerMinus1Minus1.class, "-1/-1", "-1/-1", "-1/-1", "-1/-1", CardType.CREATURE),
	LOYALTY(MarkerLoyalty.class, "Loyalty", "Loyauté", "", "", CardType.PLANESWALKER);
	private final Class<? extends Marker> clazz;
	public final String name_EN, name_FR, name_DE, name_IT;
	private final CardType[] applicables;

	MarkerType(Class<? extends Marker> clazz, String name_EN, String name_FR, String name_DE, String name_IT, CardType... applicables)
	{
		this.clazz = clazz;
		this.name_EN = name_EN;
		this.name_FR = name_FR;
		this.name_DE = name_DE;
		this.name_IT = name_IT;
		this.applicables = applicables;
	}

	@SuppressWarnings("unchecked")
	public <T extends Marker> T newInstance()
	{
		try
		{
			return (T) clazz.newInstance();
		}
		catch (InstantiationException | IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public boolean isApplicable(CardType type)
	{
		for (CardType ct : applicables)
		{
			if (type.is(ct))
				return true;
		}
		return false;
	}

	public String getTranslatedName() { return name_FR; }
}