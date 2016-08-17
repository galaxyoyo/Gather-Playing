package fr.galaxyoyo.gatherplaying.web.servlets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.MySQL;
import fr.galaxyoyo.gatherplaying.Rarity;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class SetShowerServlet extends AbstractWebServlet
{
	@Override
	public void doGet(HttpRequest request, HttpResponse resp)
	{
		String code = request.uri().substring(5);
		Set set = MySQL.getSet(code);
		if (set == null)
		{
			resp.setStatus(HttpResponseStatus.NOT_FOUND);
			return;
		}

		String html = "<!doctype html><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /><title>";
		html += set.getName();
		html += "</title></head><body><table><tbody>";
		html += "<tr><td><b>Nom :</b></td><td>" + set.getTranslatedName() + "</td></tr>";
		html += "<tr><td><b>Code :</b></td><td>" + set.getCode() + "</td></tr>";
		html += "<tr><td><b>Type :</b></td><td>" + set.getType() + "</td></tr>";
		if (set.getBlock() != null)
			html += "<tr><td><b>Bloc :</b></td><td>" + set.getBlock() + "</td></tr>";
		html += "<tr><td><b>Date de sortie :</b></td><td>" + DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE).format(set.getReleaseDate()) + "</td></tr>";
		html += "<tr><td><b>Symbole d'extension :</b></td><td>";
		for (Rarity r : new Rarity[] {Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.MYTHIC})
		{
			if (set.isPreview())
				html += "<img src=\"http://assets1.mtggoldfish.com/assets/rarity-" + set.getCode().toLowerCase() + "-" + Character.toLowerCase(r.name().charAt(0)) + ".png\" />";
			else
				html += "<img src=\"http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=" + set.getCode() + "&size=small&rarity=" + r.name().charAt(0) + "\" />";
		}
		html += "</td></tr>";
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
		html += "<tr><td><b>Nombre de cartes :</b></td><td>" + cardCount.get() + "</td></tr>";
		html += "<tr><td><b>Cartes :</b></td></tr></tbody></table>";
		List<Card> cards = Lists.newArrayList(set.getCards());
		if (set.isPreview())
			cards.sort((o1, o2) -> Integer.valueOf(o1.getNumber().replaceAll("[^\\d]", "")).compareTo(Integer.valueOf(o2.getNumber().replaceAll("[^\\d]", ""))));
		else
			cards.sort((o1, o2) -> Integer.compare(o1.getMuId("en"), o2.getMuId("en")));
		for (Card card : cards)
		{
			Integer muId = card.getMuId("fr");
			if (muId == null)
				muId = card.getMuId("en");
			html += "<a href=\"/card/" + muId + "\">";
			html += "<img src=\"/render-card?muId=" + card.getMuId("en") + "&locale=fr\" />";
		/*	if (card.isPreview())
				html += "<img src=\"" + card.getImageName() + "\" />";
			else
				html += "<img src=\"http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + muId + "&type=card\" />";*/
			html += "</a>";
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
