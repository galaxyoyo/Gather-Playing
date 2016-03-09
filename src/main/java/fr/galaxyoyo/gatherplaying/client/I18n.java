package fr.galaxyoyo.gatherplaying.client;

import com.google.common.base.MoreObjects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class I18n
{
	private static final ResourceBundle.Control control = new ResourceBundle.Control()
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
	};
	private static ResourceBundle bundle, bundle_EN = ResourceBundle.getBundle("", Locale.US, control);

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
			return String.format(bundle.getString(toTranslate), (Object[]) args);
		} catch (MissingResourceException ex)
		{
			try
			{
				return bundle_EN.getString(toTranslate);
			} catch (MissingResourceException e)
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
		} catch (MissingResourceException ex)
		{
			return toTranslate;
		}
	}

	public static void reloadTranslations() throws IOException { bundle = ResourceBundle.getBundle("", Config.getLocale(), control); }
}