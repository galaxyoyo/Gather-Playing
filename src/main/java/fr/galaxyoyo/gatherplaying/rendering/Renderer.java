package fr.galaxyoyo.gatherplaying.rendering;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.CardType;
import fr.galaxyoyo.gatherplaying.client.Config;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Renderer
{
	public static final File DIR = new File("cardgen");

	protected static List<Chunk> getChunks(String text)
	{
		text = text.replace("\r\n", "\n").replace("# ", " #").replace("\n\n•", "\n•").replace("#(", "(").replace("(", "#(").replace(")#", ")").replace(")", ")#") + "\n";
		Pattern pattern = Pattern.compile("(.*?)((?!&)[{}#\n])([^{}#\n]*)");
		Matcher matcher = pattern.matcher(text);
		List<Chunk> chunks = Lists.newArrayList();
		boolean italic = false;
		Chunk chunk = new Chunk();
		while (matcher.find())
		{
			chunk.setValue(chunk.getValue() + matcher.group(1));
			if (!chunk.getValue().isEmpty() || chunk.isNewLine() || chunk.isSymbol())
				chunks.add(chunk);
			if (matcher.group(2).equals("#"))
				italic = !italic;
			chunk = new Chunk();
			chunk.setItalic(italic);
			chunk.setSymbol(matcher.group(2).equals("{"));
			chunk.setNewLine(matcher.group(2).equals("\n"));
			chunk.setValue(matcher.group(3));
		}
		return chunks;
	}

	protected static BufferedImage readImage(File path)
	{
		try
		{
			return ImageIO.read(path);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Warning: " + path + " not found");
			return null;
		}
	}

	public int drawText(Graphics2D g, int left, int baseline, int maxWidth, String text, boolean centerX, boolean centerY)
	{
		if (maxWidth <= 0)
			maxWidth = 99999;

		text = text.replace("*", "{*}").replace("+", "{+}");
		List<Chunk> chunks = getChunks(text);
		Font font = g.getFont();
		Map<String, Number> map;
		int lastLineWidth;
		while (true)
		{
			map = testChunksWrapped(maxWidth, chunks, font);
			lastLineWidth = map.get("lastLineWidth").intValue();
			if (lastLineWidth < maxWidth && map.get("lineCount").floatValue() < text.split("\n").length + 0.1F)
				break;
			font = font.deriveFont(font.getSize2D() - 0.1F);
			if (font.getSize() < 8)
			{
				System.out.println("Warning: Text does not fit: " + text);
				break;
			}
		}

		if (centerX)
			left -= lastLineWidth / 2;
		if (centerY)
			baseline += (map.get("height").floatValue()) / 2.0F - 1;
		//	else
		//	baseline += font.getSize() - 11;
	/*	if ($tempFont -> shadow)
		{
			$shadowFont = clone $tempFont;
			$shadowFont -> setColor('0,0,0');
			$this -> drawChunksWrapped($canvas, $baseline + 2, $left + 2, 99999, $chunks, $shadowFont);
		}
		if ($tempFont -> glow)
		{
			$glowFont = clone $tempFont;
			$glowFont -> setColor('255,255,255');
			$this -> drawChunksWrapped($canvas, $baseline + 2, $left, 99999, $chunks, $glowFont);
			$this -> drawChunksWrapped($canvas, $baseline - 2, $left, 99999, $chunks, $glowFont);
			$this -> drawChunksWrapped($canvas, $baseline, $left + 2, 99999, $chunks, $glowFont);
			$this -> drawChunksWrapped($canvas, $baseline, $left - 2, 99999, $chunks, $glowFont);
		}*/
		drawChunksWrapped(g, baseline, left, 99999, chunks, font);
		return lastLineWidth;
	}

	public int drawTextWrappedAndScaled(Graphics2D g, int top, int left, int bottom, int right, String text, Font font, int heightAdjust)
	{
		if (text == null || text.trim().isEmpty())
			return 0;

		List<Chunk> chunks = getChunks(text);

		int maxHeight = bottom - top - heightAdjust;
		Font tempFont = font.deriveFont(font.getSize2D());

		Map<String, Number> textSize;
		while (true)
		{
			textSize = testChunksWrapped(right - left, chunks, tempFont);
			int difference = textSize.get("height").intValue() - maxHeight;
			float decrement;
			if (difference < 0)
				break;
			else if (difference < 15)
				decrement = 0.05F;
			else if (difference < 30)
				decrement = 0.2F;
			else if (difference < 100)
				decrement = 0.4F;
			else
				decrement = 0.8F;
			tempFont = tempFont.deriveFont(tempFont.getSize2D() - decrement);

			if (tempFont.getSize() < 8)
			{
				System.out.println("Warning: Text does not fit: " + text);
				break;
			}

			System.out.print(".");
		}

		drawChunksWrapped(g, (int) (top + (maxHeight - textSize.get("height").floatValue()) / 2.0F) + tempFont.getSize(), left, right, chunks, tempFont);
		return tempFont.getSize();
	}

	protected Map<String, Number> drawChunksWrapped(Graphics2D g, int baseline, int left, int right, List<Chunk> chunks, Font font)
	{
		int baselineStart = baseline;
		int maxWidth = right - left;
		int xOffset = 0;
		float spaceWidth = getStringWidth(" ", font);

		for (int i = 0; i < chunks.size(); i++)
		{
			Chunk chunk = chunks.get(i);
			if (chunk.isNewLine())
			{
				xOffset = 0;
				if (i != 0 && chunks.get(i - 1).isNewLine() && chunks.get(i - 1).getValue().isEmpty())
					baseline += font.getSize2D() / 2.0F;
				else
					baseline += font.getSize2D();
			}

			if (chunk.isSymbol())
			{
				if (xOffset + drawSymbol(null, 0, 0, font.getSize(), chunk.getValue(), false) > maxWidth)
				{
					xOffset = 0;
					baseline += font.getSize2D();
				}
				if (xOffset != 0 && !chunks.get(i - 1).isSymbol())
					xOffset += spaceWidth;
				xOffset += drawSymbol(g, baseline - font.getSize() + 2, left + xOffset, font.getSize(), chunk.getValue(), false);
			}
			else
			{
				if (chunk.isItalic())
				{
					font = Fonts.getItalic(font);
					if (g != null)
						g.setFont(font);
				}
				else
				{
					font = Fonts.getPlain(font);
					if (g != null)
						g.setFont(font);
				}
				if (xOffset != 0)
					xOffset += spaceWidth;
				String[] words = chunk.getValue().split(" ");
				for (int j = 0; j < words.length; j++)
				{
					String word = words[j];
					if (j != 0)
						xOffset += spaceWidth;
					if (xOffset + getStringWidth(word, font) > maxWidth)
					{
						xOffset = 0;
						baseline += font.getSize2D();
					}
					if (g != null)
						g.drawString(word, left + xOffset, baseline);
					xOffset += getStringWidth(word, font);

				}
			}
		}

		Map<String, Number> map = Maps.newHashMap();
		map.put("lastLineWidth", xOffset);
		map.put("height", baseline - baselineStart + font.getSize2D());
		map.put("lineCount", (baseline - baselineStart) / font.getSize2D() + 1);
		return map;
	}

	protected float getStringWidth(String text, Font font)
	{
		return (float) font.getStringBounds(text, new FontRenderContext(null, true, true)).getWidth();
	}

	protected int drawSymbol(Graphics2D g, int top, int left, int height, String symbol, boolean shadow)
	{
		String language = Config.getLocaleCode();
		Color color = g != null ? g.getColor() : Color.BLACK;
		if (symbol.equals("*") && (color.getRGB() & 0x00FFFFFF) != 0x0 && (color.getRGB() & 0x00FFFFFF) != 0xFFFFFF)
		{
			if ((color.getRGB() & 0x00FFFFFF) != 0x7F7F7F)
				color = Color.BLACK;
		}
		else if ((color.getRGB() & 0x00FFFFFF) != 0x0 && (color.getRGB() & 0x00FFFFFF) != 0xFFFFFF)
			color = Color.BLACK;
		if (g != null)
			g.setColor(color);

		boolean scale = true;
		int yOffset = 0;

		String colorStr = color.getRed() + "," + color.getGreen() + "," + color.getBlue();

		switch (symbol)
		{
			case "*":
				symbol = "star_" + colorStr;
				shadow = false;
				break;
			case "+":
				symbol = "plus_" + colorStr;
				shadow = false;
				break;
			case "-":
				symbol = "minus_" + colorStr;
				shadow = false;
				break;
			case "brush":
				symbol = "brush_" + colorStr;
				shadow = false;
				scale = false;
				yOffset = height / 3;
				break;
			case "brush2":
				symbol = "brush2_" + colorStr;
				shadow = false;
				scale = false;
				yOffset = height / 3;
				break;
			case "gear":
				symbol = "gear_" + colorStr;
			case "Artifact":
			case "Creature":
			case "Enchantment":
			case "Instant":
			case "Land":
			case "Multiple":
			case "Planeswalker":
			case "Sorcery":
				symbol += "_" + colorStr;
				shadow = false;
				scale = false;
				break;
			default:
				if (this instanceof TokenRenderer)
				{
					Pattern pattern = Pattern.compile("(?i)(?u)Img(\\p{L}+)");
					Matcher matcher = pattern.matcher(symbol);
					if (matcher.find())
					{
						symbol = "tokenfont/" + matcher.group(1);
						shadow = false;
						if (!language.equals("en"))
							scale = true;
						else if (((TokenRenderer) this).getToken().getType().is(CardType.EMBLEM))
						{
							scale = true;
							yOffset = 10;
						}
						else if (((TokenRenderer) this).getToken().getTranslatedName().get().replaceAll("(?i)(?u)Img(\\p{L}+)", " ").length() > 18)
						{
							scale = true;
							yOffset = 10;
						}
						else
							scale = false;
					}
				}

				if (symbol.length() == 2 && !Pattern.matches("\\w", symbol))
				{
					top -= height / 8;
					height *= 1.25F;
				}
				break;
		}

		BufferedImage image = null;

		Pattern pattern = Pattern.compile("(?i)(\\d|X|Y|Z)(W|U|R|G|B)");
		Matcher matcher = pattern.matcher(symbol);
		if (matcher.find())
		{
			BufferedImage sliceImg = null;
			try
			{
				sliceImg = ImageIO.read(new File(DIR, "images/symbols/" + matcher.group(1) + "_.png"));
			}
			catch (IOException e)
			{
				System.out.println("Warning: Symbol image not found: " + matcher.group(1) + "_");
			}
			String prefix = "";
			switch (matcher.group(2))
			{
				case "W":
				case "U":
					prefix = "G";
					break;
				case "R":
				case "G":
					prefix = "B";
					break;
				case "B":
					prefix = "U";
					break;
			}
			try
			{
				image = ImageIO.read(new File(DIR, "images/symbols/" + prefix + matcher.group(2) + ".png"));
				if (sliceImg != null)
					image.createGraphics().drawImage(sliceImg, 0, 0, null);
			}
			catch (IOException e)
			{
				System.out.println("Warning: Symbol image not found: " + matcher.group(1) + "_");
			}
		}
		else
		{
			String[] oldManaSets = {"LEA", "LEB", "2ED", "3ED", "4ED", "ATQ", "ARN", "DRK", "FEM", "HML", "LEG"};
			String[] originalTapSymbolSets = {"LEA", "LEB", "2ED", "3ED", "ATQ", "ARN", "DRK", "FEM"};
			if (symbol.equals("T") && ArrayUtils.contains(originalTapSymbolSets, this instanceof CardRenderer ? ((CardRenderer) this).getCard().getSet().getCode() : this
					instanceof TokenRenderer ? ((TokenRenderer) this).getToken().getSet().getCode() : null))
				symbol += "_old";
			else if (this instanceof PreEightRenderer && (symbol.equals("T") ||
					(Pattern.matches("(?s)(W|U|B|R|G)", symbol) && ArrayUtils.contains(oldManaSets, ((CardRenderer) this).getCard().getSet().getCode()))))
				symbol += "_pre";
			if (symbol.endsWith("/P"))
				symbol = "P" + symbol.substring(0, symbol.length() - 2);

			try
			{
				image = ImageIO.read(new File(DIR, "images/symbols/" + symbol + ".png"));
			}
			catch (IOException e)
			{
				System.out.println("Warning: Symbol image not found: " + symbol);
			}
		}

		int width = (int) (height * (image == null ? 1 : (double) image.getWidth() / (double) image.getHeight()));

		if (g != null)
		{
			if (shadow)
			{
				String extension = "";
				if (symbol.equals("1000000"))
					extension = "long";
				else if (Pattern.matches("(Half[WR])", symbol))
					extension = "Half";
				try
				{
					BufferedImage shadowImg = ImageIO.read(new File(DIR, "images/symbols/shadow" + extension + ".png"));
					g.drawImage(shadowImg, left - 2, top + 2, width + 2, height + 2, null);
				}
				catch (IOException e)
				{
					System.out.println("Warning: Symbol image not found: shadow" + extension);
				}
			}

			if (scale && image != null)
				g.drawImage(image, left, top, width, height, null);
			else if (image != null)
				g.drawImage(image, left, top + yOffset, null);
		}

		return width;
	}

	protected Map<String, Number> testChunksWrapped(int width, List<Chunk> chunks, Font font)
	{
		return drawChunksWrapped(null, 0, 0, width, chunks, font);
	}

	public abstract BufferedImage render() throws IOException;

	public abstract File getFrameDir();

	public abstract File getOutputFile();

	@ToString
	public static class Chunk
	{
		@Getter
		@Setter
		private String value = "";

		@Getter
		@Setter
		private boolean symbol;

		@Getter
		@Setter
		private boolean newLine;

		@Getter
		@Setter
		private boolean italic;
	}
}
