package fr.galaxyoyo.gatherplaying.web.servlets;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public interface WebServlet
{
	void doGet(FullHttpRequest request, FullHttpResponse resp);

	void doPost(FullHttpRequest request, FullHttpResponse resp);

	String getPattern();
}
