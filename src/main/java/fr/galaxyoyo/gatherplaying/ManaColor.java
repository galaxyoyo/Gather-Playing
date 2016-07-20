package fr.galaxyoyo.gatherplaying;

import fr.galaxyoyo.gatherplaying.client.I18n;
import javafx.beans.binding.StringBinding;

public enum ManaColor
{
	RED("R"), GREEN("G"), BLUE("U"), WHITE("W"), BLACK("B"), COLORLESS("C"), RED_PLAYER("R/P"), GREEN_PLAYER("G/P"), BLUE_PLAYER("U/P"), WHITE_PLAYER("W/P"),
	BLACK_PLAYER("B/P"), B_R("B/R"), B_G("B/G"), R_G("R/G"), R_W("R/W"), G_W("G/W"), G_U("G/U"), W_B("W/B"), W_U("W/U"), U_R("U/R"), U_B("U/B"), B_2("2/B"), R_2("2/R"),
	G_2("2/G"), W_2("2/W"), U_2("2/U"), HALF_RED("HalfR"), HALF_WHITE("HalfW"), NEUTRAL_X("X"), NEUTRAL_Y("Y"), NEUTRAL_Z("Z"), NEUTRAL_0("0"), NEUTRAL_1("1"), NEUTRAL_2("2"),
	NEUTRAL_3("3"), NEUTRAL_4("4"), NEUTRAL_5("5"), NEUTRAL_6("6"), NEUTRAL_7("7"), NEUTRAL_8("8"), NEUTRAL_9("9"), NEUTRAL_10("10"), NEUTRAL_11("11"), NEUTRAL_12("12"),
	NEUTRAL_13("13"), NEUTRAL_14("14"), NEUTRAL_15("15"), NEUTRAL_16("16"), NEUTRAL_20("20"), NEUTRAL_1000000("1000000"), NEUTRAL_INFINITY("infinity");
	private final String symbol;

	ManaColor(String symbol) { this.symbol = symbol; }

	public static ManaColor getBySignificant(String symbol)
	{
		for (ManaColor mc : values())
		{
			if (mc.symbol.toUpperCase().equals(symbol.toUpperCase()))
				return mc;
		}
		return null;
	}

	public String getAbbreviate() { return symbol; }

	@Override
	public String toString() { return getTranslatedName().get(); }

	public StringBinding getTranslatedName() { return I18n.tr("color." + name().toLowerCase()); }
}