package fr.galaxyoyo.gatherplaying.rendering;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@AllArgsConstructor
public abstract class CardRenderer extends Renderer
{
	protected static final File ARTDIR = new File(DIR, "Pictures" + (Utils.DEBUG ? "/Main" : ""));

	@Getter
	private final Card card;

	public void drawArt(Graphics2D g, File artFileName, int artTop, int artLeft, int artBottom, int artRight)
	{
		BufferedImage artImg = null;
		if (artFileName != null)
		{
			try
			{
				artImg = ImageIO.read(artFileName);
			}
			catch (IOException ignored)
			{
			}
		}

		if (artImg == null)
		{
			if (getCard().getImageName() != null && !getCard().getImageName().isEmpty())
			{
				try
				{
					artImg = ImageIO.read(new URL(getCard().getImageName()));
				}
				catch (Exception ignored)
				{
				}
			}

			if (artImg == null)
			{
				System.out.print("*Art not found*");
				return;
			}
		}

	//	int srcWidth = artImg.getWidth();
	//	int srcHeight = artImg.getHeight();
		int destWidth = artRight - artLeft + 1;
		int destHeight = artBottom - artTop + 1;

		int width = destWidth, height = destHeight;

	/*	if (srcWidth > srcHeight)
		{
			height = srcWidth * destHeight / destWidth;
			width = srcWidth;
			if (height > srcHeight)
			{
				width = srcHeight * destWidth / destHeight;
				height = srcHeight;
			}
		}
		else
		{
			width = srcHeight * destWidth / destHeight;
			height = srcHeight;
			if (width > srcWidth)
			{
				height = srcWidth * destHeight / destWidth;
				width = srcWidth;
			}
		}*/

		g.drawImage(artImg, artLeft, artTop, width, height, null);
	}

	public void drawLegalAndFlavorText(Graphics2D g, int top, int left, int bottom, int right, String legal, String flavor, int heightAdjust)
	{
		String text = legal.replace("\n", "\n\n");
		if (flavor != null && !flavor.trim().isEmpty())
			text += "\n\n#" + flavor + '#';
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

	public int drawCastingCost(Graphics2D g, ManaColor[] symbols, int top, int right, int symbolSize)
	{
		int symbolsWidth = 0;

		if (symbols == null)
			symbols = new ManaColor[0];

		for (ManaColor symbol : symbols)
			symbolsWidth += drawSymbol(null, 0, 0, symbolSize, symbol.getAbbreviate(), false) + 4;
		// Output casting cost symbols from left to right.
		int x = right - symbolsWidth;
		int left = x - 5;
		for (ManaColor symbol : symbols)
			x += drawSymbol(g, top, x, symbolSize, symbol.getAbbreviate(), true) + 4;
		return left;
	}

	public int drawFutureshiftedCastingCost(Graphics2D g, ManaColor[] symbols, int top, int right, int symbolSize)
	{
		return drawCastingCost(g, symbols, top, right, symbolSize);
	}

	public int drawFutureshiftedTextlessCastingCost(Graphics2D g, ManaColor[] symbols, int top, int right, int symbolSize)
	{
		return drawCastingCost(g, symbols, top, right, symbolSize);
	}

	public String getCardName()
	{
		return card.getName().get("en");
	}

	@Override
	public File getOutputFile()
	{
		return new File(DIR, "output/" + getCard().getSet().getCode() + "/" + getCard().getMuId(Config.getLocaleCode()) + ".jpg");
	}
}
