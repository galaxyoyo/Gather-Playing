package fr.galaxyoyo.gatherplaying.rendering;

import fr.galaxyoyo.gatherplaying.Card;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PreEightRenderer extends CardRenderer
{
	public PreEightRenderer(Card card)
	{
		super(card);
	}

	@Override
	public BufferedImage render() throws IOException
	{
		return null;
	}

	@Override
	public File getFrameDir()
	{
		return null;
	}
}
