package fr.galaxyoyo.gatherplaying.web;

public enum HttpHeader
{
	ACCEPT("Accept"),
	ACCEPT_CHARSET("Accept-Charset"),
	ACCEPT_ENCODING("Accept-Encoding"),
	ACCEPT_LANGUAGE("Accept-Language"),
	ACCEPT_RANGES("Accept-Ranges"),
	ACCEPT_PATCH("Accept-Patch"),
	ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),
	ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),
	ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
	ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
	ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),
	ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),
	ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers"),
	ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method"),
	AGE("Age"),
	ALLOW("Allow"),
	AUTHORIZATION("Authorization"),
	CACHE_CONTROL("Cache-Control"),
	CONNECTION("Connection"),
	CONTENT_BASE("Content-Base"),
	CONTENT_ENCODING("Content-Encoding"),
	CONTENT_LANGUAGE("Content-Language"),
	CONTENT_LENGTH("Content-Length"),
	CONTENT_LOCATION("Content-Location"),
	CONTENT_TRANSFER_ENCODING("Content-Transfer-Encoding"),
	CONTENT_DISPOSITION("Content-Disposition"),
	CONTENT_MD5("Content-MD5"),
	CONTENT_RANGE("Content-Range"),
	CONTENT_TYPE("Content-Type"),
	COOKIE("Cookie"),
	DATE("Date"),
	ETAG("Etag"),
	EXPECT("Expect"),
	EXPIRES("Expires"),
	FROM("From"),
	HOST("Host"),
	IF_MATCH("If-Match"),
	IF_MODIFIED_SINCE("If-Modified-Since"),
	IF_NONE_MATCH("If-None-Match"),
	IF_RANGE("If-Range"),
	IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
	KEEP_ALIVE("Keep-Alive"),
	LAST_MODIFIED("Last-Modified"),
	LOCATION("Location"),
	MAX_FORWARDS("Max-Forwards"),
	ORIGIN("Origin"),
	PRAGMA("Pragma"),
	PROXY_AUTHENTICATE("Proxy-Authenticate"),
	PROXY_AUTHORIZATION("Proxy-Authorization"),
	PROXY_CONNECTION("Proxy-Connection"),
	RANGE("Range"),
	REFERER("Referer"),
	RETRY_AFTER("Retry-After"),
	SEC_WEBSOCKET_KEY1("Sec-Websocket-Key1"),
	SEC_WEBSOCKET_KEY2("Sec-Websocket-Key2"),
	SEC_WEBSOCKET_LOCATION("Sec-Websocket-Location"),
	SEC_WEBSOCKET_ORIGIN("Sec-Websocket-Origin"),
	SEC_WEBSOCKET_PROTOCOL("Sec-Websocket-Protocol"),
	SEC_WEBSOCKET_VERSION("Sec-Websocket-Version"),
	SEC_WEBSOCKET_KEY("Sec-Websocket-Key"),
	SEC_WEBSOCKET_ACCEPT("Sec-Websocket-Accept"),
	SEC_WEBSOCKET_EXTENSIONS("Sec-Websocket-Extensions"),
	SERVER("Server"),
	SET_COOKIE("Set-Cookie"),
	SET_COOKIE2("Set-Cookie2"),
	TE("Te"),
	TRAILER("Trailer"),
	TRANSFER_ENCODING("Transfer-Encoding"),
	UPGRADE("Upgrade"),
	USER_AGENT("User-Agent"),
	VARY("Vary"),
	VIA("Via"),
	WARNING("Warning"),
	WEBSOCKET_LOCATION("Websocket-Location"),
	WEBSOCKET_ORIGIN("Websocket-Origin"),
	WEBSOCKET_PROTOCOL("Websocket-Protocol"),
	WWW_AUTHENTICATE("WWW-Authenticate");

	private final String name;

	HttpHeader(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public enum Values
	{
		APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
		APPLICATION_OCTET_STREAM("application/octet-stream"),
		ATTACHMENT("attachment"),
		BASE64("base64"),
		BINARY("binary"),
		BOUNDARY("boundary"),
		BYTES("bytes"),
		CHARSET("charset"),
		CHUNKED("chunked"),
		CLOSE("close"),
		COMPRESS("compress"),
		CONTINUE("100-continue"),
		DEFLATE("deflate"),
		X_DEFLATE("x-deflate"),
		FILE("file"),
		FILENAME("filename"),
		FORM_DATA("form-data"),
		GZIP("gzip"),
		X_GZIP("x-gzip"),
		IDENTITY("identity"),
		KEEP_ALIVE("keep-alive"),
		MAX_AGE("max-age"),
		MAX_STALE("max-stale"),
		MIN_FRESH("min-fresh"),
		MULTIPART_FORM_DATA("multipart/form-data"),
		MULTIPART_MIXED("multipart/mixed"),
		MUST_REVALIDATE("must-revalidate"),
		NAME("name"),
		NO_CACHE("no-cache"),
		NO_STORE("no-store"),
		NO_TRANSFORM("no-transform"),
		NONE("none"),
		ONLY_IF_CACHED("only-if-cached"),
		PRIVATE("private"),
		PROXY_REVALIDATE("proxy-revalidate"),
		PUBLIC("public"),
		QUOTED_PRINTABLE("quoted-printable"),
		S_MAXAGE("s-maxage"),
		TEXT_PLAIN("text/plain"),
		TRAILERS("trailers"),
		UPGRADE("Upgrade"),
		WEBSOCKET("websocket");

		private final String name;

		Values(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}
}
