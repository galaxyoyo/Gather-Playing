package fr.galaxyoyo.gatherplaying.web;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.web.servlets.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class WebServerHandler extends SimpleChannelInboundHandler<FullHttpRequest>
{
	private static final List<WebServlet> servlets = Lists.newArrayList();
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

	static
	{
		DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
		registerServlet(new SimpleHtmlReaderServlet("(?i)^/$|^/accueil$|^/home$", "/web/index.html"));
		registerServlet(new LoginServlet());
		registerServlet(new ViewDeckServlet());
		registerServlet(new CardShowerServlet());
		registerServlet(new SetShowerServlet());
		registerServlet(new RegisterSetServlet());
		registerServlet(new RegisterCardServlet());
		registerServlet(new RenderCardServet());
	}

	public static void registerServlet(WebServlet servlet)
	{
		servlets.add(servlet);
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest _req) throws Exception
	{
		if (!_req.decoderResult().isSuccess())
		{
			sendError(ctx, BAD_REQUEST, new UnknownError("Impossible to decode"));
			return;
		}

		HttpRequest req = new HttpRequest(_req);

		final String uri = req.uri().split("\\?")[0];

		HttpResponse resp = new HttpResponse();
		resp.setContentType("text/html; charset=utf-8");
		if (req.isKeepAlive())
			resp.setHeader(HttpHeader.CONNECTION, HttpHeader.Values.KEEP_ALIVE);
		resp.setHeader(HttpHeader.CACHE_CONTROL, "max-age=86400");
		AtomicBoolean found = new AtomicBoolean(false);
		servlets.stream().filter(servlet -> Pattern.compile(servlet.getPattern()).matcher(uri).find()).forEach(servlet ->
		{
			found.set(true);
			if (req.method() == HttpMethod.GET)
			{
				if (servlet instanceof AbstractWebServlet)
				{
					Date lastModified = ((AbstractWebServlet) servlet).getLastModifiedDate();
					if (lastModified != null)
					{
						Date ifModifiedSince = req.getHeaderDate(HttpHeader.IF_MODIFIED_SINCE);
						if (ifModifiedSince != null)
						{
							if (lastModified.before(ifModifiedSince))
							{
								resp.setStatus(NOT_MODIFIED);
								Calendar time = new GregorianCalendar();
								resp.headers().set(HttpHeaderNames.DATE, DATE_FORMATTER.format(time.getTime()));
								ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
								return;
							}
						}
						resp.setHeader(HttpHeader.LAST_MODIFIED, new Date());
						Calendar cal = new GregorianCalendar();
						cal.add(Calendar.DAY_OF_YEAR, 1);
						resp.setHeader(HttpHeader.EXPIRES, cal.getTime());
					}
				}
				servlet.doGet(req, resp);
			}
			else if (req.method() == HttpMethod.POST)
				servlet.doPost(req, resp);
		});
		if (!found.get() || resp.status() == NOT_FOUND)
			sendNotFound(resp);
		Calendar time = new GregorianCalendar();
		resp.headers().set(HttpHeaderNames.DATE, DATE_FORMATTER.format(time.getTime()));
		ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, Throwable cause)
	{
		StringWriter sw = new StringWriter();
		cause.printStackTrace(new PrintWriter(sw));
		HttpResponse resp = new HttpResponse(status, Unpooled.copiedBuffer("Failure: " + status + "\r\n" + sw, CharsetUtil.UTF_8));
		resp.setContentType("text/plain; charset=utf-8");
		ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
	}

	public static void sendNotFound(FullHttpResponse resp)
	{
		resp.setStatus(NOT_FOUND);
		resp.content().writeBytes(Unpooled.copiedBuffer("<html><head><title>Erreur 404 — Page non trouvée</title></head><body><h1>Erreur 404 — Page non trouvée</h1>Merci de " +
						"revérifier votre URL.</body></html>",
				CharsetUtil.UTF_8));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		sendError(ctx, INTERNAL_SERVER_ERROR, cause);
	}
}
