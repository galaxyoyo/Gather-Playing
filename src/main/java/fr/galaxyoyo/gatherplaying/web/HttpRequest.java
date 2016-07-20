package fr.galaxyoyo.gatherplaying.web;

import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpRequest extends DefaultFullHttpRequest
{
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
	private Map<String, String> parameters;
	private Cookie[] cookies;

	public HttpRequest(FullHttpRequest req)
	{
		super(req.protocolVersion(), req.method(), req.uri(), req.content(), true);
		headers().setAll(req.headers());
		trailingHeaders().setAll(req.trailingHeaders());
	}

	@Override
	public HttpRequest duplicate()
	{
		return new HttpRequest(this);
	}

	public boolean getHeaderBoolean(HttpHeader header)
	{
		return headers().getBoolean(header.getName());
	}

	public int getHeaderInt(HttpHeader header)
	{
		return headers().getInt(header.getName());
	}

	public double getHeaderDouble(HttpHeader header)
	{
		return headers().getDouble(header.getName());
	}

	public long getHeaderLong(HttpHeader header)
	{
		return headers().getLong(header.getName());
	}

	public Date getHeaderDate(HttpHeader header)
	{
		try
		{
			return DATE_FORMATTER.parse(getHeader(header));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public String getHeader(HttpHeader header)
	{
		CharSequence seq = headers().get(header.getName());
		return seq == null ? null : seq.toString();
	}

	public boolean isKeepAlive()
	{
		return HttpHeader.Values.KEEP_ALIVE.getName().equals(getHeader(HttpHeader.CONNECTION));
	}

	public int getIntParameter(String key)
	{
		return Integer.parseInt(getParameter(key));
	}

	public String getParameter(String key)
	{
		if (parameters == null)
		{
			parameters = Maps.newHashMap();
			String params = "";
			if (method() == HttpMethod.POST)
				params = getContent();
			if (uri().contains("?"))
			{
				if (!params.isEmpty())
					params += "&";
				params += uri().split("\\?")[1];
			}
			String[] split = params.split("&");
			for (String entry : split)
			{
				String[] split2 = entry.split("=", 2);
				try
				{
					if (split2.length == 1 || split2[1].isEmpty())
						parameters.put(URLDecoder.decode(split2[0], "UTF-8"), null);
					else
						parameters.put(URLDecoder.decode(split2[0], "UTF-8"), URLDecoder.decode(split2[1], "UTF-8"));
				}
				catch (UnsupportedEncodingException ex)
				{
					ex.printStackTrace();
				}
			}
		}

		return parameters.get(key);
	}

	public String getContent()
	{
		return content().toString(StandardCharsets.UTF_8);
	}

	public Date getDateParameter(String key)
	{
		try
		{
			return new SimpleDateFormat("yyyy-MM-dd").parse(getParameter(key));
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return new Date();
		}
	}

	public Set<Map.Entry<String, String>> getAllParameters()
	{
		if (parameters == null)
			getParameter("");
		return parameters.entrySet();
	}

	public Player getSession()
	{
		Cookie sess = getCookie("sess-uuid");
		Cookie hash = getCookie("sess-hash");
		if (sess == null || hash == null)
			return null;
		UUID uuid = UUID.fromString(sess.getValue());
		Player player = Server.getPlayer(uuid);
		if (player == null)
			return null;
		if (!player.sha1Pwd.equalsIgnoreCase(hash.getValue()))
			return null;
		return player;
	}

	public Cookie getCookie(String key)
	{
		if (cookies == null)
			cookies = Cookie.parse(this);
		return Arrays.stream(cookies).filter(c -> c.getKey().equalsIgnoreCase(key)).findAny().orElse(null);
	}
}
