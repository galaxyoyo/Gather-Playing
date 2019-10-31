package fr.galaxyoyo.gatherplaying.client;

import com.google.common.base.MoreObjects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static fr.galaxyoyo.gatherplaying.client.Config.getLocale;

public class I18n
{
/*	private static final ResourceBundle.Control control = new ResourceBundle.Control()
	{
		@Override
		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException
		{
			if (!format.equals("java.properties"))
				return null;
			ResourceBundle bundle = null;
			InputStream is = getClass().getResourceAsStream("/translations/" + locale + ".properties");
			if (is != null)
				bundle = new PropertyResourceBundle(new InputStreamReader(is, StandardCharsets.UTF_8));
			return bundle;
		}
	};*/

	private static ResourceBundle bundle, bundle_EN = null;

	static {
		try
		{
			bundle_EN = new PropertyResourceBundle(I18n.class.getResourceAsStream("/translations/en.properties"));
			bundle = bundle_EN;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public static StringBinding tr(String toTranslate, String... args)
	{
		return Bindings.createStringBinding(() -> strTr(toTranslate, args), Config.localeProperty());
	}

	public static String strTr(String toTranslate, String... args)
	{
		try
		{
			args = MoreObjects.firstNonNull(args, new String[0]);
			for (int i = 0; i < args.length; ++i)
				args[i] = strTr(args[i]);
			return String.format(bundle.getString(toTranslate.replace(" ", "")), (Object[]) args);
		}
		catch (MissingResourceException ex)
		{
			try
			{
				return bundle_EN.getString(toTranslate.replace(" ", ""));
			}
			catch (MissingResourceException e)
			{
				return toTranslate;
			}
		}
	}

	public static String entr(String toTranslate, String... args)
	{
		try
		{
			args = MoreObjects.firstNonNull(args, new String[0]);
			for (int i = 0; i < args.length; ++i)
				args[i] = entr(args[i]);
			return String.format(bundle_EN.getString(toTranslate), (Object[]) args);
		}
		catch (MissingResourceException ex)
		{
			return toTranslate;
		}
	}

	public static void reloadTranslations() throws IOException { bundle =
			new PropertyResourceBundle(I18n.class.getResourceAsStream("/translations/" + getLocale().getLanguage() + ".properties")); }
}