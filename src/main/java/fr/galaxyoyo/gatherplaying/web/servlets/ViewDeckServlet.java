package fr.galaxyoyo.gatherplaying.web.servlets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.web.HttpHeader;
import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ViewDeckServlet extends AbstractWebServlet
{
	@Override
	public void doGet(HttpRequest request, HttpResponse resp)
	{
		UUID uuid = UUID.fromString(request.uri().substring(11));
		Deck deck = MySQL.getDeck(uuid);
		String html = "<!doctype html><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/><title>";
		assert deck != null;
		html += deck.getName() + "</title></head><body>";
		html += "<h1>" + deck.getName();
		for (ManaColor color : deck.getColors())
			html += CardShowerServlet.getManaCostImageURL(color.getAbbreviate().replace("/", ""));
		html += "</h1><div id=\"description\">" + deck.getDesc().replace("\n", "<br />") + "</div>";
		html += "<h3>Cartes (" + deck.getCards().size() + ")</h3>";
		List<OwnedCard> cards = Lists.newArrayList(deck.getCards());
		cards.sort((o1, o2) -> o1.getCard().compareTo(o2.getCard()));
		for (OwnedCard card : cards)
		{
			html += "<a href=\"/card/" + card.getCard().getPreferredMuID() + "/\"><img src=\"";
			if (card.getCard().isPreview())
				html += card.getCard().getImageName();
			else
				html += "http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + card.getCard().getPreferredMuID() + "&type=card";
			html += "\" /></a>";
		}
		html += "<h3>Réserve (" + deck.getSideboard().size() + ")</h3>";
		cards = Lists.newArrayList(deck.getSideboard());
		cards.sort((o1, o2) -> o1.getCard().compareTo(o2.getCard()));
		for (OwnedCard card : cards)
		{
			html += "<a href=\"/card/" + card.getCard().getPreferredMuID() + "\"><img src=\"";
			if (card.getCard().isPreview())
				html += card.getCard().getImageName();
			else
				html += "http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + card.getCard().getPreferredMuID() + "&type=card";
			html += "\" /></a>";
		}
		html += "<h3>Légal en :</h3><ul>";
		for (Rules rules : deck.getLegalities())
		{
			if (!rules.isLimited())
				html += "<li>" + rules.getTranslatedName().get() + "</li>";
		}
		html += "</ul></body></html>";
		resp.setHeader(HttpHeader.CONTENT_LENGTH, html.getBytes(StandardCharsets.UTF_8).length);
		ByteBuf buffer = Unpooled.copiedBuffer(html, CharsetUtil.UTF_8);
		resp.content().writeBytes(buffer);
		buffer.release();
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse resp)
	{
	}

	@Override
	public String getPattern()
	{
		return "(?i)^/view-deck/";
	}

	@Override
	public Date getLastModifiedDate()
	{
		try
		{
			return new SimpleDateFormat("dd-MM-yyyy HH:mm").parse("11-07-2016 22:35");
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return super.getLastModifiedDate();
		}
	}
}
