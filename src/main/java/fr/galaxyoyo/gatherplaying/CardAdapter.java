package fr.galaxyoyo.gatherplaying;

import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java8.util.stream.StreamSupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardAdapter extends TypeAdapter<Card>
{
	public static final Pattern MANA_COST = Pattern.compile("\\{.*?\\}");
	private static final ObservableMap<String, String> ABBREVIATES = FXCollections.observableHashMap();
	private static final Pattern COLORLESS_REPLACE = Pattern.compile("/Add \\{\\d+\\}/g");

	static
	{
		ABBREVIATES.put("German", "de");
		ABBREVIATES.put("French", "fr");
		ABBREVIATES.put("Italian", "it");
		ABBREVIATES.put("Spanish", "es");
		ABBREVIATES.put("Portuguese (Brazil)", "pt");
		ABBREVIATES.put("Russian", "ru");
		ABBREVIATES.put("Chinese Simplified", "cn");
		ABBREVIATES.put("Chinese Traditional", "tw");
		ABBREVIATES.put("Japanese", "jp");
		ABBREVIATES.put("Korean", "ko");
	}

	@Override
	public Card read(JsonReader r) throws IOException
	{
		Card card = new Card();
		r.beginObject();
		while (r.peek() != JsonToken.END_OBJECT)
		{
			String name = r.nextName();
			switch (name)
			{
				case "name":
					card.name.put("en", r.nextString().replace("Æ", "Ae"));
					break;
				case "manaCost":
					Matcher m = MANA_COST.matcher(r.nextString());
					card.manaCost = new ManaColor[0];
					int id = -1;
					while (m.find())
					{
						String str = m.group().substring(1, m.group().length() - 1).toUpperCase().replace("H", "Half");
						card.manaCost = Arrays.copyOf(card.manaCost, card.manaCost.length + 1);
						card.manaCost[++id] = ManaColor.getBySignificant(str);
						if (card.manaCost[id] == null)
							System.err.println(str);
					}
					break;
				case "cmc":
					card.cmc = r.nextDouble();
					break;
				case "colors":
					r.beginArray();
					card.colors = new ManaColor[0];
					while (r.peek() != JsonToken.END_ARRAY)
					{
						card.colors = Arrays.copyOf(card.colors, card.colors.length + 1);
						card.colors[card.colors.length - 1] = ManaColor.valueOf(r.nextString().toUpperCase());
					}
					r.endArray();
					break;
				case "colorIdentity":
					r.beginArray();
					card.colorIdentity = new ManaColor[0];
					while (r.peek() != JsonToken.END_ARRAY)
					{
						card.colorIdentity = Arrays.copyOf(card.colorIdentity, card.colorIdentity.length + 1);
						card.colorIdentity[card.colorIdentity.length - 1] = ManaColor.getBySignificant(r.nextString());
					}
					r.endArray();
					break;
				case "type":
					r.nextString();
					break;
				case "types":
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
					{
						String type = r.nextString().toUpperCase();
						if (type.equalsIgnoreCase("enchant"))
							type = "ENCHANTMENT";
						else if (type.equalsIgnoreCase("eaturecray"))
							type = "CREATURE";
						else if ("ScariestYou'llEverSee".toUpperCase().contains(type))
							continue;
						if (card.type == null)
							card.type = CardType.valueOf(type);
						else
							card.type = card.type.with(CardType.valueOf(type));
					}
					r.endArray();
					break;
				case "subtypes":
					r.beginArray();
					card.subtypes = new SubType[0];
					while (r.peek() != JsonToken.END_ARRAY)
					{
						SubType subtype = SubType.valueOf(r.nextString());
						if (subtype == null)
							continue;
						subtype.setCanApplicate(card.type);
						card.subtypes = Arrays.copyOf(card.subtypes, card.subtypes.length + 1);
						card.subtypes[card.subtypes.length - 1] = subtype;
					}
					r.endArray();
					break;
				case "supertypes":
					card.legendary = false;
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
					{
						String supertype = r.nextString();
						if (supertype.equalsIgnoreCase("legendary"))
							card.legendary = true;
						else if (supertype.equalsIgnoreCase("basic"))
							card.basic = true;
						else if (supertype.equalsIgnoreCase("world"))
							card.world = true;
						else if (supertype.equalsIgnoreCase("snow"))
							card.snow = true;
						else if (supertype.equalsIgnoreCase("ongoing"))
							card.ongoing = true;
						else
							Utils.alert("Alerte", "Supertype inconnu", supertype);
					}
					r.endArray();
					break;
				case "rarity":
					card.rarity = Rarity.valueOf(r.nextString().toUpperCase().replace("MYTHIC RARE", "MYTHIC").replace(" ", "_"));
					break;
				case "text":
					String ab = r.nextString();
					Matcher match = COLORLESS_REPLACE.matcher(ab);
					while (match.find())
					{
						int count = Integer.parseInt(match.group().substring(5, match.group().length() - 1));
						String str = "Add ";
						for (int i = 0; i < count; ++i)
							str += "{C}";
						ab = ab.replace(match.group(), str);
					}
					card.ability.put("en", ab);
					break;
				case "flavor":
					card.flavor.put("en", r.nextString());
					break;
				case "artist":
					card.artist = r.nextString();
					break;
				case "watermark":
					card.watermark = r.nextString();
					break;
				case "number":
					card.cardId = r.nextString();
					break;
				case "mciNumber":
					card.mciNumber = r.nextString();
					break;
				case "power":
					card.power = r.nextString();
					break;
				case "toughness":
					card.toughness = r.nextString();
					break;
				case "loyalty":
					card.loyalty = r.nextInt();
					break;
				case "layout":
					card.layout = Layout.valueOf(r.nextString().toUpperCase().replace("-", "_"));
					break;
				case "legalities":
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
					{
						r.beginObject();
						r.nextName();
						r.nextString();
						r.nextName();
						r.nextString();
						r.endObject();
					}
					r.endArray();
					break;
				case "multiverseid":
					card.muId.put("en", r.nextString());
					break;
				case "variations":
					r.beginArray();
					int min;
					if (card.cardId != null)
						min = Integer.parseInt(card.cardId.replaceAll("[^\\d]", ""));
					else
						min = Integer.MAX_VALUE;
					int max = min;
					if (min == Integer.MAX_VALUE)
						max = Integer.MIN_VALUE;
					while (r.peek() != JsonToken.END_ARRAY)
					{
						int variation = r.nextInt();
						min = Math.min(min, variation);
						max = Math.max(max, variation);
					}
					card.variations = new int[] {min, max};
					r.endArray();
					break;
				case "foreignNames":
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
					{
						r.beginObject();
						r.nextName();
						String locale = r.nextString();
						r.nextName();
						String tr_name = r.nextString().replace("Æ", "Ae");
						if (r.peek() != JsonToken.NAME)
						{
							r.endObject();
							continue;
						}
						r.nextName();
						String muId = r.nextString();
						String abreviate = ABBREVIATES.get(locale);
						if (!StreamSupport.stream(MySQL.getAllCards()).filter(c -> Objects.equals(abreviate, muId)).findAny().isPresent())
						{
							card.name.put(abreviate, tr_name);
							card.muId.put(abreviate, muId);
						}
						r.endObject();
					}
					r.endArray();
					break;
				case "imageName":
					card.imageName = r.nextString();
					break;
				case "printings":
				case "names":
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
						r.nextString();
					r.endArray();
					break;
				case "releaseDate":
					card.releaseDate = CardSerializer.DATE.deserialize(new JsonPrimitive(r.nextString()), Date.class, null);
					break;
				case "rulings":
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
					{
						r.beginObject();
						r.nextName();
						r.nextString();
						r.nextName();
						r.nextString();
						r.endObject();
					}
					r.endArray();
					break;
				case "hand":
					card.vanguardHand = r.nextInt();
					break;
				case "life":
					card.vanguardLife = r.nextInt();
					break;
				case "border":
					card.border = r.nextString();
					break;
				case "reserved":
					card.reserved = r.nextBoolean();
					break;
				case "starter":
				case "timeshifted":
					r.nextBoolean();
					break;
				case "originalText":
				case "originalType":
				case "id":
				case "source":
					r.nextString();
					break;
				default:
					System.out.println(name);
					break;
			}
		}
		r.endObject();
		if (card.colors == null)
			card.colors = new ManaColor[] {ManaColor.COLORLESS};
		if (card.colorIdentity == null)
			card.colorIdentity = new ManaColor[] {ManaColor.COLORLESS};
		return card;
	}

	@Override
	public void write(JsonWriter w, Card card) throws IOException { }
}