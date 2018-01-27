package fr.galaxyoyo.gatherplaying.rendering;

import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.*;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ImageWriter
{
	private static final Map<String, String> titleToLandColors = Maps.newHashMap();

	static
	{
		try
		{
			File titleToLandColorsFile = new File(CardRenderer.DIR + "/data", "titleToLandColors.csv");
			List<String> lines = FileUtils.readLines(titleToLandColorsFile, StandardCharsets.UTF_8);
			for (String line : lines)
			{
				if (line.isEmpty())
					continue;
				String[] split;
				if (line.startsWith("\""))
					split = new String[]{line.substring(1, line.lastIndexOf('"') - 1), line.substring(line.lastIndexOf(',') + 1)};
				else
					split = line.split(",");
				titleToLandColors.put(split[0].toLowerCase(), split[1]);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Getter
	private Map<Renderer, BufferedImage> renderers = Maps.newHashMap();

	public void addCard(Card card)
	{
		renderers.put(getRenderer(card), null);
	}

	public static CardRenderer getRenderer(Card card)
	{
		if (card.getType().is(CardType.PLANESWALKER))
			return new M15Planeswalker(card);
		else if (card.getLayout() == Layout.AFTERMATH)
			return new M15AfterMathRender(card);
		return new M15Renderer(card);
	}

	public void addToken(Token token)
	{
		renderers.put(getRenderer(token), null);
	}

	public static TokenRenderer getRenderer(Token token)
	{
		return new TokenRenderer(token);
	}

	public void renderAll()
	{
		for (Map.Entry<Renderer, BufferedImage> entry : renderers.entrySet())
		{
			Renderer renderer = entry.getKey();
			if (renderer.getOutputFile().isFile() && !Utils.DEBUG)
			{
				try
				{
					entry.setValue(ImageIO.read(renderer.getOutputFile()));
					continue;
				}
				catch (IOException ignored)
				{
				}
			}

			try
			{
				entry.setValue(renderer.render());
				if (!renderer.getOutputFile().getParentFile().isDirectory())
					renderer.getOutputFile().getParentFile().mkdirs();
				renderer.getOutputFile().createNewFile();
			//	if (!Utils.DEBUG && (!(renderer instanceof CardRenderer) || ((CardRenderer) renderer).getCard().isPreview()) && (!(renderer instanceof TokenRenderer) || (
			//			(TokenRenderer) renderer).getToken().getSet().isPreview()))
					ImageIO.write(entry.getValue(), "JPEG", renderer.getOutputFile());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static String getLandColors(Card card)
	{
		return titleToLandColors.get(card.getName().get("en").toLowerCase());
	}
}
