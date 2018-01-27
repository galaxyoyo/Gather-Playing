package fr.galaxyoyo.gatherplaying.rendering;

import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Config;
import java8.util.stream.Collectors;
import java8.util.stream.RefStreams;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

import static fr.galaxyoyo.gatherplaying.rendering.CardRenderer.ARTDIR;

@AllArgsConstructor
public class TokenRenderer extends Renderer
{
	private static final Map<Character, BufferedImage> TITLE_FONT = Maps.newHashMap();

	static
	{
		File FONT_DIR = new File(DIR, "images/symbols/tokenfont");
		char[] special_chars = new char[] {'é', 'æ', '&', '\'', 'ç', 'â', 'ê', 'î', 'ô', 'û', ':', ',', '-', 'ä', 'ë', 'ï', 'ö', 'ü', 'ÿ', '”', '“', 'Ð', '!', 'à', 'è', 'ù',
			'«', 'œ', '?', '¿', '»', 'ß', '‘', 'ø', ' ', 'Ł', 'Þ'};
		String[] special_names = new String[] {"acuteE", "ae", "ampersand", "apostrophe", "cedillaC", "circumA", "circumE", "circumI", "circumO", "circumU", "colon", "comma", "dash",
				"diaeresisA", "diaeresisE", "diaeresisI", "diaeresisO", "diaeresisU", "diaeresisY", "doublequoteclose", "doublequoteopen", "eth", "exclamation", "graveA", "graveE",
				"graveU", "leftdoublearrow", "oe", "question", "questioninverted", "rightdoublearrow", "sharps", "singlequoteopen", "slashO", "space", "strokeL", "thorne"};
		try
		{
			for (char c = 'a'; c <= 'z'; ++c)
				TITLE_FONT.put(c, ImageIO.read(new File(FONT_DIR, c + ".png")));
			for (int i = 0; i < special_chars.length; ++i)
			{
				System.out.println(special_chars[i] + ", " + special_names[i]);
				TITLE_FONT.put(special_chars[i], ImageIO.read(new File(FONT_DIR, special_names[i] + ".png")));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Getter
	private final Token token;

	@Override
	public BufferedImage render() throws IOException
	{
		String language = Config.getLocaleCode();

		System.out.print("\n" + getToken().getEnglishName() + " ...");
		File frameDir = getFrameDir();

		boolean useMulticolorFrame = true;

		BufferedImage img = new BufferedImage(720, 1020, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(frameDir.getName().equals("transform-night") ? Color.WHITE : Color.BLACK);

		File picDir = new File(ARTDIR, getToken().getSet().getCode() + "/tokens");
		picDir.mkdirs();
		File artFile = new File(picDir, getToken().name().toLowerCase() + ".jpg");
		if (!artFile.isFile())
		{
			BufferedImage art = ImageIO.read(new URL("http://cartes.mtgfrance.com/images/cards/en/token/" + token.getSet().getMagicCardsInfoCode().toLowerCase() + "/"
					+ token.getNumber() + ".jpg"));
			ImageIO.write(art, "JPG", artFile);
		}
		g.drawImage(ImageIO.read(artFile), 0, 0, 720, 1020, null);

		System.out.print(".");

		String holoFoil = "";
		if (getToken().getSet().getType().equals("special"))
			holoFoil = "_H";

		BufferedImage borderImage = null;
		BufferedImage bgImage;
		String costColors;

		{
			ManaColor[] cost = getToken().getColor();
			if (cost == null)
				cost = new ManaColor[0];
			costColors = String.join("", RefStreams.of(cost).filter(color -> !color.name().contains("NEUTRAL")).map(ManaColor::getAbbreviate).distinct().collect(Collectors.toList()));
			if (!useMulticolorFrame && costColors.length() >= 2)
				costColors = "Gld";
			else if (costColors.isEmpty())
				costColors = "Art";
			if (getToken().getType().is(CardType.EMBLEM))
				costColors = "Emblem";
			else if (getToken().getType().is(CardType.MONARCH))
				costColors = "Monarch";
			if (costColors.equals("Art") || getToken().getType().is(CardType.ARTIFACT))
			{
				bgImage = readImage(new File(frameDir, "cards/Art" + holoFoil + ".png"));
				if (!StringUtils.isAllUpperCase(costColors) || costColors.length() <= 2)
					borderImage = readImage(new File(frameDir, "borders/" + costColors + holoFoil + ".png"));
				else if (costColors.length() > 2 && StringUtils.isAllUpperCase(costColors))
					borderImage = readImage(new File(frameDir, "borders/Gld" + holoFoil + ".png"));
			}
			else
			{
				bgImage = readImage(new File(frameDir, "cards/" + costColors + holoFoil + ".png"));
				if (costColors.length() == 2)
					borderImage = readImage(new File(frameDir, "borders/" + costColors + holoFoil + ".png"));
			}
		}

		if (bgImage != null)
			g.drawImage(bgImage, 0, 0, 720, 1020, null);
		if (borderImage != null)
			g.drawImage(borderImage, 0, 0, 720, 1020, null);

		if (getToken().getType().is(CardType.CREATURE))
		{
			BufferedImage pt;
			if (costColors.length() > 1)
				pt = readImage(new File(frameDir, "pt/Gld.png"));
			else
				pt = readImage(new File(frameDir, "pt/" + costColors + ".png"));

			if (pt != null)
				g.drawImage(pt, 0, 0, 720, 1020, null);
			g.setFont(Fonts.PT);
			drawText(g, 629, 938, 95, getToken().getPower() + "/" + getToken().getToughness(), true, true);
		}

		int titleX = 51;
		int titleY = 70;
		if (getToken().getType().is(CardType.MONARCH))
			titleY = 675;
		drawTitle(g, (677 + titleX) / 2, titleY, getToken().getType() == CardType.EMBLEM ? CardType.EMBLEM.getTranslatedName().get() : getToken().getTranslatedName().get());
		System.out.print(".");

		g.setFont(Fonts.TITLE);
		int typex = 51;
		StringBuilder type = new StringBuilder(getToken().getType().getTranslatedName().get());
		if (getToken().isLegendary())
		{
			if (Config.getLocaleCode().equals("fr"))
				type.append(" légendaire");
			else
				type = new StringBuilder("Legendary ").append(type);
		}
		if (getToken().getSubtypes() != null && getToken().getSubtypes().length > 0)
		{
			type.append(Config.getLocaleCode().equals("fr") ? " : " : " — ");
			for (SubType st : getToken().getSubtypes())
			{
				String name = st.getTranslatedName().get();
				if (Config.getLocaleCode().equals("fr") && getToken().getType() != CardType.EMBLEM)
					name = name.toLowerCase();
				type.append(name).append(Config.getLocaleCode().equals("fr") ? " et " : " ");
			}
			type = new StringBuilder(type.substring(0, type.length() - (Config.getLocaleCode().equals("fr") ? 4 : 1)));
		}

		System.out.print(".");
		if (frameDir.getName().equals("m15token"))
		{
			int rarityLeft = drawRarity(g, Rarity.TOKEN, getToken().getSet(), 675, 867, 41, 76);
			drawText(g, typex, 880, rarityLeft - typex, type.toString(), false, false);
		}
		else if (frameDir.getName().equals("m15emblem"))
		{
			int rarityLeft = drawRarity(g, Rarity.TOKEN, getToken().getSet(), 675, 731, 41, 76);
			drawText(g, typex, 743, rarityLeft - typex, type.toString(), false, false);
		}
		else if (frameDir.getName().equals("m15tokentext"))
		{
			int rarityLeft = drawRarity(g, Rarity.TOKEN, getToken().getSet(), 675, 721, 41, 76);
			drawText(g, typex, 733, rarityLeft - typex, type.toString(), false, false);
		}
		else if (frameDir.getName().equals("monarch"))
			drawRarity(g, Rarity.TOKEN, getToken().getSet(), 675, 675, 41, 76);
		else
		{
			int rarityLeft = drawRarity(g, Rarity.TOKEN, getToken().getSet(), 675, 604, 41, 76);
			drawText(g, typex, 616, rarityLeft - typex, type.toString(), false, false);
		}

		g.setColor(Color.BLACK);
		String legal = Config.getLocaleCode().equals("fr") ? getToken().getAbility_FR() : getToken().getAbility_EN();
		String legalTemp = legal.replace("#", "");

		g.setFont(Fonts.TEXT);
		if (getToken().getType().is(CardType.EMBLEM))
		{
			if ((legalTemp.length() <= 40 ||
					Pattern.matches("(?u)(?s)([\\#]{0,1}[\\((]\\{T\\}[ \\::]{1,2}.*?\\{[WUBRG]\\}.*?\\{[WUBRG]\\}.*?[\\.?][\\))][\\#]{0,1})(?!.)", legalTemp)) &&
					!legalTemp.contains("\n"))
				drawText(g, 358, 915, 99999, legal, true, true);
			else
				drawLegalText(g, 777, 52, 930, 668, legal, 0);
		}
		else if (frameDir.getName().equals("m15tokentext"))
		{
			if ((legalTemp.length() <= 40 ||
					Pattern.matches("(?u)(?s)([\\#]{0,1}[\\((]\\{T\\}[ \\::]{1,2}.*?\\{[WUBRG]\\}.*?\\{[WUBRG]\\}.*?[\\.?][\\))][\\#]{0,1})(?!.)", legalTemp)) &&
					!legalTemp.contains("\n"))
				drawText(g, 358, 777 + (910 - 777) / 2, 99999, legal, true, true);
			else
				drawLegalText(g, 777, 52, 920, 668, legal, 14);
		}
		else if (frameDir.getName().equals("monarch"))
			drawLegalText(g, 710, 52, 930, 668, legal, 0);
		else
		{
			if ((legalTemp.length() <= 40 ||
					Pattern.matches("(?u)(?s)([\\#]{0,1}[\\((]\\{T\\}[ \\::]{1,2}.*?\\{[WUBRG]\\}.*?\\{[WUBRG]\\}.*?[\\.?][\\))][\\#]{0,1})(?!.)", legalTemp)) &&
					!legalTemp.contains("\n"))
				drawText(g, 358, 783, 99999, legal, true, true);
			else
				drawLegalText(g, 647, 52, 920, 668, legal, 14);
		}

		g.setColor(Color.WHITE);
		g.setFont(Fonts.COLLECTION);

		StringBuilder collectorNumber = new StringBuilder(getToken().getNumber() + "/");
		while (collectorNumber.length() < 4)
			collectorNumber.insert(0, "0");
		int max = (int) RefStreams.of(Token.values()).filter(t -> t.getSet() == getToken().getSet()).count();
		collectorNumber.append(max < 10 ? "00" + max : max < 100 ? "0" + max : "" + max);

		String collectionTxtL1 = collectorNumber.toString();
		String collectionTxtL2 = getToken().getSet().getCode() + " • " + language.toUpperCase() + " ";

		drawText(g, 37, 977, 99999, collectionTxtL1 + "\n" + collectionTxtL2 + "{brush2}", false, false);
		int w = (int) getStringWidth(collectionTxtL2, g.getFont());
		drawText(g, 40 + w, 977, 99999, "T", false, false);
		g.setFont(Fonts.ARTIST);
		drawText(g, 64 + w, 996, 99999, "" /*getToken().getArtist()*/, false, false);

		String copyright = "GP ™ & © 2018 Wizards of the Coast";
		g.setFont(Fonts.COPYRIGHT);
		drawText(g, 680 - (int) getStringWidth(copyright, g.getFont()), getToken().getType().is(CardType.CREATURE) ? 996 : 977, 99999, copyright, false,
				false);

		return img;
	}
	public void drawTitle(Graphics2D g, int left, int baseline, String title)
	{
		title = title.trim();
		BufferedImage[] letters = new BufferedImage[title.length()];
		int width = 0, height = 0;
		for (int i = 0; i < letters.length; ++i)
		{
			letters[i] = TITLE_FONT.get(Character.toLowerCase(title.charAt(i)));
			if (letters[i] == null)
				System.out.println(title.charAt(i));
			double m = Character.isUpperCase(title.charAt(i)) || title.charAt(i) == '-' ? 1 : 0.8;
			width += letters[i].getWidth() * m;
			height = Math.max(height, (int) (letters[i].getHeight() * m));
		}
		left -= width / 2;
		baseline -= height / 2;

		if (getToken().getType() == CardType.EMBLEM)
			baseline += 4;

		int x = left;
		for (int i = 0; i < letters.length; i++)
		{
			BufferedImage letter = letters[i];
			double m = Character.isUpperCase(title.charAt(i)) || title.charAt(i) == '-' ? 1 : 0.8;
			if (getToken().getType() == CardType.EMBLEM)
			{
				BufferedImage newLetter = new BufferedImage(letter.getWidth(), letter.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
				Graphics2D g2D = newLetter.createGraphics();
				g2D.drawImage(letter, 0, 0, null, null);
				letter = newLetter;
				m *= 0.95;
			}
			int w = (int) (letters[i].getWidth() * m);
			int h = (int) (height * m);
			g.drawImage(letter, x, (int) (baseline + height * (Character.isUpperCase(title.charAt(i)) || title.charAt(i) == '-' ? 0 : 0.15)), w, h, null, null);
			x += w;
		}
	}

	public void drawLegalText(Graphics2D g, int top, int left, int bottom, int right, String legal, int heightAdjust)
	{
		String text = legal.replace("\n", "\n\n");
		drawTextWrappedAndScaled(g, top, left, bottom, right, text, g.getFont(), heightAdjust);
	}

	public int drawRarity(Graphics2D g, Rarity rarity, Set set, int right, int middle, int height, int width)
	{
		String[] preEXOSet = {"LEA", "LEB", "2ED", "ARN", "ATQ", "3ED", "LEG", "DRK", "FEM", "4ED", "ICE", "ALL", "HML", "CHR", "5ED", "EXO", "MIR", "POR", "STH", "TMP", "VIS", "WTH"};
		if (rarity == Rarity.TOKEN || rarity == Rarity.BASIC_LAND)
			rarity = Rarity.COMMON;
		try
		{
			BufferedImage image = ImageIO.read(new File(DIR, "images/rarity/" + set.getCode() + "_" + rarity.name().charAt(0) + ".png"));
			int destWidth = image.getWidth();
			int destHeight = image.getHeight();
			if (destHeight > height)
			{
				destWidth *= (double) height / (double) destHeight;
				destHeight = height;
			}
			if (destWidth > width)
			{
				destHeight *= (double) width / (double) destWidth;
				destWidth = width;
			}

			if (g != null)
				g.drawImage(image, right - destWidth, middle - destHeight / 2, destWidth, destHeight, null);
			return right - destWidth - 5;
		}
		catch (IOException ignored)
		{
		}
		return right;
	}

	@Override
	public File getFrameDir()
	{
		File dir = new File(DIR, "images/token");
		String dirName = "m15token";
		if (getToken().getEnglishName().toLowerCase().contains("clue"))
			dirName = "m15clue";
		else if (getToken().getType().is(CardType.EMBLEM))
			dirName = "m15emblem";
		else if (getToken().getType() == CardType.MONARCH)
			dirName = "monarch";
		else if (getToken().getAbility_EN().length() >= 120)
			dirName = "m15tokenlargetextbox";
		else if (!getToken().getAbility_EN().isEmpty())
			dirName = "m15tokentext";
		return new File(dir, dirName);
	}

	@Override
	public File getOutputFile()
	{
		return new File(DIR, "output/" + getToken().getSet().getCode() + "/tokens/" + getToken().name().toLowerCase().replace('_', ' ') + ".jpg");
	}
}
