package fr.galaxyoyo.gatherplaying.rendering;

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
import java.util.regex.Pattern;

import static fr.galaxyoyo.gatherplaying.rendering.CardRenderer.ARTDIR;

@AllArgsConstructor
public class TokenRenderer extends Renderer
{
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

		File picDir = new File(ARTDIR, getToken().getSet().getCode());
		picDir.mkdirs();
		File artFile = new File(picDir, getToken().name().toLowerCase() + ".jpg");
	/*	if (!artFile.isFile())
		{
			BufferedImage art = ImageIO.read(new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + (getCard().getMuId("en")) + "&type=card")).getSubimage(18, 36,
					205 - 18, 173 - 36);
			ImageIO.write(art, "PNG", artFile);
		}*/

	/*	boolean isEldrazi =
				getCard().getType().is(CardType.CREATURE) && !getCard().getType().is(CardType.ARTIFACT) && (ArrayUtils.contains(getCard().getSubtypes(), SubType.valueOf("Eldrazi")) ||
						(getCard().getManaCost() != null && getCard().getManaCost().length <= 1 && getCard().getColors()[0] == ManaColor.COLORLESS));
		boolean devoid = getCard().getAbilityMap().get("en") != null && getCard().getAbilityMap().get("en").contains("Devoid");
		if (isEldrazi || devoid)
			drawArt(g, artFile, 21, 17, 955, 703);
		else if (frameDir.getName().equals("fullartbasicland") || getCard().getSet().getCode().equals("EXP"))
			drawArt(g, artFile, 103, 36, 842, 682);
		else*/
	//		drawArt(g, artFile, 107, 43, 569, 674);

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
		g.setFont(Fonts.TITLE);
		g.setColor(Color.ORANGE);
		drawText(g, (677 + titleX) / 2, (90 + g.getFont().getSize()) / 2, 677 - titleX, getToken().getTranslatedName().get(), true, true);
		g.setColor(Color.BLACK);
		System.out.print(".");

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
				if (Config.getLocaleCode().equals("fr"))
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
		else
		{
			int rarityLeft = drawRarity(g, Rarity.TOKEN, getToken().getSet(), 675, 731, 41, 76);
			drawText(g, typex, 743, rarityLeft - typex, type.toString(), false, false);
		}

		g.setColor(Color.BLACK);
		g.setColor(Color.BLACK);
		String legal = Config.getLocaleCode().equals("fr") ? getToken().getAbility_FR() : getToken().getAbility_EN();
		String legalTemp = legal.replace("#", "");

		if (getToken().getType().is(CardType.CREATURE))
		{
			g.setFont(Fonts.TEXT);
			if ((legalTemp.length() <= 40 ||
					Pattern.matches("(?u)(?s)([\\#]{0,1}[\\((]\\{T\\}[ \\::]{1,2}.*?\\{[WUBRG]\\}.*?\\{[WUBRG]\\}.*?[\\.?][\\))][\\#]{0,1})(?!.)", legalTemp)) &&
					!legalTemp.contains("\r"))
				drawText(g, 358, 910, 99999, legal, true, true);
			else
				drawLegalText(g, 777, 52, 920, 668, legal, 14);
		}
		else
		{
			g.setFont(Fonts.TEXT);
			if ((legalTemp.length() <= 40 ||
					Pattern.matches("(?u)(?s)([\\#]{0,1}[\\((]\\{T\\}[ \\::]{1,2}.*?\\{[WUBRG]\\}.*?\\{[WUBRG]\\}.*?[\\.?][\\))][\\#]{0,1})(?!.)", legalTemp)) &&
					!legalTemp.contains("\n"))
				drawText(g, 358, 915, 99999, legal, true, true);
			else
				drawLegalText(g, 777, 52, 930, 668, legal, 0);
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
