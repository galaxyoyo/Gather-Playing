package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

	private List<String> usedForeignMuIds = Lists.newArrayList();

	@Override
	public void write(JsonWriter w, Card card) throws IOException { }

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
					card.getName().put("en", r.nextString());
					break;
				case "manaCost":
					Matcher m = MANA_COST.matcher(r.nextString());
					card.setManaCost(new ManaColor[0]);
					int id = -1;
					while (m.find())
					{
						String str = m.group().substring(1, m.group().length() - 1).toUpperCase().replace("H", "Half");
						card.setManaCost(Arrays.copyOf(card.getManaCost(), card.getManaCost().length + 1));
						card.getManaCost()[++id] = ManaColor.getBySignificant(str);
						if (card.getManaCost()[id] == null)
							System.err.println(str);
					}
					break;
				case "cmc":
					card.setCmc(r.nextDouble());
					break;
				case "colors":
					r.beginArray();
					card.setColors(new ManaColor[0]);
					while (r.peek() != JsonToken.END_ARRAY)
					{
						card.setColors(Arrays.copyOf(card.getColors(), card.getColors().length + 1));
						card.getColors()[card.getColors().length - 1] = ManaColor.valueOf(r.nextString().toUpperCase());
					}
					r.endArray();
					break;
				case "colorIdentity":
					r.beginArray();
					card.setColorIdentity(new ManaColor[0]);
					while (r.peek() != JsonToken.END_ARRAY)
					{
						card.setColorIdentity(Arrays.copyOf(card.getColorIdentity(), card.getColorIdentity().length + 1));
						card.getColorIdentity()[card.getColorIdentity().length - 1] = ManaColor.getBySignificant(r.nextString());
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
						if (card.getType() == null)
							card.setType(CardType.valueOf(type));
						else
							card.setType(card.getType().with(CardType.valueOf(type)));
					}
					r.endArray();
					for (SubType subType : card.getSubtypes())
						subType.setCanApplicate(card.getType());
					break;
				case "subtypes":
					r.beginArray();
					card.setSubtypes(new SubType[0]);
					while (r.peek() != JsonToken.END_ARRAY)
					{
						SubType subtype = SubType.valueOf(r.nextString());
						if (subtype == null)
							continue;
						card.setSubtypes(Arrays.copyOf(card.getSubtypes(), card.getSubtypes().length + 1));
						card.getSubtypes()[card.getSubtypes().length - 1] = subtype;
					}
					r.endArray();
					break;
				case "supertypes":
					card.setLegendary(false);
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
					{
						String supertype = r.nextString();
						if (supertype.equalsIgnoreCase("legendary"))
							card.setLegendary(true);
						else if (supertype.equalsIgnoreCase("basic"))
							card.setBasic(true);
						else if (supertype.equalsIgnoreCase("world"))
							card.setWorld(true);
						else if (supertype.equalsIgnoreCase("snow"))
							card.setSnow(true);
						else if (supertype.equalsIgnoreCase("ongoing"))
							card.setOngoing(true);
						else
							Utils.alert("Alerte", "Supertype inconnu", supertype);
					}
					r.endArray();
					break;
				case "rarity":
					card.setRarity(Rarity.valueOf(r.nextString().toUpperCase().replace("MYTHIC RARE", "MYTHIC").replace(" ", "_")));
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
					card.getAbilityMap().put("en", ab);
					break;
				case "flavor":
					card.getFlavorMap().put("en", r.nextString());
					break;
				case "artist":
					card.setArtist(r.nextString());
					break;
				case "watermark":
					card.setWatermark(r.nextString());
					break;
				case "number":
					card.setCardId(r.nextString());
					break;
				case "mciNumber":
					card.setMciNumber(r.nextString());
					break;
				case "power":
					card.setPower(r.nextString());
					break;
				case "toughness":
					card.setToughness(r.nextString());
					break;
				case "loyalty":
					card.setLoyalty(r.nextInt());
					break;
				case "layout":
					card.setLayout(Layout.valueOf(r.nextString().toUpperCase().replace("-", "_")));
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
					card.getMuId().put("en", r.nextString());
					break;
				case "variations":
					r.beginArray();
					int min;
					if (card.getCardId() != null)
						min = Integer.parseInt(card.getCardId().replaceAll("[^\\d]", ""));
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
					card.setVariations(new int[]{min, max});
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
						String tr_name = r.nextString();
						if (r.peek() != JsonToken.NAME)
						{
							r.endObject();
							continue;
						}
						r.nextName();
						String muId = r.nextString();
						String abbreviate = ABBREVIATES.get(locale);
						if (card.getMuId(abbreviate) == null && !usedForeignMuIds.contains(muId))
						{
							card.getName().put(abbreviate, tr_name);
							card.getMuId().put(abbreviate, muId);
							usedForeignMuIds.add(muId);
						}
						r.endObject();
					}
					r.endArray();
					break;
				case "imageName":
					card.setImageName(r.nextString());
					break;
				case "printings":
				case "names":
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
						r.nextString();
					r.endArray();
					break;
				case "releaseDate":
					card.setReleaseDate(CardSerializer.DATE.deserialize(new JsonPrimitive(r.nextString()), Date.class, null));
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
					card.setVanguardHand(r.nextInt());
					break;
				case "life":
					card.setVanguardLife(r.nextInt());
					break;
				case "border":
					card.setBorder(r.nextString());
					break;
				case "reserved":
					card.setReserved(r.nextBoolean());
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
		if (card.getColors() == null)
			card.setColors(new ManaColor[]{ManaColor.COLORLESS});
		if (card.getColorIdentity() == null)
			card.setColorIdentity(new ManaColor[]{ManaColor.COLORLESS});
		return card;
	}
}