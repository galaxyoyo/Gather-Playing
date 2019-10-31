package fr.galaxyoyo.gatherplaying.web.servlets;

import fr.galaxyoyo.gatherplaying.web.HttpHeader;
import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.IOUtils;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SimpleHtmlReaderServlet extends AbstractWebServlet
{
	private final String uri;
	private final String htmlPath;

	public SimpleHtmlReaderServlet(@Language("RegExp") String regex, String htmlPath)
	{
		this.uri = regex;
		this.htmlPath = htmlPath;
	}

	@Override
	public void doGet(HttpRequest request, HttpResponse resp)
	{
		try
		{
			resp.setStatus(HttpResponseStatus.OK);
			resp.setContentType("text/html; charset=utf-8");
			String text = IOUtils.toString(getClass().getResourceAsStream(htmlPath), StandardCharsets.UTF_8);
			text = text.replace("%W", "<img src=\"http://www.magiccorporation.com/images/magic/manas/mini/w.gif\" />")
					.replace("%U", "<img src=\"http://www.magiccorporation.com/images/magic/manas/mini/u.gif\" />")
					.replace("%B", "<img src=\"http://www.magiccorporation.com/images/magic/manas/mini/b.gif\" />")
					.replace("%R", "<img src=\"http://www.magiccorporation.com/images/magic/manas/mini/r.gif\" />")
					.replace("%G", "<img src=\"http://www.magiccorporation.com/images/magic/manas/mini/g.gif\" />")
					.replace("%C", "<img src=\"http://www.magiccorporation.com/images/magic/manas/mini/c.gif\" />");
			text = processReplaces(request, text);
			resp.setHeader(HttpHeader.CONTENT_LENGTH, text.getBytes(StandardCharsets.UTF_8).length);
			ByteBuf buffer = Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
			resp.content().writeBytes(buffer);
			buffer.release();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public String processReplaces(HttpRequest req, String text)
	{
		return text;
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse resp)
	{
		doGet(request, resp);
	}

	@Override
	public String getPattern()
	{
		return uri;
	}
}
