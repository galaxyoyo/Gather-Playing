package fr.galaxyoyo.gatherplaying.web;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.web.servlets.SimpleHtmlReaderServlet;
import fr.galaxyoyo.gatherplaying.web.servlets.WebServlet;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebServerHandler extends SimpleChannelInboundHandler<FullHttpRequest>
{
	private static final List<WebServlet> servlets = Lists.newArrayList();
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

	static
	{
		DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
		registerServlet(new SimpleHtmlReaderServlet("(?i)^/$|^/accueil$|^/home$", "/web/index.html"));
	}

	public static void registerServlet(WebServlet servlet)
	{
		servlets.add(servlet);
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception
	{
		if (!req.decoderResult().isSuccess())
		{
			sendError(ctx, BAD_REQUEST);
			return;
		}

		final String uri = req.uri();

		FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK);
		resp.headers().set(CONTENT_TYPE, "text/html; charset=utf-8");
		if (HttpHeaderUtil.isKeepAlive(req))
			resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		AtomicBoolean found = new AtomicBoolean(false);
		servlets.stream().filter(servlet -> Pattern.compile(servlet.getPattern()).matcher(uri).find()).forEach(servlet ->
		{
			found.set(true);
			if (req.method() == HttpMethod.GET)
				servlet.doGet(req, resp);
			else if (req.method() == HttpMethod.POST)
				servlet.doPost(req, resp);
		});
		if (!found.get())
			sendNotFound(resp);
		System.out.println(resp);
		Calendar time = new GregorianCalendar();
		resp.headers().set(HttpHeaderNames.DATE, DATE_FORMATTER.format(time.getTime()));
		ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
	}

	public static void sendNotFound(FullHttpResponse resp)
	{
		resp.setStatus(NOT_FOUND);
		resp.content().writeBytes(Unpooled.copiedBuffer("<html><head><title>Erreur 404 — Page non trouvée</title></head><body><h1>Erreur 404 — Page non trouvée</h1>Merci de " +
						"revérifier votre URL.</body></html>",
				CharsetUtil.UTF_8));
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status)
	{
		FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
		resp.headers().set(CONTENT_TYPE, "text/plain; charset=utf-8");
		ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		sendError(ctx, INTERNAL_SERVER_ERROR);
	}
}
