package fr.galaxyoyo.gatherplaying.web.servlets;

import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.MySQL;
import fr.galaxyoyo.gatherplaying.client.Config;
import fr.galaxyoyo.gatherplaying.rendering.ImageWriter;
import fr.galaxyoyo.gatherplaying.web.HttpHeader;
import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class RenderCardServet extends AbstractWebServlet
{
	@Override
	public void doGet(HttpRequest request, HttpResponse resp)
	{
		Card card = MySQL.getCard(request.getIntParameter("muId"));

		Locale oldLocale = Config.getLocale();
		String locale = request.getParameter("locale");
		if (locale != null)
			Config.localeProperty().set(Locale.forLanguageTag(locale));

		ImageWriter w = new ImageWriter();
		w.addCard(card);
		w.renderAll();

		if (locale != null)
			Config.localeProperty().set(oldLocale);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try
		{
			if (card == null)
				ImageIO.write(ImageIO.read(new File("cardgen/images/m15/regular/cards", "back.png")), "JPEG", baos);
			else
				ImageIO.write(w.getRenderers().values().iterator().next(), "JPEG", baos);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		resp.setHeader(HttpHeader.CONTENT_LENGTH, baos.size());
		resp.setContentType("image/jpeg");
		ByteBuf buffer = Unpooled.copiedBuffer(baos.toByteArray());
		resp.content().writeBytes(buffer);
		buffer.release();
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse resp)
	{
		doGet(request, resp);
	}

	@Override
	public String getPattern()
	{
		return "^/render-card$";
	}
}
