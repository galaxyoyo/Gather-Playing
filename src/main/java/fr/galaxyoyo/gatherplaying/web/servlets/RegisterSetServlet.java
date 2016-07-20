package fr.galaxyoyo.gatherplaying.web.servlets;

import fr.galaxyoyo.gatherplaying.MySQL;
import fr.galaxyoyo.gatherplaying.Set;
import fr.galaxyoyo.gatherplaying.web.HttpHeader;
import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class RegisterSetServlet extends SimpleHtmlReaderServlet
{
	public RegisterSetServlet()
	{
		super("(?i)^/register-set", "/web/register-set.html");
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse resp)
	{
		String name = request.getParameter("name");
		String name_fr = request.getParameter("name_fr");
		String code = request.getParameter("code");
		Date releaseDate = request.getDateParameter("date");
		String block = request.getParameter("block");
		String type = request.getParameter("type");

		Set set = new Set();
		set.setName(name);
		if (name_fr != null)
		{
			set.getTranslations().put("fr", name_fr);
			set.setFinishedTranslations("fr");
		}
		set.setCode(code);
		set.setMagicCardsInfoCode(code.toLowerCase());
		set.setReleaseDate(releaseDate);
		set.setBlock(block);
		set.setType(type);
		set.setBorder("black");
		set.setPreview();
		MySQL.addSet(set);

		String text = "Extension créée avec succès !";
		resp.setHeader(HttpHeader.CONTENT_LENGTH, text.getBytes(StandardCharsets.UTF_8).length);
		ByteBuf buffer = Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
		resp.content().writeBytes(buffer);
		buffer.release();
	}
}
