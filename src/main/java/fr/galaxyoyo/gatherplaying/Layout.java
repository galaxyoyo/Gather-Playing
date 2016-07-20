package fr.galaxyoyo.gatherplaying;

public enum Layout
{
	NORMAL, LEVELER, SPLIT, DOUBLE_FACED, MELD, PLANE, SCHEME, FLIP, VANGUARD, TOKEN, PHENOMENON;

	public enum MeldPair
	{
		BRISELA(414304, 414319, 414305),
		CHITTERING_HOST(414391, 414386, 414392),
		HANWEIR(414428, 414511, 414429);

		private final Card first;
		private final Card second;
		private final Card result;

		MeldPair(int firstId, int secondId, int resultId)
		{
			first = MySQL.getCard(firstId);
			second = MySQL.getCard(secondId);
			result = MySQL.getCard(resultId);
		}

		public static MeldPair getMeldPair(Card card)
		{
			for (MeldPair pair : values())
			{
				if (pair.getFirst() == card || pair.getSecond() == card || pair.getResult() == card)
					return pair;
			}

			return null;
		}

		public Card getFirst()
		{
			return first;
		}

		public Card getSecond()
		{
			return second;
		}

		public Card getResult()
		{
			return result;
		}
	}
}