package fr.galaxyoyo.gatherplaying.web.servlets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.web.HttpHeader;
import fr.galaxyoyo.gatherplaying.web.HttpRequest;
import fr.galaxyoyo.gatherplaying.web.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;

public class RegisterCardServlet extends SimpleHtmlReaderServlet
{
	public RegisterCardServlet()
	{
		super("(?i)^/register-card$", "/web/register-card.html");
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse resp)
	{
		String name = request.getParameter("name");
		String name_fr = request.getParameter("name_fr");
		Set set = MySQL.getSet(request.getParameter("set"));
		String manaCostStr = request.getParameter("manaCost");
		ManaColor[] cost = null;
		if (manaCostStr != null)
		{
			Matcher m = CardAdapter.MANA_COST.matcher(manaCostStr);
			cost = new ManaColor[0];
			int id = -1;
			while (m.find())
			{
				String str = m.group().substring(1, m.group().length() - 1).toUpperCase().replace("H", "Half");
				cost = Arrays.copyOf(cost, cost.length + 1);
				cost[++id] = ManaColor.getBySignificant(str);
				if (cost[id] == null)
					System.err.println(str);
			}
		}
		int cmc = request.getIntParameter("cmc");
		String[] colorsSplit = request.getParameter("colors").split("/");
		ManaColor[] colors = new ManaColor[colorsSplit.length];
		for (int i = 0; i < colorsSplit.length; i++)
			colors[i] = ManaColor.getBySignificant(colorsSplit[i]);
		CardType type = CardType.valueOf(request.getParameter("type").toUpperCase());
		String[] subtypesSplit = request.getParameter("subtypes") == null ? new String[0] : request.getParameter("subtypes").split(" et ");
		SubType[] subtypes = new SubType[subtypesSplit.length];
		for (int i = 0; i < subtypes.length; i++)
		{
			String stName = subtypesSplit[i];
			subtypes[i] = Arrays.stream(SubType.values()).filter(st -> st.getTranslatedName().get().equalsIgnoreCase(stName)).findAny().orElse(SubType.valueOf(stName));
		}
		String ability = request.getParameter("ability");
		String ability_fr = request.getParameter("ability_fr");
		String flavor = request.getParameter("flavor");
		String flavor_fr = request.getParameter("flavor_fr");
		String power = request.getParameter("power");
		String toughness = request.getParameter("toughness");
		int loyalty = request.getIntParameter("loyalty");
		Rarity rarity = Rarity.valueOf(request.getParameter("rarity").toUpperCase());
		Layout layout = Layout.valueOf(request.getParameter("layout").toUpperCase());
		String supertype = request.getParameter("supertype");
		String artist = request.getParameter("artist");
		String watermark = request.getParameter("watermark");
		String number = request.getParameter("number");
		String imageURL = request.getParameter("imageURL");

		Card card;
		if (request.getParameter("edit") != null)
			card = MySQL.getAllCards().stream().filter(c -> c.getMuId().values().contains(request.getIntParameter("edit"))).findAny().get();
		else
			card = new Card();
		card.getName().put("en", name);
		card.getName().put("fr", name_fr);
		card.setSet(set);
		card.setManaCost(cost);
		card.setCmc(cmc);
		card.setColors(colors);
		card.setType(type);
		card.setSubtypes(subtypes);
		card.getAbilityMap().put("en", ability);
		card.getAbilityMap().put("fr", ability_fr);
		card.getFlavorMap().put("en", flavor);
		card.getFlavorMap().put("fr", flavor_fr);
		if (type.is(CardType.CREATURE))
		{
			card.setPower(power);
			card.setToughness(toughness);
		}
		else if (type.is(CardType.PLANESWALKER))
			card.setLoyalty(loyalty);
		card.setRarity(rarity);
		card.setLayout(layout);
		switch (supertype)
		{
			case "basic":
				card.setBasic(true);
				break;
			case "legendary":
				card.setLegendary(true);
				break;
		}
		card.setArtist(artist);
		card.setWatermark(watermark);
		card.setNumber(number);
		card.setImageName(imageURL);
		card.setPreview();
		set.getCards().add(card);
		if (request.getParameter("edit") != null)
			MySQL.updateCard(card);
		else
			MySQL.addCard(card);

		String text = "Carte ajoutée avec succès !";
		resp.setHeader(HttpHeader.CONTENT_LENGTH, text.getBytes(StandardCharsets.UTF_8).length);
		ByteBuf buffer = Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
		resp.content().writeBytes(buffer);
		buffer.release();
	}

	@Override
	public String processReplaces(HttpRequest request, String text)
	{
		if (request.getParameter("edit") != null)
		{
			Card card = MySQL.getAllCards().stream().filter(c -> c.getMuId().values().contains(request.getIntParameter("edit"))).findAny().get();
			text = text.replaceFirst("value=\"\"", "value=\"" + card.getName().get("en") + "\"");
			text = text.replaceFirst("value=\"\"", "value=\"" + card.getName().get("fr") + "\"");
			text = text.replaceFirst("<option value=\"" + card.getSet().getCode() + "\">", "<option value=\"" + card.getSet().getCode() + "\" selected>");
			if (card.getManaCost() != null)
			{
				String manaCostStr = "";
				for (ManaColor cost : card.getManaCost())
					manaCostStr += "{" + cost.getAbbreviate() + "}";
				text = text.replaceFirst("value=\"\"", "value=\"" + manaCostStr + "\"");
			}
			else
				text = text.replaceFirst("value=\"\"", "value=\"¶\"");
			text = text.replaceFirst("value=\"\"", "value=\"" + (int) card.getCmc() + "\"");
			String colorStr = "";
			for (ManaColor cost : card.getColors())
				colorStr += "/" + cost.getAbbreviate();
			text = text.replaceFirst("<option value=\"" + colorStr.substring(1) + "\">", "<option value=\"" + colorStr.substring(1) + "\" selected>");
			text = text.replaceFirst("<option value=\"" + card.getType().name().toLowerCase() + "\">", "<option value=\"" + card.getType().name().toLowerCase() + "\" selected>");
			String stStr = "";
			for (SubType st : card.getSubtypes())
				stStr += " et " + (st == null ? "null" : st.getTranslatedName().get().toLowerCase());
			text = text.replaceFirst("value=\"\"", "value=\"" + (stStr.isEmpty() ? "¶" : stStr.substring(4)) + "\"");
			text = text.replaceFirst("></textarea>", ">" + StringUtils.defaultIfEmpty(card.getAbilityMap().get("en"), "¶") + "</textarea>");
			text = text.replaceFirst("></textarea>", ">" + StringUtils.defaultIfEmpty(card.getAbilityMap().get("fr"), "¶") + "</textarea>");
			text = text.replaceFirst("></textarea>", ">" + StringUtils.defaultIfEmpty(card.getFlavorMap().get("en"), "¶") + "</textarea>");
			text = text.replaceFirst("></textarea>", ">" + StringUtils.defaultIfEmpty(card.getFlavorMap().get("fr"), "¶") + "</textarea>");
			text = text.replaceFirst("value=\"\"", "value=\"" + StringUtils.defaultIfEmpty(card.getPower(), "¶") + "\"");
			text = text.replaceFirst("value=\"\"", "value=\"" + StringUtils.defaultIfEmpty(card.getToughness(), "¶") + "\"");
			text = text.replaceFirst("value=\"0\"", "value=\"" + card.getLoyalty() + "\"");
			text = text.replaceFirst("<option value=\"" + card.getRarity().name().toLowerCase() + "\">", "<option value=\"" + card.getRarity().name().toLowerCase() + "\" selected>");
			text = text.replaceFirst("<option value=\"" + card.getLayout().name().toLowerCase() + "\">", "<option value=\"" + card.getLayout().name().toLowerCase() + "\" selected>");
			if (card.isBasic())
				text = text.replace("<option value=\"basic\">", "<option value=\"basic\" selected>");
			else if (card.isLegendary())
				text = text.replace("<option value=\"legendary\">", "<option value=\"legendary\" selected>");
			text = text.replaceFirst("value=\"\"", "value=\"" + card.getArtist() + "\"");
			text = text.replaceFirst("value=\"\"", "value=\"" + StringUtils.defaultIfEmpty(card.getWatermark(), "¶") + "\"");
			text = text.replaceFirst("value=\"\"", "value=\"" + StringUtils.defaultIfEmpty(card.getNumber(), "¶") + "\"");
			text = text.replaceFirst("value=\"\"", "value=\"" + StringUtils.defaultIfEmpty(card.getImageName(), "¶") + "\"");
			text = text.replace("¶", "");
		}
		return text;
	}
}
