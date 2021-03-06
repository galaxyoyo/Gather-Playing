package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import fr.galaxyoyo.gatherplaying.client.Config;
import fr.galaxyoyo.gatherplaying.client.gui.Loading;
import javafx.collections.FXCollections;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySQL
{
	private static final Map<Integer, Card> cards = Maps.newHashMap();
	private static final Map<String, Set> sets = Maps.newHashMap();
	private static final String BOOLEAN = "BOOLEAN";
	private static final String INTEGER = "INT";
	private static final String DOUBLE = "DOUBLE";
	private static final String VARCHAR = "VARCHAR";
	private static final String UUID = "UUID";
	private static final Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, CardSerializer.DATE).registerTypeAdapter(SubType.class, CardSerializer.SUBTYPE)
			.registerTypeAdapter(ManaColor.class, CardSerializer.MANACOLOR).create();
	private static final String[] CARD_COLUMNS =
			new String[]{"id_EN", "id_DE", "id_FR", "id_IT", "id_ES", "id_PT", "id_RU", "id_CN", "id_TW", "id_JP", "id_KO", "name_EN", "name_DE", "name_FR", "name_IT", "name_ES",
					"name_PT", "name_RU", "name_CN", "name_TW", "name_JP", "name_KO", "number", "mci_number", "set", "type", "subtypes", "legendary", "basic", "world", "snow",
					"ongoing", "power", "toughness", "loyalty", "mana_cost", "converted_manacost", "colors", "color_identity", "variations", "ability_EN", "ability_DE", "ability_FR",
					"ability_IT", "ability_ES", "ability_PT", "ability_RU", "ability_CN", "ability_TW", "ability_JP", "ability_KO", "flavor_EN", "flavor_DE", "flavor_FR", "flavor_IT",
					"flavor_ES", "flavor_PT", "flavor_RU", "flavor_CN", "flavor_TW", "flavor_JP", "flavor_KO", "rarity", /*"rulings", */"layout", "artist", "image_name", "watermark"};
	private static final String[] LOCALES = {"en", "de", "fr", "it", "es", "pt", "ru", "cn", "tw", "ko"};
	private static Connection connection = null;
	private static Map<String, String> config = Maps.newHashMap();
	private static Map<String, CardType> cardTypes = new HashMap<String, CardType>()
	{
		@Override
		public CardType get(Object key)
		{
			if (!containsKey(key))
			{
				CardType type = CardType.valueOf(key.toString());
				put(key.toString(), type);
				return type;
			}
			return super.get(key);
		}
	};

	public static boolean getBooleanConfig(String key, boolean defaultValue)
	{
		return Boolean.parseBoolean(getConfig(key, Boolean.toString(defaultValue)));
	}

	public static String getConfig(String key, String def)
	{
		if (config.containsKey(key))
			return config.get(key);
		ResultSet set = query("config", Condition.like(new Value("key", key.toUpperCase())));
		assert set != null;
		try
		{
			if (!set.next())
			{
				setConfig(key, def);
				return def;
			}
			String value = set.getString("value");
			assert value != null;
			config.put(key, value);
			return value;
		}
		catch (SQLException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private static ResultSet query(String table, Condition c)
	{
		try
		{
			return connection.createStatement().executeQuery("SELECT * FROM `" + table + "` WHERE " + c.toString() + ";");
		}
		catch (SQLException ex)
		{
			if (!ex.getMessage().contains("no such table"))
				throw new RuntimeException(ex);
			else
				return null;
		}
	}

	public static boolean setConfig(String key, String value)
	{
		key = key.toUpperCase();
		if (delete("config", Condition.equals(new Value("key", key))) < 0 || insert("config", new String[]{"key", "value"}, key, value) < 0)
			return false;
		config.put(key, value);
		return true;
	}

	private static int insert(String table, String[] columns, Object... values)
	{
		try
		{
			String sql = "INSERT INTO `" + table + "` (";
			for (int i = 0; i < columns.length; ++i)
				sql += "`" + columns[i] + "`" + (i + 1 != columns.length ? ", " : "");
			sql += ") VALUES (";
			for (int i = 0; i < values.length; ++i)
				sql += "?" + (i + 1 != values.length ? ", " : "");
			sql += ");";
			PreparedStatement state = connection.prepareStatement(sql);
			for (int i = 0; i < values.length; ++i)
				state.setObject(i + 1, values[i]);
			return state.executeUpdate();
		}
		catch (SQLException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static int delete(String table, Condition c)
	{
		try
		{
			String sql = "DELETE FROM `" + table + "` WHERE " + c.toString() + ";";
			return connection.createStatement().executeUpdate(sql);
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			return -1;
		}
	}

	private static ResultSet query(String table) { return query(table, Condition.ALWAYS_TRUE); }

	private static boolean checkexists(String table)
	{
		try
		{
			ResultSet set = connection.createStatement().executeQuery("SELECT count(*) FROM `" + table + "`;");
			if (set == null)
				return false;
			set.close();
			return true;
		}
		catch (SQLException ex)
		{
			if (!ex.getMessage().contains("no such table"))
				throw new RuntimeException(ex);
			else
				return false;
		}
	}

	public static Card getCard(ResultSet set)
	{
		Card card = new Card();
		try
		{
			for (String locale : LOCALES)
			{
				card.getName().put(locale, set.getString("name_" + locale.toUpperCase()));
				String id = set.getString("id_" + locale.toUpperCase());
		/*		if (id != null && id.equals("20574b"))
					id = "20574";
				else if (id != null && id.equals("20576b"))
					id = "20576";
				else if (id != null && id.equals("20578b"))
					id = "20578";
				else if (id != null && id.equals("20580b"))
					id = "20580";
				else if (id != null && id.equals("20582b"))
					id = "20582";
				else if (id != null && id.equals("26691b"))
					id = "26691";
				else if (id != null && id.equals("27161b"))
					id = "27161";
				else if (id != null && id.equals("27162b"))
					id = "27162";
				else if (id != null && id.equals("27164b"))
					id = "27164";
				else if (id != null && id.equals("27166b"))
					id = "27166";
				else if (id != null && id.equals("27168b"))
					id = "27168";
				else if (id != null && id.equals("78600b"))
					id = "78600";
				else if (id != null && id.equals("78695b"))
					id = "78695";
				else if (id != null && id.equals("78686b"))
					id = "78686";
				else */
				if (id != null && (id.contains("a") || id.contains("b") || id.contains("c") || id.contains("d") || id.contains("e")))
					id = Integer.toString(Integer.parseInt(id.replaceAll("[^\\d]", "")));
				if (id != null)
					card.getMuId().put(locale, Integer.parseInt(id));
				card.getAbilityMap().put(locale, set.getString("ability_" + locale.toUpperCase()));
				card.getFlavorMap().put(locale, set.getString("flavor_" + locale.toUpperCase()));
			}
			String ed = set.getString("set");
			card.setNumber(set.getString("number"));
			card.setMciNumber(set.getString("mci_number"));
			card.setSet(sets.get(ed));
			if (sets.get(ed) == null)
				System.err.println(ed);
			if (card.getSet().isPreview())
				card.setPreview();
			card.getSet().getCards().add(card);
			card.setType(cardTypes.get(set.getString("type").toUpperCase()));
			card.setSubtypes(gson.fromJson(set.getString("subtypes"), SubType[].class));
			for (SubType st : card.getSubtypes())
			{
				if (st != null)
					st.setCanApplicate(card.getType());
			}
			card.setLegendary(set.getBoolean("legendary"));
			card.setBasic(set.getBoolean("basic"));
			card.setWorld(set.getBoolean("world"));
			card.setSnow(set.getBoolean("snow"));
			card.setOngoing(set.getBoolean("ongoing"));
			card.setPower(set.getString("power"));
			card.setToughness(set.getString("toughness"));
			card.setLoyalty(set.getInt("loyalty"));
			card.setManaCost(gson.fromJson(set.getString("mana_cost").toUpperCase(), ManaColor[].class));
			card.setCmc(set.getDouble("converted_manacost"));
			card.setColors(gson.fromJson(set.getString("colors").toUpperCase(), ManaColor[].class));
			card.setColorIdentity(gson.fromJson(set.getString("color_identity").toUpperCase(), ManaColor[].class));
			card.setVariations(gson.fromJson(set.getString("variations"), int[].class));
			card.setRarity(Rarity.valueOf(set.getString("rarity").toUpperCase()));
			card.setLayout(Layout.valueOf(set.getString("layout").toUpperCase()));
			card.setArtist(set.getString("artist"));
			card.setImageName(set.getString("image_name"));
			card.setWatermark(set.getString("watermark"));
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		return card;
	}

	public static void updateCard(Card card)
	{
		String l = card.getNumber() == null ? "" : card.getNumber().replaceAll("[0-9F-Zf-z]", "");
		if (l.equals("a") || card.getLayout() == Layout.DOUBLE_FACED || card.getRarity() == Rarity.BASIC_LAND || !cards.containsKey(card.getMuId("en")))
			l = "";
		delete("cards", Condition.equals(new Value("id_EN", card.getMuId("en") + l)));
		insertCard(card);
	}

	private static void insertCard(Card card)
	{
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, CardSerializer.DATE).registerTypeAdapter(SubType.class, CardSerializer.SUBTYPE)
				.registerTypeAdapter(ManaColor.class, CardSerializer.MANACOLOR).create();

		try
		{
			String l = card.getNumber() == null ? "" : card.getNumber().replaceAll("[0-9F-Zf-z]", "");
			if (l.equals("a") || card.getLayout() == Layout.DOUBLE_FACED || card.getRarity() == Rarity.BASIC_LAND || !cards.containsKey(card.getMuId("en")))
				l = "";
			if (!l.isEmpty())
				System.out.println(card.getMuId("en") + l);
			insert("cards", CARD_COLUMNS, card.getMuId("en") + l, card.getMuId("de"), card.getMuId("fr"), card.getMuId("it"),
					card.getMuId("es"), card.getMuId("pt"), card.getMuId("ru"), card.getMuId("cn"), card.getMuId("tw"),
					card.getMuId("jp"), card.getMuId("ko"), card.getName().get("en"), card.getName().get("de"), card.getName().get("fr"),
					card.getName().get("it"), card.getName().get("es"), card.getName().get("pt"), card.getName().get("ru"), card.getName().get("cn"), card.getName().get("tw"),
					card.getName().get("jp"), card.getName().get("ko"), card.getNumber(), card.getMciNumber(), card.getSet().getCode(), card.getType().name().toLowerCase(),
					gson.toJson(card.getSubtypes()), card.isLegendary(), card.isBasic(), card.isWorld(), card.isSnow(), card.isOngoing(), card.getPower(), card.getToughness(),
					card.getLoyalty(), gson.toJson(card.getManaCost()).toLowerCase(), card.getCmc(), gson.toJson(card.getColors()).toLowerCase(),
					gson.toJson(card.getColorIdentity()).toLowerCase(), gson.toJson(card.getVariations()), card.getAbilityMap().get("en"), card.getAbilityMap().get("de"),
					card.getAbilityMap().get("fr"), card.getAbilityMap().get("it"), card.getAbilityMap().get("es"), card.getAbilityMap().get("pt"), card.getAbilityMap().get("ru"),
					card.getAbilityMap().get("cn"), card.getAbilityMap().get("tw"), card.getAbilityMap().get("jp"), card.getAbilityMap().get("ko"), card.getFlavorMap().get("en"),
					card.getFlavorMap().get("de"), card.getFlavorMap().get("fr"), card.getFlavorMap().get("it"), card.getFlavorMap().get("es"), card.getFlavorMap().get("pt"),
					card.getFlavorMap().get("ru"), card.getFlavorMap().get("cn"), card.getFlavorMap().get("tw"), card.getFlavorMap().get("jp"), card.getFlavorMap().get("ko"),
					card.getRarity().name().toLowerCase(), card.getLayout().name().toLowerCase(), card.getArtist(), card.getImageName(), card.getWatermark());
		}
		catch (RuntimeException ex)
		{
			throw new RuntimeException("Error while updating: " + card, ex.getCause());
		}
	}

	public static Player getPlayer(String email)
	{
		try
		{
			PreparedStatement s = connection.prepareStatement("SELECT * FROM PLAYERS WHERE `email` = ? OR `name` = ?");
			s.setString(1, email);
			s.setString(2, email);
			ResultSet set = s.executeQuery();
			if (!set.next())
				return null;
			Player player = new Player();
			player.uuid = java.util.UUID.fromString(set.getString("uuid"));
			player.name = set.getString("name");
			player.email = set.getString("email");
			player.sha1Pwd = set.getString("password_hash");
			player.money = set.getInt("money");
			player.cards.addAll(new GsonBuilder().registerTypeAdapter(OwnedCard.class, CardSerializer.OWNEDCARD).create()
					.fromJson(set.getString("cards"), new TypeToken<HashSet<OwnedCard>>() {}.getType()));
			player.lastIp = set.getString("last_ip");
			return player;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static void savePlayer(Player player)
	{
		try
		{
			PreparedStatement s = connection.prepareStatement("SELECT * FROM PLAYERS WHERE `uuid` = ?");
			s.setString(1, player.uuid.toString());
			ResultSet set = s.executeQuery();
			if (set.next())
				update("players", Condition.equals(new Value("uuid", player.uuid.toString())), new Value("email", player.email), new Value("name", player.name),
						new Value("password_hash", player.sha1Pwd), new Value("last_ip", player.lastIp),
						new Value("cards", new GsonBuilder().registerTypeAdapter(OwnedCard.class, CardSerializer.OWNEDCARD).create().toJson(player.cards)));
			else
				insert("players", new String[]{"uuid", "name", "email", "password_hash", "last_ip"}, player.uuid, player.name, player.email, player.sha1Pwd, player.lastIp);
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}

	public static int update(String table, Condition where, Value... values)
	{
		try
		{
			String sql = "UPDATE `" + table + "` SET ";
			for (int i = 0; i < values.length; ++i)
				sql += "`" + values[i].column + "` = ?" + (i + 1 != values.length ? ", " : "");
			sql += " WHERE " + where.toString() + ";";
			PreparedStatement state = connection.prepareStatement(sql);
			for (int i = 0; i < values.length; ++i)
				state.setObject(i + 1, values[i].value);
			return state.executeUpdate();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			return -1;
		}
	}

	@SuppressWarnings("unchecked")
	public static Deck getDeck(UUID uuid)
	{
		Gson gson = new GsonBuilder().registerTypeAdapter(OwnedCard.class, CardSerializer.OWNEDCARD).create();
		try
		{
			ResultSet set = query("decks", Condition.equals(new Value("uuid", uuid)));
			assert set != null;
			set.next();
			Deck deck = new Deck();
			deck.setUuid(java.util.UUID.fromString(set.getString("uuid")));
			deck.setFree(set.getBoolean("free"));
			deck.setName(set.getString("name"));
			deck.setDesc(set.getString("desc"));
			deck.setCards(FXCollections.observableSet((java.util.Set<OwnedCard>) gson.fromJson(set.getString("cards"), new TypeToken<HashSet<OwnedCard>>() {}.getType())));
			deck.setSideboard(FXCollections.observableSet((java.util.Set<OwnedCard>) gson.fromJson(set.getString("sideboard"), new TypeToken<HashSet<OwnedCard>>() {}.getType
					())));
			deck.setColors(gson.fromJson(set.getString("colors"), ManaColor[].class));
			deck.setLegalities(gson.fromJson(set.getString("legalities"), new TypeToken<HashSet<Rules>>() {}.getType()));
			return deck;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void readDecks(Player player)
	{
		Gson gson = new GsonBuilder().registerTypeAdapter(OwnedCard.class, CardSerializer.OWNEDCARD).create();
		try
		{
			ResultSet set = query("decks", Condition.equals(new Value("owner", player.uuid)));
			player.decks.clear();
			while (set != null && set.next())
			{
				Deck deck = new Deck();
				deck.setUuid(java.util.UUID.fromString(set.getString("uuid")));
				deck.setFree(set.getBoolean("free"));
				deck.setName(set.getString("name"));
				deck.setDesc(set.getString("desc"));
				deck.setOwner(player);
				deck.setCards(FXCollections.observableSet((java.util.Set<OwnedCard>) gson.fromJson(set.getString("cards"), new TypeToken<HashSet<OwnedCard>>() {}.getType())));
				deck.setSideboard(FXCollections.observableSet((java.util.Set<OwnedCard>) gson.fromJson(set.getString("sideboard"), new TypeToken<HashSet<OwnedCard>>() {}.getType
						())));
				deck.setColors(gson.fromJson(set.getString("colors"), ManaColor[].class));
				deck.setLegalities(gson.fromJson(set.getString("legalities"), new TypeToken<HashSet<Rules>>() {}.getType()));
				player.decks.add(deck);
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}

	public static void saveDeck(Deck deck)
	{
		Gson gson = new GsonBuilder().registerTypeAdapter(OwnedCard.class, CardSerializer.OWNEDCARD).create();
		delete("decks", Condition.equals(new Value("uuid", deck.getUuid())));
		insert("decks", deck.getUuid(), deck.isFree(), deck.getOwner().uuid, gson.toJson(deck.getCards()), gson.toJson(deck.getSideboard()), deck.getName(), deck.getDesc(),
				gson.toJson(deck.getColors()), gson.toJson(deck.getLegalities()));
	}

	private static int insert(String table, Object... values)
	{
		try
		{
			String sql = "INSERT INTO `" + table + "` VALUES(";
			for (Object ignored : values)
				sql += "?, ";
			sql = sql.substring(0, sql.length() - 2) + ");";
			PreparedStatement state = connection.prepareStatement(sql);
			for (int i = 0; i < values.length; ++i)
				state.setObject(i + 1, values[i]);
			return state.executeUpdate();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			return -1;
		}
	}

	private static int createTable(String name, Column... columns)
	{
		try
		{
			String sql = "CREATE TABLE IF NOT EXISTS `" + name + "` (";
			for (Column col : columns)
			{
				sql += "`" + col.getName() + "` " + col.getType();
				if (!col.isNullable())
					sql += " NOT NULL";
				if (col.getDefaultExpression() != null)
					sql += " DEFAULT " + col.getDefaultExpression();
				if (col.isAutoIncrement())
					sql += " AUTO_INCREMENT";
				if (col.isPrimaryKey())
					sql += " PRIMARY KEY";
				sql += ", ";
			}
			sql = sql.substring(0, sql.length() - 2) + ");";
			return connection.prepareStatement(sql).executeUpdate();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			return -1;
		}
	}

	public static void init()
	{
		try
		{
			if (Utils.isMobile())
				DriverManager.registerDriver((Driver) Class.forName("org.sqldroid.SQLDroidDriver").newInstance());
			connection = DriverManager.getConnection("jdbc:sqlite:" + Utils.newFile("cards.db").getAbsolutePath());
			connection.createStatement().execute("PRAGMA encoding = 'UTF-8';");
			Loading.setLabel("Vérification de la base de données");
			if (!checkexists("sets"))
			{
				Loading.setLabel("Création de la table des extensions ...");
				Column name = new Column("name", VARCHAR);
				name.setPrimaryKey(true);
				Column name_DE = new Column("name_DE", VARCHAR);
				Column name_FR = new Column("name_FR", VARCHAR);
				Column name_IT = new Column("name_IT", VARCHAR);
				Column name_ES = new Column("name_ES", VARCHAR);
				Column name_PT = new Column("name_PT", VARCHAR);
				Column name_JP = new Column("name_JP", VARCHAR);
				Column name_CN = new Column("name_CN", VARCHAR);
				Column name_TW = new Column("name_TW", VARCHAR);
				Column name_KO = new Column("name_KO", VARCHAR);
				Column code = new Column("code", VARCHAR);
				code.setNullable(false);
				Column magicCardsInfo = new Column("magic_cards_info_code", VARCHAR);
				magicCardsInfo.setNullable(false);
				Column date = new Column("release_date", VARCHAR);
				date.setNullable(false);
				Column type = new Column("type", VARCHAR);
				type.setNullable(false);
				Column block = new Column("block", VARCHAR);
				Column booster = new Column("booster", VARCHAR);
				Column border = new Column("border", VARCHAR);
				Column finishedTranslations = new Column("finished_translations", VARCHAR);
				finishedTranslations.setDefaultExpression("");
				Column mkmId = new Column("mkm_id", INTEGER);
				Column mkmName = new Column("mkm_name", VARCHAR);
				createTable("sets", name, name_DE, name_FR, name_IT, name_ES, name_PT, name_JP, name_CN, name_TW, name_KO, code, magicCardsInfo, date, type, block, booster, border,
						finishedTranslations, mkmId, mkmName);
			}
			if (!checkexists("cards"))
			{
				Loading.setLabel("Création de la table des cartes ...");
				Column muId_EN = new Column("id_EN", VARCHAR);
				muId_EN.setPrimaryKey(true);
				Column muId_CN = new Column("id_CN", VARCHAR);
				Column muId_TW = new Column("id_TW", VARCHAR);
				Column muId_FR = new Column("id_FR", VARCHAR);
				Column muId_DE = new Column("id_DE", VARCHAR);
				Column muId_IT = new Column("id_IT", VARCHAR);
				Column muId_JP = new Column("id_JP", VARCHAR);
				Column muId_PT = new Column("id_PT", VARCHAR);
				Column muId_RU = new Column("id_RU", VARCHAR);
				Column muId_ES = new Column("id_ES", VARCHAR);
				Column muId_KO = new Column("id_KO", VARCHAR);
				Column name_EN = new Column("name_EN", VARCHAR);
				Column name_CN = new Column("name_CN", VARCHAR);
				Column name_TW = new Column("name_TW", VARCHAR);
				Column name_FR = new Column("name_FR", VARCHAR);
				Column name_DE = new Column("name_DE", VARCHAR);
				Column name_IT = new Column("name_IT", VARCHAR);
				Column name_JP = new Column("name_JP", VARCHAR);
				Column name_PT = new Column("name_PT", VARCHAR);
				Column name_RU = new Column("name_RU", VARCHAR);
				Column name_ES = new Column("name_ES", VARCHAR);
				Column name_KO = new Column("name_KO", VARCHAR);
				Column number = new Column("number", VARCHAR);
				Column mciNumber = new Column("mci_number", VARCHAR);
				Column set = new Column("set", VARCHAR);
				set.setNullable(false);
				Column type = new Column("type", VARCHAR);
				type.setNullable(false);
				Column subtypes = new Column("subtypes", VARCHAR);
				Column legendary = new Column("legendary", BOOLEAN);
				Column basic = new Column("basic", BOOLEAN);
				Column world = new Column("world", BOOLEAN);
				Column snow = new Column("snow", BOOLEAN);
				Column ongoing = new Column("ongoing", BOOLEAN);
				Column power = new Column("power", VARCHAR);
				Column toughness = new Column("toughness", VARCHAR);
				Column loyalty = new Column("loyalty", INTEGER);
				Column manaCost = new Column("mana_cost", VARCHAR);
				Column cmc = new Column("converted_manacost", DOUBLE);
				Column colors = new Column("colors", VARCHAR);
				colors.setNullable(false);
				Column colorIdentity = new Column("color_identity", VARCHAR);
				Column variations = new Column("variations", VARCHAR);
				Column ability_EN = new Column("ability_EN", VARCHAR);
				Column ability_CN = new Column("ability_CN", VARCHAR);
				Column ability_TW = new Column("ability_TW", VARCHAR);
				Column ability_FR = new Column("ability_FR", VARCHAR);
				Column ability_DE = new Column("ability_DE", VARCHAR);
				Column ability_IT = new Column("ability_IT", VARCHAR);
				Column ability_JP = new Column("ability_JP", VARCHAR);
				Column ability_PT = new Column("ability_PT", VARCHAR);
				Column ability_RU = new Column("ability_RU", VARCHAR);
				Column ability_ES = new Column("ability_ES", VARCHAR);
				Column ability_KO = new Column("ability_KO", VARCHAR);
				Column flavor_EN = new Column("flavor_EN", VARCHAR);
				Column flavor_CN = new Column("flavor_CN", VARCHAR);
				Column flavor_TW = new Column("flavor_TW", VARCHAR);
				Column flavor_FR = new Column("flavor_FR", VARCHAR);
				Column flavor_DE = new Column("flavor_DE", VARCHAR);
				Column flavor_IT = new Column("flavor_IT", VARCHAR);
				Column flavor_JP = new Column("flavor_JP", VARCHAR);
				Column flavor_PT = new Column("flavor_PT", VARCHAR);
				Column flavor_RU = new Column("flavor_RU", VARCHAR);
				Column flavor_ES = new Column("flavor_ES", VARCHAR);
				Column flavor_KO = new Column("flavor_KO", VARCHAR);
				Column rarity = new Column("rarity", VARCHAR);
				rarity.setNullable(false);
				//	Column rulings = new Column("rulings", VARCHAR);
				Column layout = new Column("layout", VARCHAR);
				Column artist = new Column("artist", VARCHAR);
				Column imageName = new Column("image_name", VARCHAR);
				Column watermark = new Column("watermark", VARCHAR);
				List<Column> columns =
						Lists.newArrayList(muId_EN, muId_DE, muId_FR, muId_IT, muId_ES, muId_PT, muId_RU, muId_CN, muId_TW, muId_JP, muId_KO, name_EN, name_DE, name_FR, name_IT,
								name_ES, name_PT, name_RU, name_CN, name_TW, name_JP, name_KO, number, mciNumber, set, type, subtypes, legendary, basic, world, snow, ongoing, power,
								toughness, loyalty, manaCost, cmc, colors, colorIdentity, variations, ability_EN, ability_DE, ability_FR, ability_IT, ability_ES, ability_PT,
								ability_RU, ability_CN, ability_TW, ability_JP, ability_KO, flavor_EN, flavor_DE, flavor_FR, flavor_IT, flavor_ES, flavor_PT, flavor_RU, flavor_CN,
								flavor_TW, flavor_JP, flavor_KO, rarity, layout, artist, imageName, watermark);
				createTable("cards", columns.toArray(new Column[columns.size()]));
			}
			Loading.setLabel("Détection des paramètres");
			String actualDBVersion = "1.0";
			try
			{
				actualDBVersion = IOUtils.toString(new URL("http://" + (Utils.DEBUG ? "localhost/gatherplaying" : "gp.arathia.fr") + "/DB_VERSION.php"));
			}
			catch (IOException ex)
			{
				System.err.println("Error while checking DB version.");
			}
			if (!checkexists("config"))
			{
				Loading.setLabel("Création de la table des paramètres ...");
				Column key = new Column("key", VARCHAR);
				key.setPrimaryKey(true);
				key.setNullable(false);
				Column value = new Column("value", VARCHAR);
				createTable("config", key, value);
			}
			if (!getConfig("dbversion", "1.0").equals(actualDBVersion))
			{
				connection.createStatement().executeUpdate(IOUtils.toString(
						new URL("http://" + (Utils.DEBUG ? "localhost/gatherplaying" : "gp.arathia.fr") + "/UpdateDB.php?old=" + getConfig("dbversion", "1.0"))));
				setConfig("dbversion", actualDBVersion);
			}
			Config.init();

			Loading.setLabel("Recherche des extensions locales ...");
			ResultSet setsSet = query("sets");
			while (setsSet.next())
			{
				Set set = new Set();
				set.setName(setsSet.getString("name"));
				set.translations.put("de", setsSet.getString("name_DE"));
				set.translations.put("fr", setsSet.getString("name_FR"));
				set.translations.put("it", setsSet.getString("name_IT"));
				set.translations.put("es", setsSet.getString("name_ES"));
				set.translations.put("pt", setsSet.getString("name_PT"));
				set.translations.put("jp", setsSet.getString("name_JP"));
				set.translations.put("cn", setsSet.getString("name_CN"));
				set.translations.put("tw", setsSet.getString("name_TW"));
				set.translations.put("ko", setsSet.getString("name_KO"));
				set.setCode(setsSet.getString("code"));
				set.setMagicCardsInfoCode(setsSet.getString("magic_cards_info_code"));
				set.setReleaseDate(CardSerializer.DATE.deserialize(new JsonPrimitive(setsSet.getString("release_date")), Date.class, null));
				set.setType(setsSet.getString("type"));
				set.setBlock(setsSet.getString("block"));
				set.booster = new Gson().fromJson(setsSet.getString("booster"), Object[].class);
				set.setBorder(setsSet.getString("border"));
				set.setFinishedTranslations(setsSet.getString("finished_translations"));
				set.setMKMId(setsSet.getInt("mkm_id"));
				set.setMKMName(setsSet.getString("mkm_name"));
				if (set.getReleaseDate().getTime() - System.currentTimeMillis() > 864000000L)
					set.setPreview();
				sets.put(set.getCode(), set);
				Loading.setLabel(setsSet.getRow() + " extensions chargées");
			}
			Loading.setLabel("Recherche des cartes locales ...");
			long timestamp = System.currentTimeMillis();
			ResultSet cardsSet = query("cards");
			while (cardsSet.next())
			{
				Card card = getCard(cardsSet);
				if (cards.containsKey(card.getMuId("en")))
				{
					cards.get(card.getMuId("en")).setRelated(card);
					card.setRelated(cards.get(card.getMuId("en")));
				}
				else
					cards.put(card.getMuId().get("en"), card);
				if (Utils.isDesktop() || cardsSet.getRow() % 100 == 0)
					Loading.setLabel(cardsSet.getRow() + " cartes chargées");
			}
			System.err.println("Time to read " + cards.size() + " cards : " + (System.currentTimeMillis() - timestamp) + " ms");
			Loading.setLabel("Recherche de mise à jour de la base de données ...");
			String[] setCodes;
			try
			{
				setCodes = gson.fromJson(IOUtils.toString(new URL("http://galaxyoyo.com/gp/json/SetCodes.json")), String[].class);
			/*	HttpURLConnection co = (HttpURLConnection) new URL("https://mtgjson.com/json/SetCodes.json").openConnection();
				co.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0");
				setCodes = gson.fromJson(IOUtils.toString(co.getInputStream()), String[].class);*/
			}
			catch (IOException ex)
			{
				if (!(ex instanceof ConnectException))
					ex.printStackTrace();
				setCodes = new String[sets.size()];
				int i = 0;
				for (Iterator<Set> $i = sets.values().iterator(); i < sets.size() && $i.hasNext(); ++i)
					setCodes[i] = $i.next().getCode();
			}

			connection.setAutoCommit(false);
			for (String code : setCodes)
			{
				if (code.equals("ATH") || code.equals("DKM") || code.equals("BRB") || code.equals("BTD") || code.equals("CST") || code.equals("DPA") || code.equals("MD1") ||
						code.equals("MGB") || code.equals("RQS") || code.equals("CED") || code.equals("CEI") || code.equals("MGB") || code.equals("ITP") || code.equals("PLS") ||
						code.equals("CPK") || code.equals("MED") || code.equals("ME2") || code.equals("ME3") || code.equals("ME4") || code.equals("TPR") || code.equals("VMA") ||
						code.equals("S99") || code.equals("S00") || code.startsWith("p"))
					continue;
				System.out.println(code);
				if (sets.containsKey(code))
					continue;
				Loading.setLabel("Installation de l'édition " + code + " ...");
				timestamp = System.currentTimeMillis();
				String jsoned = IOUtils.toString(new URL("http://galaxyoyo.com/gp/json/" + code + ".json"), StandardCharsets.UTF_8);
			/*	HttpURLConnection co = (HttpURLConnection) new URL("https://mtgjson.com/json/" + code + "-x.json").openConnection();
				co.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0");
				String jsoned = IOUtils.toString(co.getInputStream());*/
				System.out.println("Time to download : " + (System.currentTimeMillis() - timestamp) + " ms");
				timestamp = System.currentTimeMillis();
				Set set = Set.read(jsoned);
				System.out.println("Time to read : " + (System.currentTimeMillis() - timestamp) + " ms");
				System.out.println(set.getType());
				sets.put(set.getCode(), set);
				for (Card card : set.getCards())
				{
					int muId = card.getMuId().get("en");
					insertSingleCard(card);
					if (cards.containsKey(muId))
					{
						cards.get(muId).setRelated(card);
						card.setRelated(cards.get(muId));
					}
					else
						cards.put(muId, card);
				}
				timestamp = System.currentTimeMillis();
				insert("sets", new String[]{"name", "name_DE", "name_FR", "name_IT", "name_ES", "name_PT", "name_JP", "name_CN", "name_TW", "name_KO", "code", "magic_cards_info_code",
								"release_date", "type", "block", "booster", "border", "finished_translations", "mkm_id", "mkm_name"}, set.getName(), set.translations.get("de"), set
								.translations.get("fr"), set.translations.get("it"), set.translations.get("es"), set.translations.get("pt"), set.translations.get("jp"), set
								.translations.get
										("cn"), set.translations.get("tw"), set.translations.get("ko"), set.getCode(), set.getMagicCardsInfoCode(), CardSerializer.DATE.serialize(set
								.getReleaseDate(),
						Date.class, null).getAsString(), set.getType(), set.getBlock(), new Gson().toJson(set.booster), set.getBorder(), set.getFinishedTranslations(), set.getMKMId(),
						set.getMKMName());
				connection.commit();
				System.out.println("Time to commit : " + (System.currentTimeMillis() - timestamp) + " ms");
			}
			Loading.setLabel("Mise à jour des traductions");
			if (Utils.getSide() == Side.CLIENT)
				sets.values().stream().forEach(set -> set.addLang(Config.getLocaleCode()));
			else
				sets.values().stream().forEach(set -> {
					for (String locale : LOCALES)
					{
						set.addLang(locale);
						try
						{
							connection.commit();
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}
				});
			connection.setAutoCommit(true);
			if (Utils.getSide() == Side.SERVER)
			{
				if (Double.isNaN(42.0D))
				{
					Pattern p = Pattern.compile(" \\(Version .*?\\)");
					for (Set set : getAllSets())
					{
						String response = request("/expansion/1/" + set.getName());
						if (response.contains("400 - Bad Request") || response.contains("error"))
							continue;
						set.setBuyable(true);
						JsonReader r = new JsonReader(new StringReader(response));
						r.beginObject();
						r.nextName();
						r.beginObject();
						r.nextName();
						r.nextInt();
						r.nextName();
						r.nextString();
						r.nextName();
						r.nextInt();
						r.endObject();
						r.nextName();
						r.beginArray();
						List<Integer> productIds = Lists.newArrayList();
						while (r.peek() != JsonToken.END_ARRAY)
						{
							r.beginObject();
							r.nextName();
							productIds.add(r.nextInt());
							while (r.peek() != JsonToken.END_OBJECT)
								r.skipValue();
							r.endObject();
						}
						r.endArray();
						r.endObject();
						r.close();
						Gson gson = new Gson();
						for (int productId : productIds)
						{
							response = request("/product/" + productId);
							if (response.isEmpty())
								continue;
							response = response.substring(11, response.length() - 1);
							String str = response;
							Product product = gson.fromJson(response, Product.class);
							String productName = product.name.get("1").productName;
							Card card;
							if (productName.contains("(Version"))
							{
								Matcher m = p.matcher(str);
								//noinspection ResultOfMethodCallIgnored
								m.find();
								String s = productName.replace(m.group(), "").trim();
								System.out.println("|" + m.group() + "|");
								String version = m.group().substring(10, m.group().length() - 1);
								System.out.println(version);
								int ver = Integer.parseInt(version);
								card = set.getCards().stream().filter(c -> c.getName().get("en").equals(s)).findAny().orElse(null);
								if (card == null)
									continue;
								System.out.println(Arrays.toString(card.getVariations()));
								if (card.getVariations() != null)
									card = getCard(card.getVariations()[Math.min(ver, card.getVariations().length) - 1]);
								System.out.println(card);
							}
							else
								card = set.getCards().stream().filter(c -> c.getName().get("en").equals(productName)).findAny().orElse(null);
							if (card == null)
								System.err.println(productName);
							else
							{
								card.setCost(product.priceGuide.SELL);
								card.setFoilCost(product.priceGuide.LOWFOIL);
							}
						}
					}
				}
				if (!checkexists("players"))
				{
					Column uuid = new Column("uuid", UUID);
					uuid.setPrimaryKey(true);
					Column name = new Column("name", VARCHAR);
					name.setNullable(false);
					Column email = new Column("email", VARCHAR);
					email.setNullable(false);
					Column sha1Pass = new Column("password_hash", VARCHAR);
					sha1Pass.setNullable(false);
					Column money = new Column("money", INTEGER);
					money.setNullable(false);
					money.setDefaultExpression(0);
					Column cards = new Column("cards", VARCHAR);
					cards.setNullable(false);
					cards.setDefaultExpression("[]");
					Column lastIp = new Column("last_ip", VARCHAR);
					lastIp.setNullable(false);
					createTable("players", uuid, name, email, sha1Pass, money, cards, lastIp);
				}
				if (!checkexists("decks"))
				{
					Column uuid = new Column("uuid", UUID);
					uuid.setPrimaryKey(true);
					Column free = new Column("free", BOOLEAN);
					uuid.setNullable(false);
					Column owner = new Column("owner", UUID);
					owner.setNullable(false);
					Column cards = new Column("cards", VARCHAR);
					cards.setNullable(false);
					Column sideboard = new Column("sideboard", VARCHAR);
					sideboard.setNullable(false);
					Column name = new Column("name", VARCHAR);
					name.setNullable(false);
					Column desc = new Column("desc", VARCHAR);
					name.setNullable(false);
					Column colors = new Column("colors", VARCHAR);
					colors.setNullable(false);
					Column legalities = new Column("legalities", VARCHAR);
					legalities.setNullable(false);
					createTable("decks", uuid, free, owner, cards, sideboard, name, desc, colors, legalities);
				}
			}
			System.out.println(sets.size() + " éditions chargées, comprenant un total de " + cards.size() + " cartes !");
			Loading.setLabel("Connexion au serveur ...");
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
		}
	}

	private static String request(String path)
	{
		try
		{
			return IOUtils.toString(
					new URL("http://" + (Utils.DEBUG ? "localhost/gatherplaying" : "gp.arathia.fr") + "/MKM.php?output=json&path=" + URLEncoder.encode(path, "UTF-8")));
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static Collection<Card> getAllCards() { return cards.values(); }

	public static Card getCard(int muId) { return cards.get(muId); }

	public static Set getSet(String code) { return sets.get(code); }

	public static Collection<Set> getAllSets() { return sets.values(); }

	public static void addCard(Card card)
	{
		cards.put(card.getMuId("en"), card);
		insertSingleCard(card);
	}

	private static void insertSingleCard(Card card) { insertCard(card); }

	public static void addSet(Set set)
	{
		sets.put(set.getCode(), set);
		insert("sets", new String[]{"name", "name_DE", "name_FR", "name_IT", "name_ES", "name_PT", "name_JP", "name_CN", "name_TW", "name_KO", "code", "magic_cards_info_code",
						"release_date", "type", "block", "booster", "border", "finished_translations", "mkm_id", "mkm_name"}, set.getName(), set.translations.get("de"), set
						.translations.get("fr"), set.translations.get("it"), set.translations.get("es"), set.translations.get("pt"), set.translations.get("jp"), set
						.translations.get("cn"), set.translations.get("tw"), set.translations.get("ko"), set.getCode(), set.getMagicCardsInfoCode(), CardSerializer.DATE.serialize(set
						.getReleaseDate(),
				Date.class, null).getAsString(), set.getType(), set.getBlock(), new Gson().toJson(set.booster), set.getBorder(), set.getFinishedTranslations(), set.getMKMId(),
				set.getMKMName());
	}

	private static class Column
	{
		private final String name;
		private final String type;
		private boolean primaryKey;
		private boolean nullable = true;
		private Serializable defaultExpression;
		private boolean autoIncrement;

		private Column(String name, String type)
		{
			this.name = name;
			this.type = type;
		}

		private String getName()
		{
			return name;
		}

		public String getType()
		{
			return type;
		}

		private boolean isPrimaryKey()
		{
			return primaryKey;
		}

		private void setPrimaryKey(boolean primaryKey)
		{
			this.primaryKey = primaryKey;
		}

		private boolean isNullable()
		{
			return nullable;
		}

		private void setNullable(boolean nullable)
		{
			this.nullable = nullable;
		}

		private Serializable getDefaultExpression()
		{
			return defaultExpression;
		}

		private void setDefaultExpression(Serializable defaultExpression)
		{
			if (defaultExpression instanceof String)
				defaultExpression = "'" + defaultExpression + "'";
			this.defaultExpression = defaultExpression;
		}

		private boolean isAutoIncrement()
		{
			return autoIncrement;
		}

		@SuppressWarnings("unused")
		public void setAutoIncrement(boolean autoIncrement)
		{
			this.autoIncrement = autoIncrement;
		}
	}

	public static class Value
	{
		private final String column;
		private final Serializable value;

		public Value(String column, Serializable value)
		{
			this.column = column;
			this.value = value;
		}
	}

	@SuppressWarnings("unused")
	public static class Condition
	{
		private static final Condition ALWAYS_TRUE = new Condition(Type.ALWAYS_TRUE);
		private static final Condition ALWAYS_FALSE = new Condition(Type.ALWAYS_FALSE);
		private final Type type;
		private final Value test;
		private Condition and;
		private Condition or;

		private Condition(Type type)
		{
			this.type = type;
			test = null;
		}

		private Condition(Type type, Value test)
		{
			this.type = type;
			this.test = test;
		}

		public static Condition equals(Value value) { return new Condition(Type.EQUALS, value); }

		public static Condition notEquals(Value value) { return new Condition(Type.NOT_EQUALS, value); }

		private static Condition like(Value value) { return new Condition(Type.LIKE, value); }

		public static Condition regexp(Value value) { return new Condition(Type.REGEXP, value); }

		public Condition and(Condition c)
		{
			and = c;
			return this;
		}

		public Condition or(Condition c)
		{
			or = c;
			return this;
		}

		@Override
		public String toString()
		{
			String ret = "(" + (test != null ? "`" + test.column + "` " : "") + type.operator + (test != null ? " '" + test.value.toString().replace("'", "\\'") + "'" : "");
			if (and != null)
				ret += " AND " + and;
			ret += ")";
			if (or != null)
				ret += " OR " + or;
			return ret;
		}

		enum Type
		{
			ALWAYS_TRUE("1"), ALWAYS_FALSE("0"), EQUALS("="), NOT_EQUALS("!="), LIKE("LIKE"), REGEXP("REGEXP");
			private final String operator;

			Type(String operator) { this.operator = operator; }
		}
	}
}