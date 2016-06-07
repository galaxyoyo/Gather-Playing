package fr.galaxyoyo.gatherplaying.web.servlets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
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
	public void doGet(FullHttpRequest request, FullHttpResponse resp)
	{
		try
		{
			resp.setStatus(HttpResponseStatus.OK);
			resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
			String text = IOUtils.toString(getClass().getResourceAsStream(htmlPath), StandardCharsets.UTF_8);
			resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, "" + text.getBytes(StandardCharsets.UTF_8).length);
			ByteBuf buffer = Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
			resp.content().writeBytes(buffer);
			buffer.release();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void doPost(FullHttpRequest request, FullHttpResponse resp)
	{
		doGet(request, resp);
	}

	@Override
	public String getPattern()
	{
		return uri;
	}
}
