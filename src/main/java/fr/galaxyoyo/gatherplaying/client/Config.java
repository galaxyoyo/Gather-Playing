package fr.galaxyoyo.gatherplaying.client;

import fr.galaxyoyo.gatherplaying.MySQL;
import fr.galaxyoyo.gatherplaying.Rules;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.Locale;

public class Config
{
	private static final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(Locale.getDefault());
	private static final ObjectProperty<Rules> format = new SimpleObjectProperty<>(Rules.LEGACY);
	private static final BooleanProperty stayLogged = new SimpleBooleanProperty(false);
	private static final BooleanProperty hqCards = new SimpleBooleanProperty(false);
	private static final BooleanProperty stackCards = new SimpleBooleanProperty(false);

	public static Rules getFormat()
	{
		return format.get();
	}

	public static ObjectProperty<Rules> formatProperty()
	{
		return format;
	}

	public static Locale getLocale()
	{
		return locale.get();
	}

	public static String getLocaleCode()
	{
		if (locale.get().getLanguage().equals("zh"))
			return locale.get().getCountry().toLowerCase();
		else if (locale.get().getLanguage().equals("ja"))
			return "jp";
		return locale.get().getLanguage().toLowerCase();
	}

	public static ObjectProperty<Locale> localeProperty()
	{
		return locale;
	}

	public static boolean getStayLogged()
	{
		return stayLogged.get();
	}

	public static BooleanProperty stayLoggedProperty()
	{
		return stayLogged;
	}

	public static boolean getHqCards() { return hqCards.get(); }

	public static BooleanProperty hqCardsProperty() { return hqCards; }

	public static boolean getStackCards()
	{
		return stackCards.get();
	}

	public static BooleanProperty stackCardsProperty() { return stackCards; }

	public static void init()
	{
		locale.addListener((observable, oldValue, newValue) -> {
			Locale.setDefault(newValue);
			MySQL.setConfig("preferred-language", Locale.getDefault().toString().toLowerCase());
			try
			{
				I18n.reloadTranslations();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		});
		String prefLg = MySQL.getConfig("preferred-language", Locale.getDefault().toString().toLowerCase());
		if (prefLg.contains("_"))
			locale.setValue(new Locale(prefLg.split("_")[0], prefLg.split("_")[1].toUpperCase()));
		else
			locale.set(new Locale(prefLg));

		format.set(Rules.valueOf(MySQL.getConfig("preferred-format", "LEGACY")));
		format.addListener((observable, oldValue, newValue) -> MySQL.setConfig("preferred-format", newValue.name()));

		stayLogged.addListener((observable, oldValue, newValue) -> MySQL.setConfig("stay-logged", newValue.toString()));

		hqCards.set(MySQL.getBooleanConfig("hq-cards", false));
		hqCards.addListener((observable, oldValue, newValue) -> MySQL.setConfig("hq-cards", newValue.toString()));

		stackCards.set(MySQL.getBooleanConfig("stack-cards", false));
		stackCards.addListener((observable, oldValue, newValue) -> MySQL.setConfig("stack-cards", newValue.toString()));
	}
}
