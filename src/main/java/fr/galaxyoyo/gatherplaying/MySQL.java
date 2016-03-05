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
import java8.util.stream.StreamSupport;
import javafx.collections.FXCollections;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
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
	private static final Map<String, Card> cards = Maps.newHashMap();
	private static final Map<String, Set> sets = Maps.newHashMap();
	private static final String BOOLEAN = "BOOLEAN";
	private static final String INTEGER = "INT";
	private static final String DOUBLE = "DOUBLE";
	private static final String VARCHAR = "VARCHAR";
	private static final String UUID = "UUID";
	private static final Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, CardSerializer.DATE).registerTypeAdapter(SubType.class, CardSerializer.SUBTYPE)
			.registerTypeAdapter(ManaColor.class, CardSerializer.MANACOLOR).create();
	private static final String[] CARD_COLUMNS =
			new String[] {"id_EN", "id_DE", "id_FR", "id_IT", "id_ES", "id_PT", "id_RU", "id_CN", "id_TW", "id_JP", "id_KO", "name_EN", "name_DE", "name_FR", "name_IT", "name_ES",
					"name_PT", "name_RU", "name_CN", "name_TW", "name_JP", "name_KO", "mci_number", "set", "type", "subtypes", "legendary", "basic", "world", "snow", "ongoing",
					"power", "toughness", "loyalty", "mana_cost", "converted_manacost", "colors", "color_identity", "variations", "ability_EN", "ability_DE", "ability_FR",
					"ability_IT", "ability_ES", "ability_PT", "ability_RU", "ability_CN", "ability_TW", "ability_JP", "ability_KO", "flavor_EN", "flavor_DE", "flavor_FR",
					"flavor_IT", "flavor_ES", "flavor_PT", "flavor_RU", "flavor_CN", "flavor_TW", "flavor_JP", "flavor_KO", "rarity", /*"rulings", */"layout", "artist",
					"image_name", "watermark"};
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

	public static boolean getBooleanConfig(String key, boolean defaultValue)
	{
		return Boolean.parseBoolean(getConfig(key, Boolean.toString(defaultValue)));
	}

	public static boolean setConfig(String key, String value)
	{
		key = key.toUpperCase();
		if (delete("config", Condition.equals(new Value("key", key))) < 0 || insert("config", new String[] {"key", "value"}, key, value) < 0)
			return false;
		config.put(key, value);
		return true;
	}

	public static ResultSet query(String table) { return query(table, Condition.ALWAYS_TRUE); }

	public static ResultSet query(String table, Condition c)
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

	public static boolean checkexists(String table)
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
				card.name.put(locale, set.getString("name_" + locale.toUpperCase()));
				card.muId.put(locale, set.getString("id_" + locale.toUpperCase()));
				card.ability.put(locale, set.getString("ability_" + locale.toUpperCase()));
				card.flavor.put(locale, set.getString("flavor_" + locale.toUpperCase()));
			}
			String ed = set.getString("set");
			card.mciNumber = set.getString("mci_number");
			card.set = sets.get(ed);
			card.set.cards.add(card);
			card.type = cardTypes.get(set.getString("type").toUpperCase());
			card.subtypes = gson.fromJson(set.getString("subtypes"), SubType[].class);
			for (SubType st : card.subtypes)
				st.setCanApplicate(card.type);
			card.legendary = set.getBoolean("legendary");
			card.basic = set.getBoolean("basic");
			card.world = set.getBoolean("world");
			card.snow = set.getBoolean("snow");
			card.ongoing = set.getBoolean("ongoing");
			card.power = set.getString("power");
			card.toughness = set.getString("toughness");
			card.loyalty = set.getInt("loyalty");
			card.manaCost = gson.fromJson(set.getString("mana_cost").toUpperCase(), ManaColor[].class);
			card.cmc = set.getDouble("converted_manacost");
			card.colors = gson.fromJson(set.getString("colors").toUpperCase(), ManaColor[].class);
			card.colorIdentity = gson.fromJson(set.getString("color_identity").toUpperCase(), ManaColor[].class);
			card.variations = gson.fromJson(set.getString("variations"), int[].class);
			card.rarity = Rarity.valueOf(set.getString("rarity").toUpperCase());
			card.layout = Layout.valueOf(set.getString("layout").toUpperCase());
			card.artist = set.getString("artist");
			card.imageName = set.getString("image_name");
			card.watermark = set.getString("watermark");
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		return card;
	}

	public static void insertSingleCard(Card card) { insertCard(card); }

	public static void updateCard(Card card)
	{
		delete("cards", Condition.equals(new Value("id_EN", card.muId.get("en"))));
		insertCard(card);
	}

	public static void insertCard(Card card)
	{
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, CardSerializer.DATE).registerTypeAdapter(SubType.class, CardSerializer.SUBTYPE)
				.registerTypeAdapter(ManaColor.class, CardSerializer.MANACOLOR).create();
		assert card != null;
		assert gson.toJson(card.manaCost).toLowerCase() != null;
		assert gson.toJson(card.colors).toLowerCase() != null;
		assert gson.toJson(card.colorIdentity).toLowerCase() != null;
		assert card.set != null;
		assert card.type != null;
		assert card.rarity != null;
		assert card.layout != null;

		insert("cards", CARD_COLUMNS, card.muId.get("en"), card.muId.get("de"), card.muId.get("fr"), card.muId.get("it"), card.muId.get("es"), card.muId.get("pt"),
				card.muId.get("ru"), card.muId.get("cn"), card.muId.get("tw"), card.muId.get("jp"), card.muId.get("ko"), card.name.get("en"), card.name.get("de"),
				card.name.get("fr"), card.name.get("it"), card.name.get("es"), card.name.get("pt"), card.name.get("ru"), card.name.get("cn"), card.name.get("tw"),
				card.name.get("jp"), card.name.get("ko"), card.mciNumber, card.set.code, card.type.name().toLowerCase(), gson.toJson(card.subtypes), card.legendary, card.basic,
				card.world, card.snow, card.ongoing, card.power, card.toughness, card.loyalty, gson.toJson(card.manaCost).toLowerCase(), card.cmc,
				gson.toJson(card.colors).toLowerCase(), gson.toJson(card.colorIdentity).toLowerCase(), gson.toJson(card.variations), card.ability.get("en"), card.ability.get("de"),
				card.ability.get("fr"), card.ability.get("it"), card.ability.get("es"), card.ability.get("pt"), card.ability.get("ru"), card.ability.get("cn"),
				card.ability.get("tw"), card.ability.get("jp"), card.ability.get("ko"), card.flavor.get("en"), card.flavor.get("de"), card.flavor.get("fr"), card.flavor.get("it"),
				card.flavor.get("es"), card.flavor.get("pt"), card.flavor.get("ru"), card.flavor.get("cn"), card.flavor.get("tw"), card.flavor.get("jp"), card.flavor.get("ko"),
				card.rarity.name().toLowerCase(), card.layout.name().toLowerCase(), card.artist, card.imageName, card.watermark);
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
				insert("players", new String[] {"uuid", "name", "email", "password_hash", "last_ip"}, player.uuid, player.name, player.email, player.sha1Pwd, player.lastIp);
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
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
				deck.uuid = java.util.UUID.fromString(set.getString("uuid"));
				deck.free = set.getBoolean("free");
				deck.name.setValue(set.getString("name"));
				deck.desc = set.getString("desc");
				deck.owner = player;
				deck.cards = FXCollections.observableSet((java.util.Set<OwnedCard>) gson.fromJson(set.getString("cards"), new TypeToken<HashSet<OwnedCard>>() {}.getType()));
				deck.sideboard = FXCollections.observableSet((java.util.Set<OwnedCard>) gson.fromJson(set.getString("sideboard"), new TypeToken<HashSet<OwnedCard>>() {}.getType
						()));
				deck.colors = gson.fromJson(set.getString("colors"), ManaColor[].class);
				deck.legalities = gson.fromJson(set.getString("legalities"), new TypeToken<HashSet<Rules>>() {}.getType());
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
		delete("decks", Condition.equals(new Value("uuid", deck.uuid)));
		insert("decks", deck.uuid, deck.free, deck.owner.uuid, gson.toJson(deck.cards), gson.toJson(deck.sideboard), deck.name.getValue(), deck.desc, gson.toJson(deck.colors),
				gson.toJson(deck.legalities));
	}

	public static int createTable(String name, Column... columns)
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

	public static int insert(String table, Object... values)
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

	public static int insert(String table, String[] columns, Object... values)
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

	public static void init()
	{
		try
		{
			if (Utils.isMobile())
				DriverManager.registerDriver((Driver) Class.forName("org.sqldroid.SQLDroidDriver").newInstance());
			connection = DriverManager.getConnection("jdbc:sqlite:" + Utils.newFile("cards.db").getAbsolutePath());
			connection.createStatement().execute("PRAGMA encoding = \"UTF-8\";");
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
								name_ES, name_PT, name_RU, name_CN, name_TW, name_JP, name_KO, mciNumber, set, type, subtypes, legendary, basic, world, snow, ongoing, power,
								toughness, loyalty, manaCost, cmc, colors, colorIdentity, variations, ability_EN, ability_DE, ability_FR, ability_IT, ability_ES, ability_PT,
								ability_RU, ability_CN, ability_TW, ability_JP, ability_KO, flavor_EN, flavor_DE, flavor_FR, flavor_IT, flavor_ES, flavor_PT, flavor_RU, flavor_CN,
								flavor_TW, flavor_JP, flavor_KO, rarity, layout, artist, imageName, watermark);
				createTable("cards", columns.toArray(new Column[columns.size()]));
			}
			Loading.setLabel("Détection des paramètres");
			String actualDBVersion = IOUtils.toString(new URL("http://" + (Utils.DEBUG ? "localhost/gatherplaying" : "gatherplaying.arathia.fr") + "/DB_VERSION.php"));
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
						new URL("http://" + (Utils.DEBUG ? "localhost/gatherplaying" : "gatherplaying.arathia.fr") + "/UpdateDB.php?old=" + getConfig("dbversion", "1.0"))));
				setConfig("dbversion", actualDBVersion);
			}
			Config.init();

			Loading.setLabel("Recherche des extensions locales ...");
			ResultSet setsSet = query("sets");
			while (setsSet.next())
			{
				Set set = new Set();
				set.name = setsSet.getString("name");
				set.translations.put("de", setsSet.getString("name_DE"));
				set.translations.put("fr", setsSet.getString("name_FR"));
				set.translations.put("it", setsSet.getString("name_IT"));
				set.translations.put("es", setsSet.getString("name_ES"));
				set.translations.put("pt", setsSet.getString("name_PT"));
				set.translations.put("jp", setsSet.getString("name_JP"));
				set.translations.put("cn", setsSet.getString("name_CN"));
				set.translations.put("tw", setsSet.getString("name_TW"));
				set.translations.put("ko", setsSet.getString("name_KO"));
				set.code = setsSet.getString("code");
				set.magicCardsInfoCode = setsSet.getString("magic_cards_info_code");
				set.releaseDate = CardSerializer.DATE.deserialize(new JsonPrimitive(setsSet.getString("release_date")), Date.class, null);
				set.type = setsSet.getString("type");
				set.block = setsSet.getString("block");
				set.booster = new Gson().fromJson(setsSet.getString("booster"), Object[].class);
				set.border = setsSet.getString("border");
				set.finishedTranslations = setsSet.getString("finished_translations");
				set.mkm_id = setsSet.getInt("mkm_id");
				set.mkm_name = setsSet.getString("mkm_name");
				sets.put(set.code, set);
				Loading.setLabel(setsSet.getRow() + " extensions chargées");
			}
			Loading.setLabel("Recherche des cartes locales ...");
			long timestamp = System.currentTimeMillis();
			ResultSet cardsSet = query("cards");
			while (cardsSet.next())
			{
				Card card = getCard(cardsSet);
				cards.put(card.muId.get("en"), card);
				if (Utils.isDesktop() || cardsSet.getRow() % 100 == 0)
					Loading.setLabel(cardsSet.getRow() + " cartes chargées");
			}
			System.err.println("Time to read " + cards.size() + " cards : " + (System.currentTimeMillis() - timestamp) + " ms");
			Loading.setLabel("Recherche de mise à jour de la base de données ...");
			String[] setCodes;
			try
			{
				setCodes = new GsonBuilder().registerTypeAdapter(Date.class, CardSerializer.DATE).create()
						.fromJson(IOUtils.toString(new URL("http://mtgjson.com/json/SetCodes.json"), "UTF-8"), String[].class);
			}
			catch (IOException ex)
			{
				setCodes = new String[sets.size()];
				int i = 0;
				for (Iterator<Set> $i = sets.values().iterator(); i < sets.size() && $i.hasNext(); ++i)
					setCodes[i] = $i.next().code;
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
				//	String jsoned = IOUtils.toString(new URL("http://mtgjson.com/json/" + code + "-x.json"), StandardCharsets.UTF_8);
				String jsoned = IOUtils.toString(new URL("http://gatherplaying.arathia.fr/json/" + code.replace("CON", "CON_") + ".json"), StandardCharsets.UTF_8);
				System.out.println("Time to download : " + (System.currentTimeMillis() - timestamp) + " ms");
				timestamp = System.currentTimeMillis();
				Set set = Set.read(jsoned);
				System.out.println("Time to read : " + (System.currentTimeMillis() - timestamp) + " ms");
				System.out.println(set.type);
				sets.put(set.code, set);
				for (Card card : set.cards)
				{
					String muId = card.muId.get("en");
					if (cards.containsKey(muId))
						muId += "b";
					while (cards.containsKey(muId))
						muId = muId.substring(0, muId.length() - 1) + (char) (muId.charAt(muId.length() - 1) + 1);
					card.muId.put("en", muId);
					insertSingleCard(card);
					cards.put(muId, card);
				}
				set.addLang(Config.getLocaleCode());
				timestamp = System.currentTimeMillis();
				insert("sets", new String[] {"name", "code", "magic_cards_info_code", "release_date", "type", "block", "booster", "border"}, set.name, set.code,
						set.magicCardsInfoCode, CardSerializer.DATE.serialize(set.releaseDate, Date.class, null).getAsString(), set.type, set.block, new Gson().toJson(set.booster),
						set.border);
				connection.commit();
				System.out.println("Time to commit : " + (System.currentTimeMillis() - timestamp) + " ms");
			}
			Loading.setLabel("Mise à jour des traductions");
			StreamSupport.stream(sets.values()).filter(set -> !set.finishedTranslations.contains(Config.getLocaleCode())).forEach(set -> set.addLang(Config.getLocaleCode()));
			connection.commit();
			connection.setAutoCommit(true);
			if (Utils.getSide() == Side.SERVER)
			{
				if (Double.isNaN(42.0D))
				{
					Pattern p = Pattern.compile(" \\(Version .*?\\)");
					for (Set set : getAllSets())
					{
						String response = request("/expansion/1/" + set.name);
						if (response.contains("400 - Bad Request") || response.contains("error"))
							continue;
						set.buyable = true;
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
								card = StreamSupport.stream(set.cards).filter(c -> c.name.get("en").equals(s)).findAny().orElse(null);
								if (card == null)
									continue;
								System.out.println(Arrays.toString(card.variations));
								if (card.variations != null)
									card = getCard(String.valueOf(card.variations[Math.min(ver, card.variations.length) - 1]));
								System.out.println(card);
							}
							else
								card = StreamSupport.stream(set.cards).filter(c -> c.name.get("en").equals(productName)).findAny().orElse(null);
							if (card == null)
								System.err.println(productName);
							else
							{
								card.cost = product.priceGuide.SELL;
								card.foilCost = product.priceGuide.LOWFOIL;
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

	public static String request(String path)
	{
		try
		{
			return IOUtils.toString(
					new URL("http://" + (Utils.DEBUG ? "localhost/gatherplaying" : "gatherplaying.arathia.fr") + "/MKM.php?output=json&path=" + URLEncoder.encode(path, "UTF-8")));
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static Collection<Card> getAllCards() { return cards.values(); }

	public static Card getCard(String muId) { return cards.get(muId); }

	public static Set getSet(String code) { return sets.get(code); }

	public static Collection<Set> getAllSets() { return sets.values(); }

	public static class Column
	{
		private final String name;
		private final String type;
		private boolean primaryKey;
		private boolean nullable = true;
		private Serializable defaultExpression;
		private boolean autoIncrement;

		public Column(String name, String type)
		{
			this.name = name;
			this.type = type;
		}

		public String getName()
		{
			return name;
		}

		public String getType()
		{
			return type;
		}

		public boolean isPrimaryKey()
		{
			return primaryKey;
		}

		public void setPrimaryKey(boolean primaryKey)
		{
			this.primaryKey = primaryKey;
		}

		public boolean isNullable()
		{
			return nullable;
		}

		public void setNullable(boolean nullable)
		{
			this.nullable = nullable;
		}

		public Serializable getDefaultExpression()
		{
			return defaultExpression;
		}

		public void setDefaultExpression(Serializable defaultExpression)
		{
			if (defaultExpression instanceof String)
				defaultExpression = "'" + defaultExpression + "'";
			this.defaultExpression = defaultExpression;
		}

		public boolean isAutoIncrement()
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
		public static final Condition ALWAYS_TRUE = new Condition(Type.ALWAYS_TRUE);
		public static final Condition ALWAYS_FALSE = new Condition(Type.ALWAYS_FALSE);
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

		public static Condition like(Value value) { return new Condition(Type.LIKE, value); }

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