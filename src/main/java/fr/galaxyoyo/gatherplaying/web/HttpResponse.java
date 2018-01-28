package fr.galaxyoyo.gatherplaying.web;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HttpResponse extends DefaultFullHttpResponse
{
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

	public HttpResponse()
	{
		super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	}

	public HttpResponse(HttpResponseStatus status, ByteBuf content)
	{
		super(HttpVersion.HTTP_1_1, status, content);
	}

	public void setHeader(HttpHeader header, HttpHeader.Values value)
	{
		setHeader(header, value.getName());
	}

	public void setHeader(HttpHeader header, String value)
	{
		List<CharSequence> list = Lists.newArrayList(headers().getAll(header.getName()));
		list.add(value);
		headers().set(header.getName(), list);
	}

	public boolean getHeaderBoolean(HttpHeader header)
	{
		return Boolean.getBoolean(headers().get(header.getName()));
	}

	public void setHeader(HttpHeader header, boolean value)
	{
		headers().set(header.getName(), value);
	}

	public int getHeaderInt(HttpHeader header)
	{
		return headers().getInt(header.getName());
	}

	public void setHeader(HttpHeader header, int value)
	{
		headers().setInt(header.getName(), value);
	}

	public double getHeaderDouble(HttpHeader header)
	{
		return Double.parseDouble(headers().get(header.getName()));
	}

	public void setHeader(HttpHeader header, double value)
	{
		headers().get(header.getName(), Double.toString(value));
	}

	public long getHeaderLong(HttpHeader header)
	{
		return Long.parseLong(headers().get(header.getName()));
	}

	public void setHeader(HttpHeader header, long value)
	{
		headers().set(header.getName(), value);
	}

	public Date getHeaderDate(HttpHeader header)
	{
		try
		{
			return DATE_FORMATTER.parse(getHeader(header));
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return new Date();
		}
	}

	public String getHeader(HttpHeader header)
	{
		return headers().get(header.getName()).toString();
	}

	public void setHeader(HttpHeader header, Date value)
	{
		setHeader(header, DATE_FORMATTER.format(value));
	}

	public void setContentType(String contentType)
	{
		headers().remove(HttpHeader.CONTENT_TYPE.getName());
		setHeader(HttpHeader.CONTENT_TYPE, contentType);
	}

	public void setCookie(Cookie c)
	{
		setHeader(HttpHeader.SET_COOKIE, c.toString());
	}
}
