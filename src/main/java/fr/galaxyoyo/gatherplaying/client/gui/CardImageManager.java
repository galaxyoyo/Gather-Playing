package fr.galaxyoyo.gatherplaying.client.gui;

import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Config;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class CardImageManager
{
	private static final Map<Integer, Image> images = Maps.newHashMap();
	@SuppressWarnings("unused")
	private static final Map<Integer, Image> generateds = Maps.newHashMap();
	private static final Map<Token, Image> tokens = Maps.newHashMap();
	private static final Map<String, Image> icons = Maps.newHashMap();
	private static final File DIR = Utils.newFile("pics");
	private static final Image FOIL = new Image(CardImageManager.class.getResourceAsStream("/templates/foil.png"));

	static Image getImage(PlayedCard card)
	{
		if (card == null)
			return getImage((Card) null);
		else if (card.isCard())
			return getImage(card.getCard());
		else
			return getImage(card.getToken());
	}

	public static Image getImage(Card card)
	{
		Integer muId = card == null ? null : card.getMuId(Config.getLocaleCode());
		String locale = Config.getLocaleCode();
		if (images.containsKey(card == null ? "" : muId))
			return images.get(card == null ? "" : muId);
		try
		{
			File file = new File(DIR, card == null ? "back.png" : card.getSet().getCode().replace("CON", "CON ") + File.separatorChar + card.getPreferredMuID() + ".png");
			if (!file.getParentFile().exists())
				//noinspection ResultOfMethodCallIgnored
				file.getParentFile().mkdirs();
			else
			{
				if (file.exists())
				{
					Image img = new Image(file.toURI().toURL().toString(), true);
					images.put(muId, img);
					return img;
				}
			}
			URL url;
			if (card == null)
				url = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=-1&type=card");
			else
			{
				Image img = new Image("http://galaxyoyo.com:42000/render-card?muId=" + card.getMuId("en") + "&locale=" + Config.getLocaleCode());
				if (!img.isError())
				{
					img.progressProperty().addListener((observable, oldValue, newValue) ->
					{
						if (newValue.doubleValue() >= 1.0D)
						{
							try
							{
								File f = new File(DIR, card.getSet().getCode().replace("CON", "CON ") + File.separatorChar + card.getPreferredMuID() + "_HQ.png");
								if (Utils.isDesktop())
									ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", f);
								else
									FileUtils.copyURLToFile(new URL("http://galaxyoyo.com:42000/render-card?muId=" + card.getMuId("en") + "&locale=" + Config.getLocaleCode()), f);
								images.put(muId, img);
							}
							catch (IOException ex)
							{
								ex.printStackTrace();
							}
						}
					});
					return img;
				}

				url = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + (card.getMuId(locale) != null ? card.getMuId(locale) : muId) + "&type=card");
			}
			HttpURLConnection co = (HttpURLConnection) url.openConnection();
			co.connect();
			if (card != null && (co.getResponseCode() == 404 || co.getContentLength() == 73739L || co.getContentLength() == 0L))
			{
				co.disconnect();
				co = (HttpURLConnection) new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + muId + "&type=card").openConnection();
				co.connect();
			}
			Image img = new Image(co.getURL().toString(), 0, 0, true, true, true);
			if (img.isError())
			{
				System.err.println("Can't get image for " + card + " (" + co.getURL() + ")");
				return getImage((Card) null);
			}

			URL finalUrl = url;
			img.progressProperty().addListener((observable, oldValue, newValue) ->
			{
				if (newValue.doubleValue() >= 1.0D)
				{
					if (img.isError())
						return;
					try
					{
						if (Utils.isDesktop())
							ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", file);
						else
							FileUtils.copyURLToFile(finalUrl, file);
					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
				}
			});
			images.put(card == null ? null : muId, img);
			return img;
		}
		catch (IOException ex)
		{
			//	ex.printStackTrace();
			if (card != null)
				return getImage((Card) null);
			return null;
		}
	}

	public static Image getImage(Token token)
	{
		if (tokens.containsKey(token))
			return tokens.get(token);
		try
		{
			File file = new File(DIR, "tokens" + File.separatorChar + token.getSet().getCode().replace("CON", "CON ") + File.separatorChar + token.name().toLowerCase() + "_fr.jpg");
			if (!file.getParentFile().exists())
				//noinspection ResultOfMethodCallIgnored
				file.getParentFile().mkdirs();
			else if (file.exists())
			{
				Image img = new Image(file.toURI().toURL().toString());
				tokens.put(token, img);
				return img;
			}
			String url = "http://cartes.mtgfrance.com/images/cards/fr/token/" + token.getSet().getMagicCardsInfoCode().toLowerCase() + "/" + token.getNumber() + ".jpg";
			url = "http://galaxyoyo.com:42000/render-token?token=" + token.name().toLowerCase() + "&locale=" + Config.getLocaleCode();
			Image img = new Image(url, 223.0D, 310.0D, true, true);
			if (img.isError())
			{
				url = "http://cartes.mtgfrance.com/images/cards/en/token/" + token.getSet().getMagicCardsInfoCode().toLowerCase() + "/" + token.getNumber() + ".jpg";
				img = new Image(url, 223.0D, 310.0D, true, true);
				if (img.isError())
				{
					System.err.println("Can't resolve: " + url + " for token " + token);
					return null;
				}
			}

			if (Utils.isDesktop())
				ImageIO.write(SwingFXUtils.fromFXImage(img, null), "JPEG", file);
			else
				FileUtils.copyURLToFile(new URL(url), file);
			tokens.put(token, img);
			return img;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unused")
	public static Image getFoilCover() { return FOIL; }

	public static Image getIcon(ManaColor mc) { return getIcon(mc, true); }

	private static Image getIcon(ManaColor mc, boolean small) { return getIcon0(mc.getAbbreviate(), small); }

	private static Image getIcon0(String id, boolean small)
	{
		id = id.replace("/", "");
		if (icons.containsKey(id))
			return icons.get(id);
		try
		{
			File file = getIconFile(id, small);
			if (!file.getParentFile().exists())
				//noinspection ResultOfMethodCallIgnored
				file.getParentFile().mkdirs();
			else if (file.exists())
			{
				Image img = new Image(file.toURI().toURL().toString());
				icons.put(id, img);
				return img;
			}
			String url = "http://www.magiccorporation.com/images/magic/manas/" + (small ? "mini" : "maxi") + "/" + id.toLowerCase() + ".gif";
			Image img;
			if (id.equals("1000000"))
				url = "http://gatherer.wizards.com/Handlers/Image.ashx?size=" + (small ? "small" : "medium") + "&name=" + id + "&type=symbol";
			else if (id.contains("10") || id.equals("Infinity") || id.contains("Half") || id.contains("P"))
				url = "http://gatherer.wizards.com/Handlers/Image.ashx?size=medium&name=" + id + "&type=symbol";
			if (!id.equals("1000000") && (id.contains("10") || id.equals("Infinity") || id.contains("Half") || id.contains("P")))
				img = new Image(url, 18, 18, true, true);
			else
				img = new Image(url);
			icons.put(id, img);
			if (Utils.isDesktop())
				ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", file);
			else
				FileUtils.copyURLToFile(new URL(url), file);
			return img;
		}
		catch (IOException | IllegalArgumentException ex)
		{
			System.err.println("Couldn't get icon for " + id);
			return null;
		}
	}

	public static File getIconFile(String icon, boolean small) { return new File(DIR, "icons" + File.separatorChar + icon + (small ? "_small" : "") + ".png"); }

	public static Image getIcon(String id, boolean small) { return getIcon0(id, small); }
}