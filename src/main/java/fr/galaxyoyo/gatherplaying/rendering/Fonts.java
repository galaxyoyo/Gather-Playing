package fr.galaxyoyo.gatherplaying.rendering;

import java.awt.*;
import java.io.File;

import static fr.galaxyoyo.gatherplaying.rendering.Renderer.DIR;

@SuppressWarnings("ConstantConditions")
public class Fonts
{
	public static final Font TEXT = getFont("MPlantin", 34);
	public static final Font TEXT_ITALIC = getFont("MPlantinI", 34);
	public static final Font TITLE = getFont("JaceBeleren-Bold", 36).deriveFont(Font.BOLD);
	public static final Font TYPE = TITLE;
	public static final Font PT = getFont("BelerenBoldSmallCaps", 42).deriveFont(Font.BOLD);
	public static final Font COLLECTION = getFont("GothamMedium", 18);
	public static final Font ARTIST = getFont("BelerenBoldSmallCaps", 19).deriveFont(Font.BOLD);
	public static final Font COPYRIGHT = getFont("MPlantin", 14);
	public static final Font LOYALTY_STARTING = getFont("BelerenBoldSmallCaps", 38).deriveFont(Font.BOLD);
	public static final Font LOYALTY_CHANGE = getFont("MPlantinB", 32).deriveFont(Font.BOLD);

	private static Font getFont(String file, int size)
	{
		try
		{
			return Font.createFont(Font.TRUETYPE_FONT, new File(DIR, "fonts/" + file + (file.endsWith(".ttf") ? "" : ".ttf"))).deriveFont((float) size);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static Font getItalic(Font font)
	{
		if (font.getName().equals(TEXT.getName()))
			return TEXT_ITALIC.deriveFont(font.getSize2D());
		return font.deriveFont(Font.ITALIC);
	}

	public static Font getPlain(Font font)
	{
		if (font.getName().equals(TEXT_ITALIC.getName()))
			return TEXT.deriveFont(font.getSize2D());
		return font.deriveFont(Font.PLAIN);
	}
}
