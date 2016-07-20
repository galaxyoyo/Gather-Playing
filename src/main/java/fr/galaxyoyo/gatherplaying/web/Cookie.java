package fr.galaxyoyo.gatherplaying.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Cookie
{
	private static final DateFormat FORMAT = new SimpleDateFormat("DDD, dd-MMM-yyyy HH:mm:ss Z", Locale.US);
	private final String key;
	private final String value;
	private Date expires;
	private String path;
	private String domain;
	private boolean httpOnly;

	public Cookie(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	protected static Cookie[] parse(HttpRequest req)
	{
		String all = req.getHeader(HttpHeader.COOKIE);
		if (all == null)
			return new Cookie[0];
		String[] split = all.split(";");
		Cookie[] cookies = new Cookie[split.length];
		for (int i = 0; i < split.length; i++)
		{
			String toParse = split[i];
			toParse = toParse.trim();
			String[] splitted = toParse.split("=");
			Cookie c = new Cookie(splitted[0], splitted[1]);
			cookies[i] = c;
		}
		return cookies;
	}

	public String getKey()
	{
		return key;
	}

	public String getValue()
	{
		return value;
	}

	public Date getExpires()
	{
		return expires;
	}

	public void setExpires(Date expires)
	{
		this.expires = expires;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public boolean isHttpOnly()
	{
		return httpOnly;
	}

	public void setHttpOnly(boolean httpOnly)
	{
		this.httpOnly = httpOnly;
	}

	@Override
	public String toString()
	{
		try
		{
			String str = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
			if (expires != null)
				str += "; expires=" + FORMAT.format(expires);
			if (path != null)
				str += "; path=" + URLEncoder.encode(path, "UTF-8");
			if (domain != null)
				str += "; domain=" + URLEncoder.encode(domain, "UTF-8");
			if (httpOnly)
				str += "; HttpOnly";
			return str;
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return super.toString();
	}
}
