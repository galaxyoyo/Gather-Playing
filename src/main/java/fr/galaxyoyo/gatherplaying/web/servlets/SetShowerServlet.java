package fr.galaxyoyo.gatherplaying.web.servlets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.Set;
import fr.galaxyoyo.gatherplaying.web.HttpHeader;
import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SetShowerServlet extends AbstractWebServlet
{
	@Override
	public void doGet(HttpRequest request, HttpResponse resp)
	{
		String locale = "fr";
		String code = request.uri().substring(5);
		Set set = MySQL.getSet(code);
		if (set == null)
		{
			resp.setStatus(HttpResponseStatus.NOT_FOUND);
			return;
		}

		StringBuilder html = new StringBuilder("<!doctype html><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /><title>");
		html.append(set.getName());
		html.append("</title></head><body><table><tbody>");
		html.append("<tr><td><b>Nom :</b></td><td>").append(set.getTranslatedName()).append("</td></tr>");
		html.append("<tr><td><b>Code :</b></td><td>").append(set.getCode()).append("</td></tr>");
		html.append("<tr><td><b>Type :</b></td><td>").append(set.getType()).append("</td></tr>");
		if (set.getBlock() != null)
			html.append("<tr><td><b>Bloc :</b></td><td>").append(set.getBlock()).append("</td></tr>");
		html.append("<tr><td><b>Date de sortie :</b></td><td>").append(DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE).format(set.getReleaseDate())).append("</td></tr>");
		html.append("<tr><td><b>Symbole d'extension :</b></td><td>");
		for (Rarity r : new Rarity[] {Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.MYTHIC})
		{
			if (set.isPreview())
				html.append("<img src=\"http://assets1.mtggoldfish.com/assets/rarity-").append(set.getCode().toLowerCase()).append("-").append(Character.toLowerCase(r.name().charAt
						(0)))
						.append(".png\" />");
			else
				html.append("<img src=\"http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=").append(set.getCode()).append("&size=small&rarity=").append(r.name().charAt
						(0))
						.append("\" />");
		}
		html.append("</td></tr>");
		AtomicInteger cardCount = new AtomicInteger();
		set.getCards().forEach(c ->
		{
			if (c.getNumber() == null)
				return;
			int number = Integer.parseInt(c.getNumber().replaceAll("[^\\d]", ""));
			if (number > cardCount.get())
				cardCount.set(number);
		});
		if (cardCount.get() == 0)
			cardCount.set(set.getCards().size());
		html.append("<tr><td><b>Nombre de cartes :</b></td><td>").append(cardCount.get()).append("</td></tr>");
		html.append("<tr><td><b>Cartes :</b></td></tr></tbody></table>");
		List<Card> cards = Lists.newArrayList(set.getCards());
		if (set.isPreview())
			cards.sort(Comparator.comparing(o -> Integer.valueOf(o.getNumber().replaceAll("[^\\d]", ""))));
		else
			cards.sort(Comparator.comparingInt(o -> o.getMuId("en")));
		for (Card card : cards)
		{
			Integer muId = card.getMuId("fr");
			if (muId == null)
			{
				muId = card.getMuId("en");
				locale = "en";
			}
			System.out.println(locale);
			html.append("<a href=\"/card/").append(muId).append("\">");
			html.append("<img src=\"/render-card?muId=").append(card.getMuId("en")).append("&locale=").append(locale).append("\" />");
		/*	if (card.isPreview())
				html += "<img src=\"" + card.getImageName() + "\" />";
			else
				html += "<img src=\"http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + muId + "&type=card\" />";*/
			html.append("</a>\n");
		}
		List<Token> tokens = Arrays.stream(Token.values()).filter(t -> t.getSet() == set).collect(Collectors.toList());
		for (Token token : tokens)
		{
			html.append("<a href=\"/token/").append(token.getSet().getCode()).append("/").append(token.getNumber()).append("\"/>");
			html.append("<img src=\"/render-token?token=").append(token.name().toLowerCase()).append("&locale=fr\" />");
			html.append("</a>\n");
		}
		html.append("</body></html>");
		resp.setHeader(HttpHeader.CONTENT_LENGTH, html.toString().getBytes(StandardCharsets.UTF_8).length);
		ByteBuf buffer = Unpooled.copiedBuffer(html.toString(), CharsetUtil.UTF_8);
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
		return "(?i)^/set/.*?";
	}

	@Override
	public Date getLastModifiedDate()
	{
		try
		{
			return new SimpleDateFormat("dd-MM-yyyy HH:mm").parse("04-08-2016 17:42");
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return super.getLastModifiedDate();
		}
	}
}
