package fr.galaxyoyo.gatherplaying;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.galaxyoyo.gatherplaying.server.Server;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class CardSerializer implements JsonDeserializer<Card>
{
	public static final DateSerializer DATE = new DateSerializer();
	public static final SubTypeSerializer SUBTYPE = new SubTypeSerializer();
	public static final ManaColorSerializer MANACOLOR = new ManaColorSerializer();
	public static final LayoutSerializer LAYOUT = new LayoutSerializer();
	public static final OwnedCardSerializer OWNEDCARD = new OwnedCardSerializer();
	public static final CardSerializer CARD = new CardSerializer();

	private CardSerializer() { }

	@SuppressWarnings("WeakerAccess")
	public static class DateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date>
	{
		private DateSerializer() { }

		@Override
		public Date deserialize(JsonElement elem, Type type, JsonDeserializationContext cxt) throws JsonParseException
		{
			String str = elem.getAsString();
			String[] split = str.split("-");
			Calendar cal = new GregorianCalendar();
			cal.setTimeZone(TimeZone.getTimeZone("UTC"));
			cal.setTimeInMillis(0L);
			cal.set(Calendar.YEAR, Integer.parseInt(split[0]));
			if (split.length >= 2)
				cal.set(Calendar.MONTH, Integer.parseInt(split[1]) - 1);
			if (split.length == 3)
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(split[2]));
			return cal.getTime();
		}

		@Override
		public JsonElement serialize(Date date, Type type, JsonSerializationContext cxt)
		{
			Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
			return new JsonPrimitive(cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH));
		}
	}

	private static class ManaColorSerializer implements JsonDeserializer<ManaColor>, JsonSerializer<ManaColor>
	{
		private ManaColorSerializer() { }

		@Override
		public ManaColor deserialize(JsonElement elem, Type type, JsonDeserializationContext cxt) throws JsonParseException
		{
			return ManaColor.getBySignificant(elem.getAsString());
		}

		@Override
		public JsonElement serialize(ManaColor mc, Type type, JsonSerializationContext cxt)
		{
			return new JsonPrimitive(mc.getAbbreviate());
		}
	}

	private static class LayoutSerializer implements JsonDeserializer<Layout>, JsonSerializer<Layout>
	{
		private LayoutSerializer() { }

		@Override
		public Layout deserialize(JsonElement elem, Type type, JsonDeserializationContext cxt) throws JsonParseException
		{
			return Layout.valueOf(elem.getAsString().replace(' ', '_').replace('-', '_').toUpperCase());
		}

		@Override
		public JsonElement serialize(Layout layout, Type type, JsonSerializationContext cxt)
		{
			return new JsonPrimitive(layout.name().toLowerCase());
		}
	}

	private static class OwnedCardSerializer extends TypeAdapter<OwnedCard>
	{
		private OwnedCardSerializer() { }

		@Override
		public void write(JsonWriter w, OwnedCard card) throws IOException
		{
			w.beginObject();
			w.name("muId");
			w.value(card.getCard().getMuId().get("en"));
			w.name("foiled");
			w.value(card.isFoiled());
			w.name("owner");
			w.value(card.getOwner().uuid.toString());
			w.endObject();
		}

		@Override
		public OwnedCard read(JsonReader r) throws IOException
		{
			r.beginObject();
			r.nextName();
			Card c = MySQL.getCard(r.nextInt());
			r.nextName();
			boolean foiled = r.nextBoolean();
			r.nextName();
			Player player = Server.getPlayer(UUID.fromString(r.nextString()));
			r.endObject();
			return new OwnedCard(c, player, foiled);
		}
	}

	private static class SubTypeSerializer implements JsonDeserializer<SubType>, JsonSerializer<SubType>
	{
		private SubTypeSerializer() { }

		@Override
		public SubType deserialize(JsonElement elem, Type type, JsonDeserializationContext cxt) throws JsonParseException { return SubType.valueOf(elem.getAsString()); }

		@Override
		public JsonElement serialize(SubType subtype, Type type, JsonSerializationContext cxt) { return new JsonPrimitive(subtype.name); }
	}

	@Override
	public Card deserialize(JsonElement elem, Type type, JsonDeserializationContext ctx) throws JsonParseException { return MySQL.getCard(elem.getAsInt()); }
}