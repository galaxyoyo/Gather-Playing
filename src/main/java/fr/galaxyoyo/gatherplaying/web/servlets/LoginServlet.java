package fr.galaxyoyo.gatherplaying.web.servlets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.server.Server;
import fr.galaxyoyo.gatherplaying.web.Cookie;
import fr.galaxyoyo.gatherplaying.web.HttpHeader;
import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

public class LoginServlet extends AbstractWebServlet
{
	@Override
	public void doGet(HttpRequest request, HttpResponse resp)
	{
		String html = "<!doctype html><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><title>";
		Player user = request.getSession();
		if (user == null)
		{
			html += "Connexion</title></head><body><form method=\"POST\"><table><tbody><tr><td><label for=\"user\">Pseudo / e-mail :</label></td><td><input type=\"text\" " +
					"name=\"user\" id=\"user\" /></td></tr>";
			html += "<tr><td><label for=\"password\">Mot de passe :</label></td><td><input type=\"password\" name=\"password\" id=\"password\" /></td></tr>";
			html += "<tr><td colspan=\"2\" align=\"center\"><input type=\"submit\" /></td></tr></tbody></table></body></html>";
		}
		else
		{
			html += "Connecté ! - " + user.name + "</title></head><body>";
			html += "Vous êtes connecté sous le nom de " + user.name + ", votre e-mail est " + user.email + " et votre uuid est " + user.uuid + " !";
			html += "<br />Voici un deck que vous possédez :";
			if (user.decks.isEmpty())
				MySQL.readDecks(user);
			Deck deck = Lists.newArrayList(user.decks).get(new Random().nextInt(user.decks.size()));
			html += "<br />" + deck.getUuid() + "<br />";
			for (OwnedCard card : deck.getAllCards())
			{
				int muId = card.getCard().getPreferredMuID();
				html += "<a style=\"color: blue; text-decoration: none;\" href=\"/card/" + muId + "\">" + card.getTranslatedName().get() + "</a><br />";
			}
		}
		html += "</body></html>";
		resp.setHeader(HttpHeader.CONTENT_LENGTH, html.getBytes(StandardCharsets.UTF_8).length);
		ByteBuf buffer = Unpooled.copiedBuffer(html, CharsetUtil.UTF_8);
		resp.content().writeBytes(buffer);
		buffer.release();
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse resp)
	{
		String id = request.getParameter("user");
		String pwd = request.getParameter("password");
		Player player = MySQL.getPlayer(id);
		if (player != null)
		{
			if (Utils.toSHA1(pwd).equals(player.sha1Pwd))
			{
				Server.connectSuccess(player);
				Calendar cal = new GregorianCalendar();
				cal.add(Calendar.YEAR, 1);
				Cookie sessid = new Cookie("sess-uuid", player.uuid.toString());
				sessid.setExpires(cal.getTime());
				sessid.setHttpOnly(true);
				resp.setCookie(sessid);
				Cookie sesshash = new Cookie("sess-hash", player.sha1Pwd);
				sesshash.setExpires(cal.getTime());
				sesshash.setHttpOnly(true);
				resp.setCookie(sesshash);
				resp.setHeader(HttpHeader.LOCATION, "/login");
				resp.setStatus(HttpResponseStatus.FOUND);
			}
			else
			{
				resp.setStatus(HttpResponseStatus.FORBIDDEN);
				String text = "Invalid password.";
				resp.setHeader(HttpHeader.CONTENT_LENGTH, text.getBytes(StandardCharsets.UTF_8).length);
				ByteBuf buffer = Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
				resp.content().writeBytes(buffer);
				buffer.release();
			}
		}
		else
		{
			resp.setStatus(HttpResponseStatus.FORBIDDEN);
			String text = "Invalid user.";
			resp.setHeader(HttpHeader.CONTENT_LENGTH, text.getBytes(StandardCharsets.UTF_8).length);
			ByteBuf buffer = Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
			resp.content().writeBytes(buffer);
			buffer.release();
		}
	}

	@Override
	public String getPattern()
	{
		return "(?i)^/login$";
	}
}
