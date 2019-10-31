package fr.galaxyoyo.gatherplaying;

import fr.galaxyoyo.gatherplaying.client.I18n;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Rules
{
	DRAFT, SEALED, STANDARD, MODERN, COMMANDER, LEGACY, VINTAGE, KAMIGAWA_BLOCK, ICE_AGE_BLOCK, INNISTRAD_BLOCK, INVASION_BLOCK, LORWYN_SHADOWMOOR_BLOCK, MASQUES_BLOCK, MIRAGE_BLOCK,
	MIRRODIN_BLOCK, ODYSSEY_BLOCK, ONSLAUGHT_BLOCK, RAVNICA_BLOCK, RETURN_TO_RAVNICA_BLOCK, SCARS_OF_MIRRODIN_BLOCK, SHARDS_OF_ALARA_BLOCK, KHANS_OF_TARKIR_BLOCK, TEMPEST_BLOCK,
	THEROS_BLOCK, TIME_SPIRAL_BLOCK, URZA_BLOCK, ZENDIKAR_BLOCK, BATTLE_FOR_ZENDIKAR_BLOCK, SHADOWS_OVER_INNISTRAD_BLOCK, KALADESH_BLOCK, AMONKHET_BLOCK, IXALAN_BLOCK, UN_SETS,
	FREEFORM;
	// CLASSIC, WARS_LEGACY, TRIBAL_WARS_LEGACY, TRIBAL_WARS_STANDARD, PRISMATIC, SINGLETON_100

	private final ObservableSet<String> legals = FXCollections.observableSet("Plains", "Mountain", "Swamp", "Forest", "Island");

	public boolean isLimited()
	{
		return this == DRAFT || this == SEALED;
	}

	@Override
	public String toString() { return getTranslatedName().get(); }

	public StringBinding getTranslatedName() { return I18n.tr("rules." + name().toLowerCase()); }

	public boolean isLegal(Card card)
	{
		if (legals.contains(card.getName().get("en")))
			return true;

		switch (this)
		{
			case FREEFORM:
				return true;
			case STANDARD:
				switch (card.getName().get("en"))
				{
					case "Aetherworks Marvel":
					case "Felidar Guardian":
					case "Smuggler's Copter":
						return false;
				}
				switch (card.getSet().getCode())
				{
					case "KLD":
					case "AER":
					case "AKH":
					case "HOU":
					case "IXN":
					case "W17":
					case "RIX":
						legals.add(card.getName().get("en"));
						return true;
					default:
						for (Set set : Arrays.stream(new String[]{"KLD", "AER", "AKH", "HOU", "IXN", "W17", "RIX"}).map(MySQL::getSet).collect(Collectors.toSet()))
						{
							if (set == null)
								continue;

							for (Card c : set.getCards())
							{
								if (card.getName().get("en").equals(c.getName().get("en")))
								{
									legals.add(card.getName().get("en"));
									return true;
								}
							}
						}
						return false;
				}
			case VINTAGE:
				if (card.getType() == CardType.CONSPIRACY)
					return false;
				switch (card.getName().get("en"))
				{
					case "Amulet of Quoz":
					case "Bronze Tablet":
					case "Chaos Orb":
					case "Contract from Below":
					case "Darkpact":
					case "Demonic Attorney":
					case "Falling Star":
					case "Jeweled Bird":
					case "Rebirth":
					case "Shahrazad":
					case "Tempest Efreet":
					case "Timmerian Fiends":
						return false;
					default:
						return card.getSet().getBorder().equalsIgnoreCase("black") || card.getSet().getBorder().equalsIgnoreCase("white");
				}
			case LEGACY:
				if (card.getType() == CardType.CONSPIRACY)
					return false;
				switch (card.getName().get("en"))
				{
					case "Amulet of Quoz":
					case "Ancestral Recall":
					case "Balance":
					case "Bazaar of Baghdad":
					case "Black Lotus":
					case "Bronze Tablet":
					case "Channel":
					case "Chaos Orb":
					case "Contract from Below":
					case "Darkpact":
					case "Demonic Attorney":
					case "Demonic Consultation":
					case "Demonic Tutor":
					case "Dig Through Time":
					case "Earthcraft":
					case "Falling Star":
					case "Fastbond":
					case "Flash":
					case "Frantic Search":
					case "Goblin Recruiter":
					case "Gush":
					case "Hermit Druid":
					case "Imperial Seal":
					case "Jeweled Bird":
					case "Library of Alexandria":
					case "Mana Crypt":
					case "Mana Drain":
					case "Mana Vault":
					case "Memory Jar":
					case "Mental Misstep":
					case "Mind Twist":
					case "Mind 's Desire":
					case "Mishra's Workshop":
					case "Mox Emerald":
					case "Mox Jet":
					case "Mox Pearl":
					case "Mox Ruby":
					case "Mox Sapphire":
					case "Mystical Tutor":
					case "Necropotence":
					case "Oath of Druids":
					case "Rebirth":
					case "Shahrazad":
					case "Skullclamp":
					case "Sol Ring":
					case "Strip Mine":
					case "Survival of the Fittest":
					case "Tempest Efreet":
					case "Time Vault":
					case "Time Walk":
					case "Timetwister":
					case "Timmerian Fiends":
					case "Tinker":
					case "Tolarian Academy":
					case "Treasure Cruise":
					case "Vampiric Tutor":
					case "Wheel of Fortune":
					case "Windfall":
					case "Yawgmoth's Bargain":
					case "Yawgmoth's Will":
						return false;
					default:
						return !UN_SETS.isLegal(card) && card.getSet().getBorder().equalsIgnoreCase("black");
				}
			case MODERN:
				switch (card.getName().get("en"))
				{
					//case "Ancestral Vision":
					case "Ancient Den":
					case "Birthing Pod":
					case "Blazing Shoal":
					case "Bloodbraid Elf":
					case "Chrome Mox":
					case "Cloudpost":
					case "Dark Depths":
					case "Deathrite Shaman":
					case "Dig Through Time":
					case "Dread Return":
					case "Eye of Ugin":
					case "Gitaxian Probe":
					case "Glimpse of Nature":
					case "Golgari Grave-Troll":
					case "Great Furnace":
					case "Green Sun 's Zenith":
					case "Hypergenesis":
					case "Jace, the Mind Sculptor":
					case "Mental Misstep":
					case "Ponder":
					case "Preordain":
					case "Punishing Fire":
					case "Rite of Flame":
					case "Seat of the Synod":
					case "Second Sunrise":
					case "Seething Song":
					case "Sensei's Divining Top":
					case "Skullclamp":
					case "Splinter Twin":
					case "Stoneforge Mystic":
					case "Summer Bloom":
					//case "Sword of the Meek":
					case "Treasure Cruise":
					case "Tree of Tales":
					case "Umezawa 's Jitte":
					case "Vault of Whispers":
						return false;
					default:
						return card.getSet().compareTo(MySQL.getSet("8ED")) >= 0;
				}
			case COMMANDER:
				if (card.getType() == CardType.CONSPIRACY)
					return false;
				switch (card.getName().get("en"))
				{
					case "Ancestral Recall":
					case "Balance":
					case "Biorhythm":
					case "Black Lotus":
					case "Brago's Favor":
					case "Braids, Cabal Minion":
					case "Bronze Tablet":
					case "Chaos Orb":
					case "Coalition Victory":
					case "Channel":
					case "Emrakul, the Aeons Torn":
					case "Erayo, Soratami Ascendant":
					case "Falling Star":
					case "Fastbond":
					case "Gifts Ungiven":
					case "Griselbrand":
					case "Karakas":
					case "Leovold, Emissary of Trest":
					case "Library of Alexandria":
					case "Limited Resources":
					case "Mox Emerald":
					case "Mox Jet":
					case "Mox Pearl":
					case "Mox Ruby":
					case "Mox Sapphire":
					case "Painter's Servant":
					case "Panoptic Mirror":
					case "Primeval Titan":
					case "Prophet of Kruphix":
					case "Rofellos, Llanowar Emissary":
					case "Shahrazad":
					case "Sundering Titan":
					case "Sway of the Stars":
					case "Sylvan Primordial":
					case "Time Vault":
					case "Time Walk":
					case "Tinker":
					case "Tolarian Academy":
					case "Trade Secrets":
					case "Upheaval":
					case "Worldfire":
					case "Yawgmoth's Bargain":
						return false;
					default:
						return card.getSet().getBorder().equalsIgnoreCase("black") || card.getSet().getBorder().equalsIgnoreCase("white");
				}
			case UN_SETS:
				return card.getSet().getType().equals("un");
		}

		if (name().contains("BLOCK"))
		{
			switch (card.getName().get("en"))
			{
				case "Intangible Virtue":
				case "Lingering Souls":
				case "AEther Vial":
				case "Ancient Den":
				case "Arcbound Ravager":
				case "Darksteel Citadel":
				case "Disciple of the Vault":
				case "Great Furnace":
				case "Seat of the Synod":
				case "Tree of Tales":
				case "Vault of Whispers":
				case "Skullclamp":
				case "Lin Sivvi, Defiant Hero":
				case "Rishadan Port":
				case "Gaea's Cradle":
				case "Memory Jar":
				case "Serra's Sanctum":
				case "Time Spiral":
				case "Tolarian Academy":
				case "Voltaic Key":
				case "Windfall":
				case "Cursed Scroll":
				case "Squandered Resources":
				case "Amulet of Quoz":
				case "Thawing Glaciers":
				case "Zuran Orb":
					return false;
			}
			List<Set> matching =
					MySQL.getAllSets().stream().filter(set -> set.getBlock() != null && set.getBlock().equalsIgnoreCase(name().replace("_BLOCK", "").replace("_", " ")))
							.collect(Collectors.toList());
			for (Set set : matching)
			{
				if (set.getCards().stream().anyMatch(c -> c.getName().get("en").equals(card.getName().get("en"))))
				{
					legals.add(card.getName().get("en"));
					return true;
				}
			}
			return false;
		}

		return true;
	}

	public boolean isRestricted(Card card)
	{
		if (this != VINTAGE)
			return false;
		switch (card.getName().get("en"))
		{
			case "Ancestral Recall":
			case "Balance":
			case "Black Lotus":
			case "Brainstorm":
			case "Chalice of the Void":
			case "Channel":
			case "Demonic Consultation":
			case "Demonic Tutor":
			case "Dig Through Time":
			case "Fastbond":
			case "Flash":
			case "Gitaxian Probe":
			case "Gush":
			case "Imperial Seal":
			case "Library of Alexandria":
			case "Lion's Eye Diamond":
			case "Lodestone Golem":
			case "Lotus Petal":
			case "Mana Crypt":
			case "Mana Vault":
			case "Memory Jar":
			case "Merchant Scroll":
			case "Mind's Desire":
			case "Monastery Mentor":
			case "Mox Emerald":
			case "Mox Jet":
			case "Mox Pearl":
			case "Mox Ruby":
			case "Mox Sapphire":
			case "Mystical Tutor":
			case "Necropotence":
			case "Ponder":
			case "Sol Ring":
			case "Strip Mine":
			case "Thorn of Amethyst":
			case "Time Vault":
			case "Time Walk":
			case "Timetwister":
			case "Tinker":
			case "Tolarian Academy":
			case "Treasure Cruise":
			case "Trinisphere":
			case "Vampiric Tutor":
			case "Wheel of Fortune":
			case "Windfall":
			case "Yawgmoth's Will":
				return true;
			default:
				return false;
		}
	}

	public enum Type
	{
		LEGAL, RESTRICTED, BANNED
	}
}
