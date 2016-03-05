package fr.galaxyoyo.gatherplaying;

import java.util.Map;

public class Product
{
	public int idProduct;
	public int idMetaproduct;
	public int idGame;
	public int countReprints;
	public Map<String, LanguageInfo> name;
	public String website;
	public String image;
	public Category category;
	public PriceGuide priceGuide;
	public String expansion;
	public String number;
	public String rarity;

	public static class LanguageInfo
	{
		public int idLanguage;
		public String languageName;
		public String productName;
	}

	public static class Category
	{
		public int idCategory;
		public String categoryName;
	}

	public class PriceGuide
	{
		public double SELL;
		public double LOW;
		public double LOWEX;
		public double LOWFOIL;
		public double AVG;
		public double TREND;
	}
}