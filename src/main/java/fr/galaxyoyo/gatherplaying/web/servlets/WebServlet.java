package fr.galaxyoyo.gatherplaying.web.servlets;

import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;

public interface WebServlet
{
	void doGet(HttpRequest request, HttpResponse resp);

	void doPost(HttpRequest request, HttpResponse resp);

	String getPattern();
}
