package fr.galaxyoyo.gatherplaying.rendering;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Config;
import java8.util.stream.Collectors;
import java8.util.stream.RefStreams;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class M15Renderer extends CardRenderer
{
	protected M15Renderer(Card card)
	{
		super(card);
	}

	@Override
	public BufferedImage render() throws IOException
	{
		String language = Config.getLocaleCode();

		System.out.print("\n" + getCardName() + " ...");
		File frameDir = getFrameDir();

		boolean useMulticolorFrame = getCard().getLayout() == Layout.DOUBLE_FACED || getCard().getLayout() == Layout.SPLIT;

		BufferedImage img = new BufferedImage(720, 1020, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(frameDir.getName().equals("transform-night") ? Color.WHITE : Color.BLACK);

		File picDir = new File(ARTDIR, getCard().getSet().getCode());
		picDir.mkdirs();
		File artFile = new File(picDir, getCard().getImageName() + ".jpg");
		if (!artFile.isFile())
		{
			BufferedImage art = ImageIO.read(new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + (getCard().getMuId("en")) + "&type=card")).getSubimage(18, 36,
					205 - 18, 173 - 36);
			ImageIO.write(art, "JPG", artFile);
		}

		boolean isEldrazi =
				getCard().getType().is(CardType.CREATURE) && !getCard().getType().is(CardType.ARTIFACT) && (ArrayUtils.contains(getCard().getSubtypes(), SubType.valueOf("Eldrazi")) ||
						(getCard().getManaCost() != null && getCard().getManaCost().length <= 1 && getCard().getColors()[0] == ManaColor.COLORLESS));
		boolean devoid = getCard().getAbilityMap().get("en") != null && getCard().getAbilityMap().get("en").contains("Devoid");
		if (isEldrazi || devoid)
			drawArt(g, artFile, 21, 17, 955, 703);
		else if (frameDir.getName().equals("fullartbasicland") || getCard().getSet().getCode().equals("EXP"))
			drawArt(g, artFile, 103, 36, 842, 682);
		else
			drawArt(g, artFile, 107, 43, 569, 674);

		System.out.print(".");

		String holoFoil = "";
		if ((getCard().getRarity() == Rarity.RARE || getCard().getRarity() == Rarity.MYTHIC || getCard().getRarity() == Rarity.SPECIAL) && !getCard().getNumber().endsWith("b"))
			holoFoil = "_H";

		BufferedImage borderImage = null;
		BufferedImage greyTitleAndTypeOverlay = null;
		BufferedImage bgImage;
		String costColors = "", landColors = "";

		if (getCard().getType().is(CardType.LAND))
		{
			landColors = ImageWriter.getLandColors(getCard());
			if (landColors == null)
			{
				System.out.println("Warning: land colors not found for " + getCardName());
				landColors = "C";
			}

			String landColorsTemp = landColors + holoFoil;
			bgImage = readImage(new File(frameDir, "land/" + landColorsTemp + ".png"));

			if (getCard().getName().get("en").toLowerCase().equals("murmuring bosk"))
				greyTitleAndTypeOverlay = readImage(new File(frameDir, "land/G-overlay.png"));
		}
		else
		{
			ManaColor[] cost = getCard().getManaCost();
			if (getCard().getMuId("en") == 414497)
				cost = new ManaColor[] {ManaColor.RED, ManaColor.GREEN};
			else if (getCard().getLayout() == Layout.DOUBLE_FACED && getCard().getNumber().endsWith("b"))
				cost = getCard().getColors();
			if (cost == null)
				cost = new ManaColor[0];
			costColors = String.join("", RefStreams.of(cost).filter(color -> !color.name().contains("NEUTRAL")).map(ManaColor::getAbbreviate).distinct().collect(Collectors.toList())).replace("/P", "");
			switch (costColors)
			{
				case "WG":
					costColors = "GW";
					break;
				case "WR":
					costColors = "RW";
					break;
			}
			if (!useMulticolorFrame && costColors.length() >= 2)
				costColors = "Gld";
			else if (devoid || isEldrazi)
				costColors = "C";
			else if (costColors.isEmpty())
				costColors = "Art";
			if (getCard().getType().is(CardType.CONSPIRACY))
				bgImage = readImage(new File(frameDir, "cards/Conspiracy.png"));
			else if (isEldrazi && !devoid)
				bgImage = readImage(new File(frameDir, "cards/Eldrazi" + holoFoil + ".png"));
			else if (devoid && cost.length >= 2)
				bgImage = readImage(new File(frameDir, "cards/Devoid_Gld" + holoFoil + ".png"));
			else if (devoid)
				bgImage = readImage(new File(frameDir, "cards/Devoid_" + costColors + holoFoil + ".png"));
			else if (costColors.equals("Art") || getCard().getType().is(CardType.ARTIFACT))
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
		if (greyTitleAndTypeOverlay != null)
			g.drawImage(greyTitleAndTypeOverlay, 0, 0, 720, 1020, null);

		if (getCard().getType().is(CardType.CREATURE))
		{
			BufferedImage pt;
			if (costColors.length() > 1 && !costColors.equals("Art"))
				pt = readImage(new File(frameDir, "pt/Gld.png"));
			else
				pt = readImage(new File(frameDir, "pt/" + costColors + ".png"));

			if (pt != null)
				g.drawImage(pt, 0, 0, 720, 1020, null);
			g.setFont(Fonts.PT);
			drawText(g, 629, 938, 95, getCard().getPower() + "/" + getCard().getToughness(), true, true);
		}

		if (frameDir.getName().startsWith("transform-") && getCard().getNumber().endsWith("a"))
		{
			Card back;
			if (Layout.MeldPair.getMeldPair(getCard()) != null)
				//noinspection ConstantConditions
				back = Layout.MeldPair.getMeldPair(getCard()).getResult();
			else
				back = MySQL.getCard(getCard().getMuId("en") + 1);

			if (back.getType().is(CardType.CREATURE))
			{
				Color oldColor = g.getColor();
				g.setColor(new Color(127, 127, 127));
				g.setFont(Fonts.PT.deriveFont(80.0F));
				drawText(g, 650, 885, 50, back.getPower() + "/" + back.getToughness(), true, true);
				g.setColor(oldColor);
			}
		}

		int costLeft = drawCastingCost(g, getCard().getManaCost(), costColors.length() >= 2 && StringUtils.isAllUpperCase(costColors) ? 53 : 51, 677, 35);
		System.out.print(".");
		int rarityLeft = drawRarity(g, getCard().getRarity(), getCard().getSet(), 675, frameDir.getName().equals("fullartbasicland") ? 872 : 604, 41, 76);

		int titleX = frameDir.getName().startsWith("transform-") ? 110 : 51;
		g.setFont(Fonts.TITLE);
		drawText(g, titleX, frameDir.getName().startsWith("transform-") ? 82 : 84, costLeft - 20 - titleX, getCard().getTranslatedName().get(), false, false);
		System.out.print(".");

		g.setFont(Fonts.TYPE);
		if (Pattern.matches("BFZ|OGW|ZEN|UNH|UGL|UST", getCard().getSet().getCode()) && frameDir.getName().equals("fullartbasicland"))
		{
			drawText(g, 51, 885, 439, "Terrain de base", false, false);
			if (!getCard().getName().get("en").equals("Wastes"))
				drawText(g, 490, 885, rarityLeft - 490, getCard().getSubtypes()[0].getTranslatedName().get(), false, false);
		}
		else
		{
			int typex = frameDir.getName().equals("transform-night") ? 85 : 51;
			StringBuilder type = new StringBuilder(getCard().getType().getTranslatedName().get());
			if (getCard().isLegendary())
			{
				if (Config.getLocaleCode().equals("fr"))
					type.append(" légendaire");
				else
					type = new StringBuilder("Legendary ").append(type);
			}
			if (getCard().getSubtypes() != null && getCard().getSubtypes().length > 0)
			{
				type.append(Config.getLocaleCode().equals("fr") ? " : " : " — ");
				for (SubType st : getCard().getSubtypes())
				{
					String name = st.getTranslatedName().get();
					if (Config.getLocaleCode().equals("fr") && !getCard().getType().is(CardType.PLANESWALKER))
						name = name.toLowerCase();
					type.append(name).append(Config.getLocaleCode().equals("fr") ? " et " : " ");
				}
				type = new StringBuilder(type.substring(0, type.length() - (Config.getLocaleCode().equals("fr") ? 4 : 1)));
			}
			drawText(g, typex, 616, rarityLeft - typex, type.toString(), false, false);
		}

		g.setColor(Color.BLACK);
		String legal = getCard().getAbilityMap().get("en") == null ? "" : getCard().getAbility();
		String legalTemp = legal.replace("#", "");

		if (getCard().isBasic())
		{
			BufferedImage image = readImage(new File(DIR, "images/symbols/land/" + landColors + ".png"));
			if (image != null && frameDir.getName().equals("fullartbasicland"))
			{
				int inputWidth = 140;
				int inputHeight = 140;
				int outputWidth, outputHeight;
				if (image.getWidth() > image.getHeight())
				{
					outputWidth = inputWidth;
					outputHeight = (int) (inputWidth * (double) image.getHeight() / (double) image.getWidth());
				}
				else if (image.getWidth() < image.getHeight())
				{
					outputWidth = (int) (inputHeight * (double) image.getWidth() / (double) image.getHeight());
					outputHeight = inputHeight;
				}
				else
				{
					outputWidth = inputWidth;
					outputHeight = inputHeight;
				}
				g.drawImage(image, 363 - outputWidth / 2, 795, outputWidth, outputHeight, null);
			}
			else if (image != null)
				g.drawImage(image, 363 - image.getWidth() / 2, 645, null);
		}
		else if (getCard().getType().is(CardType.CREATURE))
		{
			g.setFont(Fonts.TEXT);
			if ((legalTemp.length() <= 40 ||
					Pattern.matches("(?u)(?s)([\\#]{0,1}[\\((]\\{T\\}[ \\::]{1,2}.*?\\{[WUBRG]\\}.*?\\{[WUBRG]\\}.*?[\\.?][\\))][\\#]{0,1})(?!.)", legalTemp)) &&
					!legalTemp.contains("\r") && getCard().getFlavorMap().get("en").isEmpty())
				drawText(g, 358, 783, 99999, legal, true, true);
			else
				drawLegalAndFlavorText(g, 647, 52, 920, 668, legal, getCard().getFlavor(), 14);
		}
		else
		{
			g.setFont(Fonts.TEXT);
			if ((legalTemp.length() <= 40 ||
					Pattern.matches("(?u)(?s)([\\#]{0,1}[\\((]\\{T\\}[ \\::]{1,2}.*?\\{[WUBRG]\\}.*?\\{[WUBRG]\\}.*?[\\.?][\\))][\\#]{0,1})(?!.)", legalTemp)) &&
					!legalTemp.contains("\n") && getCard().getFlavorMap().get("en").isEmpty())
				drawText(g, 358, 788, 99999, legal, true, true);
			else
				drawLegalAndFlavorText(g, 647, 52, 930, 668, legal, getCard().getFlavor(), 0);
		}

		g.setColor(Color.WHITE);
		g.setFont(Fonts.COLLECTION);

		StringBuilder collectorNumber = new StringBuilder(getCard().getNumber().replaceAll("[^\\d]", "") + "/");
		while (collectorNumber.length() < 4)
			collectorNumber.insert(0, "0");
		AtomicInteger max = new AtomicInteger(0);
		getCard().getSet().getCards().forEach(c -> max.set(Math.max(max.get(), Integer.parseInt(c.getNumber().replaceAll("[^\\d]", "")))));
		collectorNumber.append(max.get());

		String collectionTxtL1 = collectorNumber.toString();
		String collectionTxtL2 = getCard().getSet().getCode() + " • " + language.toUpperCase() + " ";

		drawText(g, 37, 977, 99999, collectionTxtL1 + "\n" + collectionTxtL2 + "{brush2}", false, false);
		int w = (int) getStringWidth(collectionTxtL2, g.getFont());
		drawText(g, 40 + w, 977, 99999, getCard().getRarity() == Rarity.BASIC_LAND ? "L" : "" + getCard().getRarity().name().charAt(0), false, false);
		g.setFont(Fonts.ARTIST);
		drawText(g, 64 + w, 996, 99999, getCard().getArtist(), false, false);

		String copyright = "GP ™ & © 2018 Wizards of the Coast";
		g.setFont(Fonts.COPYRIGHT);
		drawText(g, 680 - (int) getStringWidth(copyright, g.getFont()), getCard().getType().is(CardType.CREATURE) ? 996 : 977, 99999, copyright, false,
				false);

		return img;
	}

	@Override
	public File getFrameDir()
	{
		File dir = new File(DIR, "images/m15");
		String dirName = "regular";
		switch (getCard().getLayout())
		{
			case NORMAL:
				dirName = "regular";
				break;
			case LEVELER:
				dirName = "leveler";
				break;
			case SPLIT:
				dirName = "fuse-" + (getCard().getNumber().endsWith("a") ? "left" : "3right");
				break;
			case DOUBLE_FACED:
				if (getCard().getType().is(CardType.PLANESWALKER) && getCard().getSet().getCode().equals("ORI"))
					dirName = "transform-spark";
				else if (getCard().getSet().getCode().equals("EMN") && getCard().getMuId("en") != 414496 && getCard().getMuId("en") != 414497)
					dirName = "transform-" + (getCard().getNumber().endsWith("a") ? "moon" : "eldrazi");
				else if (getCard().getSet().getCode().equals("XLN") || getCard().getSet().getCode().equals("RIX"))
					dirName = "transform-" + (getCard().getNumber().endsWith("a") ? "explore" : "destination");
				else
					dirName = "transform-" + (getCard().getNumber().endsWith("a") ? "day" : "night");
				break;
			case MELD:
				dirName = "transform-" + (getCard().getNumber().endsWith("a") ? "moon" : "eldrazi");
				break;
			case FLIP:
				dirName = "flip";
				break;
		}

		if (getCard().getSet().getCode().equals("EXP"))
			dirName = "expedition";
		else if (getCard().getType().is(CardType.PLANESWALKER))
			dirName = "planeswalker";
		else if (getCard().getType() == CardType.LAND && getCard().isBasic() && Pattern.matches("(?i)^(ZEN|BFZ|OGW)$", getCard().getSet().getCode()) &&
				getCard().getNumber().endsWith("b"))
			dirName = "fullartbasicland";

		File result = new File(dir, dirName);
		if (!result.isDirectory())
			result = new File(DIR, "images/eight/" + dirName);

		return result;
	}
}
