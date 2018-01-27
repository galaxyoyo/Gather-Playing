package fr.galaxyoyo.gatherplaying.rendering;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Config;
import java8.util.stream.Collectors;
import java8.util.stream.RefStreams;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class M15AfterMathRender extends M15Renderer
{
	private final Card left, right;

	public M15AfterMathRender(Card card)
	{
		super (card);
		if (card.getNumber().endsWith("a"))
		{
			left = card;
			right = card.getRelated();
		}
		else
		{
			left = card.getRelated();
			right = card;
		}
	}

	@Override
	public BufferedImage render() throws IOException
	{
		String language = Config.getLocaleCode();

		System.out.print("\n" + getCardName() + " ...");

		File frameDirLeft = new File(getFrameDir(), "aftermath-left");
		File frameDirRight = new File(getFrameDir(), "aftermath-right");

		BufferedImage img = new BufferedImage(720, 1020, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);

		BufferedImage imgLeft = new BufferedImage(720, 552, BufferedImage.TYPE_INT_RGB);
		Graphics2D gLeft = imgLeft.createGraphics();
		gLeft.setColor(Color.BLACK);

		BufferedImage imgRight = new BufferedImage(468, 720, BufferedImage.TYPE_INT_RGB);
		Graphics2D gRight = imgRight.createGraphics();
		gRight.setColor(Color.BLACK);

		File picDir = new File(ARTDIR, getCard().getSet().getCode());
		picDir.mkdirs();
		File artFileLeft = new File(picDir, getCardLeft().getImageName() + ".png");
	//	if (!artFileLeft.isFile())
		{
			BufferedImage art = ImageIO.read(new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + (getCard().getMuId("en")) + "&type=card")).getSubimage(18, 36,
					205 - 18, 105 - 36);
			ImageIO.write(art, "PNG", artFileLeft);
		}
		File artFileRight = new File(picDir, getCardRight().getImageName() + ".png");
	//	if (!artFileRight.isFile())
		{
			BufferedImage rotatedArt = ImageIO.read(new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + (getCard().getMuId("en")) + "&type=card")).getSubimage
					(123, 176, 64, 109);
			BufferedImage art = new BufferedImage(rotatedArt.getHeight(), rotatedArt.getWidth(), BufferedImage.TYPE_INT_RGB);
			AffineTransform at = new AffineTransform();
			at.rotate(-Math.PI / 2.0, 32, 54);
			at.translate(54 - 32, 54 - 32);
			art.createGraphics().drawImage(rotatedArt, at, null);
			ImageIO.write(art, "PNG", artFileRight);
		}

		drawArt(gLeft, artFileLeft, 107, 43, 341, 674);
		drawArt(gRight, artFileRight, 110, 24, 324, 388);

		System.out.print(".");

		String holoFoil = "";
		if ((getCard().getRarity() == Rarity.RARE || getCard().getRarity() == Rarity.MYTHIC || getCard().getRarity() == Rarity.SPECIAL))
			holoFoil = "_H";

		BufferedImage bgImageLeft, bgImageRight;
		String colorsLeft, colorsRight;


		ManaColor[] costLeft = getCardLeft().getManaCost(), costRight = getCardRight().getManaCost();
		if (costLeft == null)
			costLeft = new ManaColor[0];
		if (costRight == null)
			costRight = new ManaColor[0];
		colorsLeft = String.join("", RefStreams.of(costLeft).filter(color -> !color.name().contains("NEUTRAL")).map(ManaColor::getAbbreviate).distinct().collect(Collectors.toList()));
		colorsRight = String.join("", RefStreams.of(costRight).filter(color -> !color.name().contains("NEUTRAL")).map(ManaColor::getAbbreviate).distinct().collect(Collectors.toList()));
		bgImageLeft = readImage(new File(frameDirLeft, "cards/" + colorsLeft + ".png"));
		bgImageRight = readImage(new File(frameDirRight, "cards/" + colorsRight + holoFoil + ".png"));

		if (bgImageLeft != null)
			gLeft.drawImage(bgImageLeft, 0, 0, 720, 552, null);
		if (bgImageRight != null)
			gRight.drawImage(bgImageRight, 0, 0, 468, 720, null);

		int costLeftPix = drawCastingCost(gLeft, getCardLeft().getManaCost(), 51, 677, 35);
		int costRightPix = drawCastingCost(gRight, getCardRight().getManaCost(), 55, 389, 35);
		System.out.print(".");
		int rarityLeft = drawRarity(gLeft, getCardLeft().getRarity(), getCardLeft().getSet(), 675, 375, 41, 76);

		int titleXLeft = 51;
		int titleXRight = 27;
		gLeft.setFont(Fonts.TITLE);
		gRight.setFont(Fonts.TITLE);
		drawText(gLeft, titleXLeft, 84, costLeftPix - 20 - titleXLeft, getCardLeft().getTranslatedName().get(), false, false);
		drawText(gRight, titleXRight, 84, costRightPix - 20 - titleXRight, getCardRight().getTranslatedName().get(), false, false);
		System.out.print(".");

		gLeft.setFont(Fonts.TYPE);
		gRight.setFont(Fonts.TYPE);

		int typeXLeft = 51;
		int typeXRight = 27;
		StringBuilder typeLeft = new StringBuilder(getCardLeft().getType().getTranslatedName().get());
		StringBuilder typeRight = new StringBuilder(getCardRight().getType().getTranslatedName().get());
		if (getCardLeft().getSubtypes() != null && getCardLeft().getSubtypes().length > 0)
		{
			typeLeft.append(Config.getLocaleCode().equals("fr") ? " : " : " — ");
			for (SubType st : getCardLeft().getSubtypes())
			{
				String name = st.getTranslatedName().get();
				if (Config.getLocaleCode().equals("fr"))
					name = name.toLowerCase();
				typeLeft.append(name).append(Config.getLocaleCode().equals("fr") ? " et " : " ");
			}
			typeLeft = new StringBuilder(typeLeft.substring(0, typeLeft.length() - (Config.getLocaleCode().equals("fr") ? 4 : 1)));
		}
		if (getCardRight().getSubtypes() != null && getCardRight().getSubtypes().length > 0)
		{
			typeRight.append(Config.getLocaleCode().equals("fr") ? " : " : " — ");
			for (SubType st : getCardRight().getSubtypes())
			{
				String name = st.getTranslatedName().get();
				if (Config.getLocaleCode().equals("fr"))
					name = name.toLowerCase();
				typeRight.append(name).append(Config.getLocaleCode().equals("fr") ? " et " : " ");
			}
			typeRight = new StringBuilder(typeRight.substring(0, typeRight.length() - (Config.getLocaleCode().equals("fr") ? 4 : 1)));
		}
		drawText(gLeft, typeXLeft, 387, rarityLeft - typeXLeft, typeLeft.toString(), false, false);
		drawText(gRight, typeXRight, 370, rarityLeft - typeXRight, typeRight.toString(), false, false);

		gLeft.setColor(Color.BLACK);
		gRight.setColor(Color.BLACK);
		String legalLeft = (getCardLeft().getAbilityMap().get("en") == null ? "" : getCardLeft().getAbility());
		String legalRight = (getCardRight().getAbilityMap().get("en") == null ? "" : getCardRight().getAbility());

		gLeft.setFont(Fonts.TEXT);
		gRight.setFont(Fonts.TEXT);
		if ((legalLeft.length() <= 40 ||
				Pattern.matches("(?u)(?s)([\\#]{0,1}[\\((]\\{T\\}[ \\::]{1,2}.*?\\{[WUBRG]\\}.*?\\{[WUBRG]\\}.*?[\\.?][\\))][\\#]{0,1})(?!.)", legalLeft)) &&
				!legalLeft.contains("\n"))
			drawText(gLeft, 358, 475, 99999, legalLeft, true, true);
		else
			drawLegalAndFlavorText(gLeft, 415, 52, 540, 668, legalLeft, getCardLeft().getFlavor(), 0);
		if ((legalRight.length() <= 40 ||
				Pattern.matches("(?u)(?s)([\\#]{0,1}[\\((]\\{T\\}[ \\::]{1,2}.*?\\{[WUBRG]\\}.*?\\{[WUBRG]\\}.*?[\\.?][\\))][\\#]{0,1})(?!.)", legalLeft)) &&
				!legalRight.contains("\n"))
			drawText(gRight, 358, 475, 99999, legalRight, true, true);
		else
			drawLegalAndFlavorText(gRight, 388, 34, 672, 385, legalRight, getCardRight().getFlavor(), 0);

		g.drawImage(imgLeft, 0, 0, imgLeft.getWidth(), imgLeft.getHeight(), null, null);
		AffineTransform at = new AffineTransform();
		at.rotate(Math.PI / 2.0, 0, 552);
		at.translate(0, 552 - 720);
		g.drawImage(imgRight, at, null);

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

	public Card getCardLeft()
	{
		return left;
	}

	public Card getCardRight()
	{
		return right;
	}

	@Override
	public File getFrameDir()
	{
		return new File(DIR, "images/m15");
	}
}
