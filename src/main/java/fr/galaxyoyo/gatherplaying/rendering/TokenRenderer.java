package fr.galaxyoyo.gatherplaying.rendering;

import fr.galaxyoyo.gatherplaying.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@AllArgsConstructor
public class TokenRenderer extends Renderer
{
	@Getter
	private final Token token;

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

	@Override
	public File getOutputFile()
	{
		return new File(DIR, "output\\" + getToken().getSet().getCode() + "\\tokens\\" + getToken().name().toLowerCase().replace('_', ' ') + ".jpg");
	}
}
