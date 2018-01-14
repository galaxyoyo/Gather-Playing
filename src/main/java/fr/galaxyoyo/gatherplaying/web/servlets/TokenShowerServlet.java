package fr.galaxyoyo.gatherplaying.web.servlets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Config;
import fr.galaxyoyo.gatherplaying.web.HttpHeader;
import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import java8.util.stream.RefStreams;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

public class TokenShowerServlet extends AbstractWebServlet
{
	@Override
	public void doGet(HttpRequest request, HttpResponse resp)
	{
		String path = request.uri().substring(7);
		Set set = MySQL.getSet(path.split("/")[0]);
		Token token = RefStreams.of(Token.values()).filter(t -> t.getSet() == set && t.getNumber().equals(path.split("/")[1])).findAny().get();

		String locale = Config.getLocaleCode();
		StringBuilder html = new StringBuilder("<!doctype html><html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /><title>");
		html.append(token.getTranslatedName().get());
		html.append("</title></head><body><table><tbody><tr><td>");
		html.append("<img src=\"/render-token?token=").append(token.name().toLowerCase()).append("&locale=").append(locale).append("\" />");
	/*	if (card.isPreview())
			html += "<img src=\"" + card.getImageName() + "\" />";
		else
			html += "<img src=\"http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + card.getMuId(locale) + "&type=card\" />";*/
		html.append("</td><td><table><tbody><tr><td><b>Nom de la carte :</b></td><td>").append(token.getTranslatedName().get()).append("</td></tr>");

		html.append("<tr><td><b>Couleur").append(token.getColor().length > 1 ? "s" : "").append(" :</b></td><td>");
		for (int i = 0; i < token.getColor().length; i++)
		{
			ManaColor color = token.getColor()[i];
			html.append(color.getTranslatedName().get());
			if (i + 1 != token.getColor().length)
				html.append(", ");
		}
		html.append("<tr><td><b>Type :</b></td><td>");
		html.append(token.getType().getTranslatedName().get());
		if (token.isLegendary())
			html.append(" légendaire");
		if (token.getSubtypes().length > 0)
		{
			html.append(" : ");
			for (int i = 0; i < token.getSubtypes().length; i++)
			{
				SubType st = token.getSubtypes()[i];
				if (token.getType().is(CardType.PLANESWALKER))
					html.append(st.getTranslatedName().get());
				else
					html.append(st.getTranslatedName().get().toLowerCase());
				if (i + 1 != token.getSubtypes().length)
					html.append(" et ");
			}
		}
		html.append("</td></tr>");
		if (!token.getAbility_EN().isEmpty())
		{
			html.append("<tr><td><b>Texte :</b></td><td>");
			String ability = locale.equals("fr") ? token.getAbility_FR() : token.getAbility_EN();
			for (String line : ability.split("£|\n"))
			{
				line = line.replace("#_", "<i>").replace("_#", "</i>");
				Matcher m = CardAdapter.MANA_COST.matcher(line);
				while (m.find())
				{
					String icon = m.group().substring(1, m.group().length() - 1);
					line = line.replace(m.group(), getManaCostImageURL(icon));
				}
				html.append(line.replace("(", "<i>(").replace(")", ")</i>")).append("<br />");
			}
			html.append("</td></tr>");
		}
		if (token.getType().is(CardType.CREATURE))
			html.append("<tr><td><b>Force / Endurance :</b></td><td>").append(token.getPower()).append("/").append(token.getToughness()).append("</td></tr>");
		int max = (int) RefStreams.of(Token.values()).filter(t -> t.getSet() == token.getSet()).count();
			html.append("<tr><td><b>Numéro de carte :</b></td><td>").append(token.getNumber()).append("/").append(max).append("</td></tr>");
		html.append("<tr><td><b>Artiste :</b></td><td>" + /*card.getArtist() +*/ "</td></tr>");
		html.append("<tr><td><b>Extension :</b></td><td><a style=\"color: black; text-decoration: none;\" href=\"/set/").append(token.getSet().getCode()).append("\">")
				.append(token.getSet().getTranslatedName());
		if (token.getSet().isPreview())
			html.append("<img src=\"http://assets1.mtggoldfish.com/assets/rarity-").append(token.getSet().getCode().toLowerCase()).append("-c.png\" />");
		else
			html.append("<img src=\"http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=").append(token.getSet().getCode()).append("&size=small&rarity=C\" />");
		html.append("</a></td></tr>");
		html.append("</tbody></table></td></tr></tbody></table>");
		html.append("</body></html>");
		resp.setHeader(HttpHeader.CONTENT_LENGTH, html.toString().getBytes(StandardCharsets.UTF_8).length);
		ByteBuf buffer = Unpooled.copiedBuffer(html.toString(), CharsetUtil.UTF_8);
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
		return "(?i)^/token/.*?/.*?";
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
