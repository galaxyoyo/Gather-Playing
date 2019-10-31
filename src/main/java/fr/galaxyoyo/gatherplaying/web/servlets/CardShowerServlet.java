package fr.galaxyoyo.gatherplaying.web.servlets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.web.HttpHeader;
import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class CardShowerServlet extends AbstractWebServlet
{
	@Override
	public void doGet(HttpRequest request, HttpResponse resp)
	{
		int muId = Integer.parseInt(request.uri().substring(6));
		Card card = MySQL.getAllCards().stream().filter(c -> c.getMuId().values().contains(muId)).findAny().orElse(null);
		if (card == null)
		{
			resp.setStatus(HttpResponseStatus.NOT_FOUND);
			return;
		}
		String locale = card.getMuId().entrySet().stream().filter(entry -> muId == entry.getValue()).findAny().get().getKey();
		String html = "<!doctype html><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /><title>";
		html += card.getName().get(locale);
		html += "</title></head><body><table><tbody><tr><td>";
		html += "<img src=\"/render-card?muId=" + card.getMuId("en") + "&locale=" + locale + "\" />";
	/*	if (card.isPreview())
			html += "<img src=\"" + card.getImageName() + "\" />";
		else
			html += "<img src=\"http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + card.getMuId(locale) + "&type=card\" />";*/
		html += "</td><td><table><tbody><tr><td><b>Nom de la carte :</b></td><td>" + card.getName().get(locale) + "</td></tr>";
		if (card.getManaCost() != null)
		{
			html += "<tr><td><b>Coût de mana :</b></td><td>";
			for (ManaColor cost : card.getManaCost())
				html += getManaCostImageURL(cost.getAbbreviate().replace("/", ""));
			html += "</td></tr>";
		}
		html += "<tr><td><b>Coût converti de mana :</b></td><td>" + (int) card.getCmc() + "</td></tr>";
		html += "<tr><td><b>Couleur" + (card.getColors().length > 1 ? "s" : "") + " :</b></td><td>";
		for (int i = 0; i < card.getColors().length; i++)
		{
			ManaColor color = card.getColors()[i];
			html += color.getTranslatedName().get();
			if (i + 1 != card.getColors().length)
				html += ", ";
		}
		html += "<tr><td><b>Type :</b></td><td>";
		html += card.getType().getTranslatedName().get();
		if (card.isLegendary())
			html += " légendaire";
		else if (card.isBasic())
			html += " de base";
		else if (card.isSnow())
			html += " neigeux";
		else if (card.isOngoing())
			html += " ongoing";
		else if (card.isWorld())
			html += " du monde";
		if (card.getSubtypes().length > 0)
		{
			html += " : ";
			for (int i = 0; i < card.getSubtypes().length; i++)
			{
				SubType st = card.getSubtypes()[i];
				if (card.getType().is(CardType.PLANESWALKER))
					html += st.getTranslatedName().get();
				else
					html += st.getTranslatedName().get().toLowerCase();
				if (i + 1 != card.getSubtypes().length)
					html += " et ";
			}
		}
		html += "</td></tr>";
		if (card.getAbilityMap().get("en") != null)
		{
			html += "<tr><td><b>Texte :</b></td><td>";
			String ability = card.getAbilityMap().get(locale);
			if (ability == null)
				ability = card.getAbilityMap().get("en");
			for (String line : ability.split("£|\n"))
			{
				line = line.replace("#_", "<i>").replace("_#", "</i>");
				Matcher m = CardAdapter.MANA_COST.matcher(line);
				while (m.find())
				{
					String icon = m.group().substring(1, m.group().length() - 1);
					line = line.replace(m.group(), getManaCostImageURL(icon));
				}
				html += line.replace("(", "<i>(").replace(")", ")</i>") + "<br />";
			}
			html += "</td></tr>";
		}
		if (card.getFlavorMap().get("en") != null)
		{
			String flavor = card.getFlavorMap().get(locale);
			if (flavor == null)
				flavor = card.getFlavorMap().get("en");
			html += "<tr><td><b>Texte d'ambiance :</b></td><td><i>" + flavor + "</i></td><tr>";
		}
		if (card.getWatermark() != null)
			html += "<tr><td><b>Watermark :</b></td><td>" + card.getWatermark() + "</td></tr>";
		if (card.getType().is(CardType.CREATURE))
			html += "<tr><td><b>Force / Endurance :</b></td><td>" + card.getPower() + "/" + card.getToughness() + "</td></tr>";
		else if (card.getType().is(CardType.PLANESWALKER))
			html += "<tr><td><b>Loyauté :</b></td><td>" + card.getLoyalty() + "</td></tr>";
		html += "<tr><td><b>Rareté :</b></td><td>" + card.getRarity().getTranslatedName().get() + "</td></tr>";
		if (card.getNumber() != null)
		{
			AtomicInteger cardCount = new AtomicInteger();
			card.getSet().getCards().forEach(c ->
			{
				int number = Integer.parseInt(c.getNumber().replaceAll("[^\\d]", ""));
				if (number > cardCount.get())
					cardCount.set(number);
			});
			html += "<tr><td><b>Numéro de carte :</b></td><td>" + card.getNumber() + "/" + cardCount.get() + "</td></tr>";
		}
		html += "<tr><td><b>Artiste :</b></td><td>" + card.getArtist() + "</td></tr>";
		html += "<tr><td><b>Extension :</b></td><td><a style=\"color: black; text-decoration: none;\" href=\"/set/" + card.getSet().getCode() + "\">" + card.getSet()
				.getTranslatedName();
		if (card.isPreview())
			html += "<img src=\"http://assets1.mtggoldfish.com/assets/rarity-" + card.getSet().getCode().toLowerCase() + "-" + (card.isBasic() ? "c" : card.getRarity().name()
					.substring(0, 1).toLowerCase()) + ".png\" />";
		else
			html += "<img src=\"http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=" + card.getSet().getCode() + "&size=small&rarity=" + (card.isBasic() ? "L" : card
					.getRarity().name().charAt(0)) + "\" />";
		html += "</a></td></tr>";
		html += "</tbody></table></td></tr></tbody></table>";
		html += "<table><thead><th>Langue</th><th>Carte</th></thead><tbody>";
		StringBuilder languages = new StringBuilder();
		card.getMuId().forEach((l, m) ->
		{
			if (m == null)
				return;
			languages.append("<tr><td>");
			String[] split = l.split("_");
			Locale loc;
			if (split.length >= 2)
				loc = new Locale(split[0], split[1]);
			else
				loc = new Locale(split[0]);
			String languageName = loc.getDisplayName(loc);
			languages.append(Character.toUpperCase(languageName.charAt(0))).append(languageName.substring(1));
			languages.append("</td><td>");
			languages.append("<a style=\"color: blue; text-decoration: none;\" href=\"/card/").append(m).append("\">").append(card.getName().get(l)).append("</a></td></tr>");
		});
		html += languages;
		html += "</tbody></table>";
		StringBuilder reprints = new StringBuilder();
		List<Set> sets = MySQL.getAllSets().stream().filter(set -> set != card.getSet()).collect(Collectors.toList());
		sets.sort(Set::compareTo);
		Collections.reverse(sets);
		if (!sets.isEmpty())
			html += "<table><thead><th>Nom de la carte</th><th>Édition</th></thead><tbody>";
		sets.forEach(set -> set.getCards().stream().filter(c -> c.getName().get("en").equals(card.getName().get("en"))).forEach(c ->
		{
			String l = locale;
			reprints.append("<tr><td>");
			Integer m = c.getMuId().get(locale);
			if (m == null)
			{
				m = c.getMuId().get("en");
				l = "en";
			}
			reprints.append("<a style=\"color: blue; text-decoration: none;\" href=\"/card/").append(m).append("\">").append(c.getName().get(l)).append("</a></td><td>");
			reprints.append("<a style=\"color: blue; text-decoration: none;\" href=\"/set/").append(c.getSet().getCode()).append("\">").append(c.getSet().getTranslatedName());
			if (c.isPreview())
				reprints.append("<img src=\"http://assets1.mtggoldfish.com/assets/rarity-").append(c.getSet().getCode().toLowerCase()).append("-")
						.append(c.isBasic() ? "c" : c.getRarity().name().substring(0, 1).toLowerCase()).append(".png\" />");
			else
				reprints.append("<img src=\"http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=").append(c.getSet().getCode()).append("&size=small&rarity=")
						.append(c.isBasic() ? "L" : c.getRarity().name().charAt(0)).append("\" />");
			reprints.append("</a></td></tr>");
		}));
		html += reprints;
		if (!sets.isEmpty())
			html += "</tbody></table></body></html>";
		resp.setHeader(HttpHeader.CONTENT_LENGTH, html.getBytes(StandardCharsets.UTF_8).length);
		ByteBuf buffer = Unpooled.copiedBuffer(html, CharsetUtil.UTF_8);
		resp.content().writeBytes(buffer);
		buffer.release();
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse resp)
	{
		doGet(request, resp);
	}

	@Override
	public String getPattern()
	{
		return "(?i)^/card/.*?";
	}

	public static String getManaCostImageURL(String id)
	{
		String url = "http://www.magiccorporation.com/images/magic/manas/mini/" + id.toLowerCase() + ".gif";
		if (id.equals("1000000"))
			url = "http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=" + id + "&type=symbol";
		else if (id.contains("10") || id.contains("Infinity") || id.contains("Half") || id.contains("P"))
			url = "http://gatherer.wizards.com/Handlers/Image.ashx?size=medium&name=" + id + "&type=symbol";
		return "<img src=\"" + url + "\" alt=\"" + id + "\" title=\"" + id + "\">";
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
