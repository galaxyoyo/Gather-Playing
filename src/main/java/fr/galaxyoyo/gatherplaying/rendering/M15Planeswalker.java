package fr.galaxyoyo.gatherplaying.rendering;

import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.Layout;
import fr.galaxyoyo.gatherplaying.ManaColor;
import fr.galaxyoyo.gatherplaying.Rarity;
import fr.galaxyoyo.gatherplaying.client.Config;
import java8.util.stream.Collectors;
import java8.util.stream.RefStreams;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class M15Planeswalker extends CardRenderer
{
	public M15Planeswalker(Card card)
	{
		super(card);
	}

	@Override
	public BufferedImage render() throws IOException
	{
		String language = Config.getLocaleCode();

		System.out.print("\n" + getCard().getTranslatedName().get() + "...");

		File frameDir = getFrameDir();

		ManaColor[] colorsObj = getCard().getColors();
		String colors = String.join("", RefStreams.of(colorsObj).map(ManaColor::getAbbreviate).collect(Collectors.toList()));
		switch (colors)
		{
			case "WG":
				colors = "GW";
				break;
			case "WR":
				colors = "RW";
				break;
			case "UG":
				colors = "GU";
				break;
		}
		boolean useMulticolorFrame = colors.length() == 2;

		BufferedImage img = new BufferedImage(720, 1020, BufferedImage.TYPE_INT_RGB);

		Graphics2D g = img.createGraphics();

		File picDir = new File(ARTDIR, getCard().getSet().getCode());
		picDir.mkdirs();
		File artFile = new File(picDir, getCard().getName().get("en") + ".jpg");
		if (!artFile.isFile())
		{
			BufferedImage art = ImageIO.read(new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + (getCard().getMuId("en")) + "&type=card")).getSubimage(18, 36,
					205 - 18, 173 - 36);
			ImageIO.write(art, "JPG", artFile);
		}

		//drawArt(g, artFile, 0, 0, 1020, 720);
		drawArt(g, artFile, 107, 43, 569, 674);

		System.out.print(".");

		String legal = getCard().getAbility().replace(" : ", ": ").replace('−', '-').replace("\r\n", "\n").replace("\r", "\n");
		String[] split = legal.split("\n");
		String nb = split.length == 4 ? "4" : split.length == 2 ? "2" : "";

		BufferedImage bgImage;
		if (colorsObj[0] == ManaColor.COLORLESS)
			bgImage = readImage(new File(frameDir, "cards/Art" + nb + ".png"));
		else if (useMulticolorFrame)
			bgImage = readImage(new File(frameDir, "cards/Gld" + colors + nb + ".png"));
		else
			bgImage = readImage(new File(frameDir, "cards/" + (colors.length() >= 3 ? "Gld" : colors) + nb + ".png"));

		if (bgImage != null)
			g.drawImage(bgImage, 0, 0, 720, 1020, null);

		if (getCard().getLoyalty() > 0)
		{
			BufferedImage image = readImage(new File(DIR, "images/m15/planeswalker/loyalty/LoyaltyBegin.png"));
			if (image != null)
				g.drawImage(image, 0, 0, 720, 1020, null);
			g.setFont(Fonts.LOYALTY_STARTING);
			g.setColor(Color.WHITE);
			drawText(g, 641, 940, 114, Integer.toString(getCard().getLoyalty()), true, true);
		}

		int costLeft = drawCastingCost(g, getCard().getManaCost(), colors.length() >= 2 ? 37 : 35, 677, 35);
		System.out.print(".");
		int rarityLeft = drawRarity(g, getCard().getRarity(), getCard().getSet(), 675, split.length == 4 ? 534 : 604, 41, 76);

		int titleX = frameDir.getName().startsWith("transform-") ? 105 : 51;
		g.setColor(Color.BLACK);
		g.setFont(Fonts.TITLE);
		drawText(g, titleX, frameDir.getName().startsWith("transform-") ? 64 : 68, costLeft - 20 - titleX, getCard().getTranslatedName().get(), false, false);
		System.out.print(".");

		int typex = frameDir.getName().equals("transform-night") || frameDir.getName().equals("transform-ignite") ? 87 : 51;
		String type = "Planeswalker";
		if (getCard().isLegendary())
		{
			if (Config.getLocaleCode().equals("fr"))
				type += " légendaire";
			else
				type = "Legendary Planeswalker";
		}
		if (Config.getLocaleCode().equals("fr"))
			type += " : ";
		else
			type += " — ";
		drawText(g, typex, split.length == 4 ? 546 : 616, rarityLeft - typex, type + getCard().getSubtypes()[0].getTranslatedName().get(), false, false);
		String[][] infos = new String[split.length][2];
		for (int i = 0; i < split.length; ++i)
		{
			infos[i] = split[i].split(":[ | ]");
			if (infos[i].length == 1)
				infos[i] = new String[]{"", split[i]};
		}
		int maxWidth = 673;
		Font f = Fonts.TEXT;
		Map<String, Number> map1, map2, map3, map4 = null;
		if (split.length == 4)
		{
			while (true)
			{
				map1 = testChunksWrapped(maxWidth, getChunks(infos[0][1]), f);
				map2 = testChunksWrapped(maxWidth, getChunks(infos[1][1]), f);
				map3 = testChunksWrapped(maxWidth, getChunks(infos[2][1]), f);
				map4 = testChunksWrapped(maxWidth, getChunks(infos[3][1]), f);
				int difference = Math.max(Math.max(Math.max(map1.get("height").intValue(), map2.get("height").intValue()), map3.get("height").intValue()),
						map4.get("height").intValue()) - 65;
				int lastLineWidth = map3.get("lastLineWidth").intValue();
				float decrement;
				if (difference < 0 && lastLineWidth <= 600)
					break;
				else if (15 > difference)
					decrement = 0.05F;
				else if (30 > difference)
					decrement = 0.2F;
				else if (difference < 100)
					decrement = 0.4F;
				else
					decrement = 0.8F;
				f = f.deriveFont(f.getSize2D() - decrement);
			}
		}
		else
		{
			while (true)
			{
				map1 = testChunksWrapped(maxWidth, getChunks(infos[0][1]), f);
				map2 = testChunksWrapped(maxWidth, getChunks(infos[1][1]), f);
				map3 = testChunksWrapped(maxWidth, getChunks(infos[2][1]), f);
				int difference = Math.max(Math.max(map1.get("height").intValue(), map2.get("height").intValue()), map3.get("height").intValue()) - 65;
				int lastLineWidth = map3.get("lastLineWidth").intValue();
				float decrement;
				if (difference < 0 && lastLineWidth <= 600)
					break;
				else if (15 > difference)
					decrement = 0.05F;
				else if (30 > difference)
					decrement = 0.2F;
				else if (difference < 100)
					decrement = 0.4F;
				else
					decrement = 0.8F;
				f = f.deriveFont(f.getSize2D() - decrement);
			}
		}
		for (int i = 0; i < split.length; i++)
		{
			BufferedImage image = infos[i][0].isEmpty() ? null : loyaltyIcon(infos[i][0].charAt(0));
			int y = 0;
			switch (split.length - i)
			{
				case 4:
					y = 600;
					break;
				case 3:
					y = 680;
					break;
				case 2:
					y = 782;
					break;
				case 1:
					y = 872;
					break;
			}

			if (image != null)
				g.drawImage(image, 0, y - 57, 120, 120, null);
			g.setColor(Color.WHITE);
			g.setFont(Fonts.LOYALTY_CHANGE);
			drawText(g, 60, y, 114, infos[i][0], true, true);
			g.setColor(Color.BLACK);
			drawChunksWrapped(g, (int) (y  + f.getSize2D() - ((i == 0 ? map1 : i == 1 ? map2 : i == 2 ? map3 : map4).get("height").floatValue()) / 2.0F), 122,
					673, getChunks(infos[i][1]), f);
		}

		g.setColor(Color.WHITE);
		g.setFont(Fonts.COLLECTION);

		String collectorNumber = getCard().getNumber().replaceAll("[^\\d]", "") + "/";
		while (collectorNumber.length() < 4)
			collectorNumber = "0" + collectorNumber;
		AtomicInteger max = new AtomicInteger(0);
		getCard().getSet().getCards().forEach(c -> max.set(Math.max(max.get(), Integer.parseInt(c.getNumber().replaceAll("[^\\d]", "")))));
		collectorNumber += max.get();

		String collectionTxtL1 = collectorNumber;
		String collectionTxtL2 = getCard().getSet().getCode() + " • " + language.toUpperCase() + " ";

		drawText(g, 37, 977, 99999, collectionTxtL1 + "\n" + collectionTxtL2 + "{brush2}", false, false);
		int w = (int) getStringWidth(collectionTxtL2, g.getFont());
		drawText(g, 40 + w, 977, 99999, getCard().getRarity() == Rarity.BASIC_LAND ? "L" : "" + getCard().getRarity().name().charAt(0), false, false);
		g.setFont(Fonts.ARTIST);
		drawText(g, 64 + w, 996, 99999, getCard().getArtist(), false, false);

		String copyright = "GP ™ & © 2018 Wizards of the Coast";
		g.setFont(Fonts.COPYRIGHT);
		drawText(g, 680 - (int) getStringWidth(copyright, g.getFont()), 996, 99999, copyright, false, false);

		return img;
	}

	private BufferedImage loyaltyIcon(char sign)
	{
		BufferedImage loyaltyImage;
		if (sign == '+')
			loyaltyImage = readImage(new File(DIR, "images/m15/planeswalker/loyalty/LoyaltyUp.png"));
		else if (sign == '-')
			loyaltyImage = readImage(new File(DIR, "images/m15/planeswalker/loyalty/LoyaltyDown.png"));
		else
			loyaltyImage = readImage(new File(DIR, "images/m15/planeswalker/loyalty/LoyaltyZero.png"));
		return loyaltyImage;
	}

	@Override
	public File getFrameDir()
	{
		String frame = "regular";
		if (getCard().getSet().getCode().equals("ORI"))
			frame = "transform-ignite";
		else if (getCard().getLayout() == Layout.DOUBLE_FACED)
			frame = "transform-" + (getCard().getNumber().endsWith("a") ? "day" : "night");
		return new File(DIR, "images/m15/planeswalker/" + frame);
	}
}
