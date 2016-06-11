package fr.galaxyoyo.gatherplaying;

import com.google.common.base.Joiner;
import fr.galaxyoyo.gatherplaying.client.I18n;
import java8.util.stream.Collectors;
import java8.util.stream.RefStreams;
import javafx.beans.binding.StringBinding;

import java.util.List;

import static fr.galaxyoyo.gatherplaying.CardType.*;

public enum Token
{
	// Eternal Masters (EMA)
	SPIRIT_18(CREATURE_TOKEN, "EMA", "1", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Spirit"), "413714"),
	SOLDIER_21(CREATURE_TOKEN, "EMA", "2", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit"), "413566"),
	SPIRIT_19(CREATURE_TOKEN, "EMA", "3", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit"), "413553"),
	WALL(CREATURE_TOKEN, "EMA", "4", "Defender", "Défenseur", 5, 5, ManaColor.BLUE, SubType.valueOf("Spirit"), "413617"),
	SERF(CREATURE_TOKEN, "EMA", "5", 0, 1, ManaColor.BLACK, SubType.valueOf("Serf"), "413647"),
	ZOMBIE_28(CREATURE_TOKEN, "EMA", "6", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie"), "413658"),
	CARNIVORE(CREATURE_TOKEN, "EMA", "7", 3, 1, ManaColor.RED, SubType.valueOf("Beast"), "413693"),
	DRAGON_EGG(CREATURE_TOKEN, "EMA", "8", "Flying\n{R}: This creature gets +1/+0 until end of turn.", "Vol\n{R} : Cette créature gagne +1/+0 jusqu'à la fin du tour.",
			2, 2, ManaColor.RED, SubType.valueOf("Dragon"), "413668"),
	ELEMENTAL_22(CREATURE_TOKEN, "EMA", "9", 1, 1, ManaColor.RED, SubType.valueOf("Elemental"), "413697"),
	GOBLIN_17(CREATURE_TOKEN, "EMA", "10", 1, 1, ManaColor.RED, SubType.valueOf("Goblin"), "413661", "413681", "413689"),
	ELEPHANT_8(CREATURE_TOKEN, "EMA", "11", 3, 3, ManaColor.GREEN, SubType.valueOf("Elephant"), "413705"),
	ELF_WARRIOR_6(CREATURE_TOKEN, "EMA", "12", 1, 1, ManaColor.GREEN, new SubType[]{SubType.valueOf("Elf"), SubType.valueOf("Warrior")}, "413715", "413718"),
	WURM_9(CREATURE_TOKEN, "EMA", "13", "", "", 6, 6, new ManaColor[]{ManaColor.BLACK, ManaColor.GREEN}, false, SubType.valueOf("Wurm"), "413724"),
	ELEMENTAL_23(CREATURE_TOKEN, "EMA", "14", "", "", 5, 5, new ManaColor[]{ManaColor.BLUE, ManaColor.RED}, false, SubType.valueOf("Elemental"), "413756"),
	GOBLIN_SOLDIER_3(CREATURE_TOKEN, "EMA", "15", "", "", 1, 1, new ManaColor[]{ManaColor.RED, ManaColor.WHITE}, false, new SubType[]{SubType.valueOf("Goblin"),
			SubType.valueOf("Soldier")}, "413745"),
	DACK_FAYDEN_2(EMBLEM, "EMA", "16", "Whenever you cast a spell that targets one or more permanents, gain control of those permanents.",
			"À chaque fois que vous lancez un sort qui cible un ou plusieurs permanents, acquérez le contrôle de ces permanents.", SubType.valueOf("Dack"), "413741"),

	// Ténèbres sur Innistrad (SOI)
	ANGEL_12(CREATURE_TOKEN, "SOI", "1", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	HUMAN_SOLDIER(CREATURE_TOKEN, "SOI", "2", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Human"), SubType.valueOf("Soldier")}),
	SPIRIT_17(CREATURE_TOKEN, "SOI", "3", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	VAMPIRE_KNIGHT(CREATURE_TOKEN, "SOI", "4", "Lifelink", "Lien de vie", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Vampire"), SubType.valueOf("Knight")}),
	ZOMBIE_27(CREATURE_TOKEN, "SOI", "5", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	DEVIL(CREATURE_TOKEN, "SOI", "6", "When this creature dies, it deals 1 damage to target creature or player.", "Quand cette créature meurt, elle inflige 1 blessure à une cible " +
			"créature ou joueur.", 2, 2, ManaColor.BLACK, SubType.valueOf("Devil")),
	INSECT_6(CREATURE_TOKEN, "SOI", "7", 1, 1, ManaColor.GREEN, SubType.valueOf("Insect")),
	OOZE_7(CREATURE_TOKEN, "SOI", "8", 3, 3, ManaColor.GREEN, SubType.valueOf("Ooze")),
	WOLF_14(CREATURE_TOKEN, "SOI", "9", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	HUMAN_CLERIC(CREATURE_TOKEN, "SOI", "10", "", "", 1, 1, new ManaColor[]{ManaColor.WHITE, ManaColor.BLACK}, new SubType[]{SubType.valueOf("Human"), SubType.valueOf("Cleric")}),
	CLUE_1(ARTIFACT_TOKEN, "SOI", "11", "{2}, Sacrifice this artifact: Draw a card.", "{2}, sacrifiez cet artefact : piochez une carte.", SubType.valueOf("Clue")),
	CLUE_2(ARTIFACT_TOKEN, "SOI", "12", "{2}, Sacrifice this artifact: Draw a card.", "{2}, sacrifiez cet artefact : piochez une carte.", SubType.valueOf("Clue")),
	CLUE_3(ARTIFACT_TOKEN, "SOI", "13", "{2}, Sacrifice this artifact: Draw a card.", "{2}, sacrifiez cet artefact : piochez une carte.", SubType.valueOf("Clue")),
	CLUE_4(ARTIFACT_TOKEN, "SOI", "14", "{2}, Sacrifice this artifact: Draw a card.", "{2}, sacrifiez cet artefact : piochez une carte.", SubType.valueOf("Clue")),
	CLUE_5(ARTIFACT_TOKEN, "SOI", "15", "{2}, Sacrifice this artifact: Draw a card.", "{2}, sacrifiez cet artefact : piochez une carte.", SubType.valueOf("Clue")),
	CLUE_6(ARTIFACT_TOKEN, "SOI", "16", "{2}, Sacrifice this artifact: Draw a card.", "{2}, sacrifiez cet artefact : piochez une carte.", SubType.valueOf("Clue")),
	JACE_UNRAVELER_OF_SECRETS(EMBLEM, "SOI", "17", "Whenever an opponent casts his or her first spell each turn, counter that spell.",
			"À chaque fois qu'un adversaire lance son premier sort, contrecarrez ce sort.", SubType.valueOf("Jace")),
	ARLINN_EMBRACED_BY_THE_MOON(EMBLEM, "SOI", "18", "Creatures you control have haste and \"{T}: This creature deals damage equals to its power to target creature or player.\"",
			"Les créatures que vous contrôlez ont la célérité et « {T} : Cette créature inflige à la cible créature ou joueur un nombre de blessures égale à sa force. »", SubType
			.valueOf("Arlinn")),

	// Le serment des sentinelles (OGW)
	ELDRAZI_SCION_4(CREATURE_TOKEN, "OGW", "1", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 1, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Scion")}),
	ELDRAZI_SCION_5(CREATURE_TOKEN, "OGW", "2", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 1, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Scion")}),
	ELDRAZI_SCION_6(CREATURE_TOKEN, "OGW", "3", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 1, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Scion")}),
	ELDRAZI_SCION_7(CREATURE_TOKEN, "OGW", "4", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 1, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Scion")}),
	ELDRAZI_SCION_8(CREATURE_TOKEN, "OGW", "5", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 1, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Scion")}),
	ELDRAZI_SCION_9(CREATURE_TOKEN, "OGW", "6", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 1, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Scion")}),
	ANGEL_11(CREATURE_TOKEN, "OGW", "7", "Flying", "Vol", 3, 3, ManaColor.WHITE, SubType.valueOf("Angel"), "407535"),
	ZOMBIE_26(CREATURE_TOKEN, "OGW", "8", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	ELEMENTAL_20(CREATURE_TOKEN, "OGW", "9", "Haste", "Célérité", 2, 2, ManaColor.RED, SubType.valueOf("Elemental")),
	ELEMENTAL_21(CREATURE_TOKEN, "OGW", "10", 0, 0, ManaColor.GREEN, SubType.valueOf("Elemental")),
	PLANT_3(CREATURE_TOKEN, "OGW", "11", 0, 1, ManaColor.GREEN, SubType.valueOf("Plant")),

	// Commander 2015 (C15)
	SHAPESHIFTER_2(CREATURE_TOKEN, "C15", "1a", "Changelin", "Changelin", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Shapeshifter")),
	ANGEL_10(CREATURE_TOKEN, "C15", "3a", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	SPIRIT_15(CREATURE_TOKEN, "C15", "3b", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	CAT_4(CREATURE_TOKEN, "C15", "5a", 2, 2, ManaColor.WHITE, SubType.valueOf("Cat")),
	SPIRIT_16(CREATURE_TOKEN, "C15", "5b", "This creature's power and toughness are each equal to the number of experience counter you have.",
			"La force et l'endurance de cette créature sont égales au nombre de marqueurs « expérience » que vous avez.", 0, 0, new ManaColor[]{ManaColor.WHITE, ManaColor.BLACK},
			false, SubType.valueOf("Spirit")),
	KNIGHT_4(CREATURE_TOKEN, "C15", "7a", "First Strike", "Initiative", 2, 2, ManaColor.WHITE, SubType.valueOf("Knight")),
	ELEMENTAL_SHAMAN_2(CREATURE_TOKEN, "C15", "7b", 3, 1, ManaColor.RED, new SubType[]{SubType.valueOf("Elemental"), SubType.valueOf("Shaman")}),
	KNIGHT_5(CREATURE_TOKEN, "C15", "8a", "Vigilance", "Vigilance", 2, 2, ManaColor.WHITE, SubType.valueOf("Knight")),
	GOLD_2(ARTIFACT_TOKEN, "C15", "9b", "Sacrifice this artifact: Add one mana of any color to your mana pool.",
			"Sacrifiez cet artefact : Ajoutez un mana de la couleur de votre choix à votre réserve."),
	DRAKE_2(CREATURE_TOKEN, "C15", "10a", "Flying", "Vol", 2, 2, ManaColor.BLUE, SubType.valueOf("Drake")),
	GERM_4(CREATURE_TOKEN, "C15", "12a", 0, 0, ManaColor.BLACK, SubType.valueOf("Germ")),
	FROG_LIZARD_2(CREATURE_TOKEN, "C15", "12b", 3, 3, ManaColor.RED, new SubType[]{SubType.valueOf("Frog"), SubType.valueOf("Lizard")}),
	ZOMBIE_25(CREATURE_TOKEN, "C15", "13a", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	DRAGON_12(CREATURE_TOKEN, "C15", "17a", "Flying", "Vol", 5, 5, ManaColor.RED, SubType.valueOf("Dragon")),
	LIGHTNING_RAGER(CREATURE_TOKEN, "C15", "18a", "Trample, haste\nAt the beginning of the end step, sacrifice this creature.",
			"Piétinement, célérité\nAu début de l'étape de fin, sacrifiez cette créature.", 5, 1, ManaColor.RED, SubType.valueOf("Elemental")),
	BEAR_4(CREATURE_TOKEN, "C15", "19a", 2, 2, ManaColor.GREEN, SubType.valueOf("Bear")),
	BEAST_16(CREATURE_TOKEN, "C15", "20a", 4, 4, ManaColor.GREEN, SubType.valueOf("Beast")),
	SNAKE_5(CREATURE_TOKEN, "C15", "20b", 1, 1, ManaColor.GREEN, SubType.valueOf("Snake")),
	ELEPHANT_7(CREATURE_TOKEN, "C15", "21a", 3, 3, ManaColor.GREEN, SubType.valueOf("Elephant")),
	SAPROLING_10(CREATURE_TOKEN, "C15", "22a", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling")),
	SPIDER_5(CREATURE_TOKEN, "C15", "25a", "Reach", "Portée", 1, 2, ManaColor.GREEN, SubType.valueOf("Spider")),
	WOLF_13(CREATURE_TOKEN, "C15", "25b", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),

	// Bataille de Zendikar (BFZ)
	ELDRAZI(CREATURE_TOKEN, "BFZ", "1", 10, 10, ManaColor.COLORLESS, SubType.valueOf("Eldrazi")),
	ELDRAZI_SCION_1(CREATURE_TOKEN, "BFZ", "2", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 1, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Scion")}),
	ELDRAZI_SCION_2(CREATURE_TOKEN, "BFZ", "3", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 1, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Scion")}),
	ELDRAZI_SCION_3(CREATURE_TOKEN, "BFZ", "4", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 1, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Scion")}),
	KNIGHT_ALLY(CREATURE_TOKEN, "BFZ", "5", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Knight"), SubType.valueOf("Ally")}),
	KOR_ALLY(CREATURE_TOKEN, "BFZ", "6", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Kor"), SubType.valueOf("Ally")}),
	OCTOPUS(CREATURE_TOKEN, "BFZ", "7", 8, 8, ManaColor.BLUE, SubType.valueOf("Octopus")),
	DRAGON_11(CREATURE_TOKEN, "BFZ", "8", "Flying", "Vol", 5, 5, ManaColor.RED, SubType.valueOf("Dragon")),
	ELEMENTAL_19(CREATURE_TOKEN, "BFZ", "9", "Trample, Haste", "Piétinement, célérité", 3, 1, ManaColor.RED, SubType.valueOf("Elemental")),
	PLANT_2(CREATURE_TOKEN, "BFZ", "10", 1, 1, ManaColor.GREEN, SubType.valueOf("Plant")),
	ELEMENTAL_18(CREATURE_TOKEN, "BFZ", "11", "", "", 5, 5, new ManaColor[]{ManaColor.RED, ManaColor.GREEN}, false, SubType.valueOf("Elemental")),
	GIDEON_ALLY_OF_ZENDIKAR(EMBLEM, "BFZ", "12", "Creatures you control gain +1/+1.", "Les créatures que vous contrôlez gagnent +1/+1.", SubType.valueOf("Gideon")),
	OB_NIXILIS_REIGNITED(EMBLEM, "BFZ", "13", "Whenever a player draws a card, you lose 2 life.", "À chaque fois qu'un joueur pioche une carte, vous perdez 2 points de vie",
			SubType.valueOf("Nixilis")),
	KIORA_MASTER_OF_THE_DEPTHS(EMBLEM, "BFZ", "14", "Whenever a creature enters the battlefield under your control, you may have it fight target creature.",
			"À chaque fois qu'une créature arrive sur le champ de bataille sous votre contrôle, vous pouvez faire qu'elle se batte contre une créature ciblée.",
			SubType.valueOf("Kiora")),

	//Magic Origines (ORI)
	ANGEL_9(CREATURE_TOKEN, "ORI", "1", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	KNIGHT_3(CREATURE_TOKEN, "ORI", "2", "Vigilance", "Vigilance", 2, 2, ManaColor.WHITE, SubType.valueOf("Knight")),
	SOLDIER_20(CREATURE_TOKEN, "ORI", "3", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	DEMON_7(CREATURE_TOKEN, "ORI", "4", "Flying", "Vol", 5, 5, ManaColor.BLACK, SubType.valueOf("Demon")),
	ZOMBIE_24(CREATURE_TOKEN, "ORI", "5", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	GOBLIN_16(CREATURE_TOKEN, "ORI", "6", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	ASHAYA(CREATURE_TOKEN, "ORI", "7", "", "", 4, 4, ManaColor.GREEN, true, SubType.valueOf("Elemental")),
	ELEMENTAL_17(CREATURE_TOKEN, "ORI", "8", 2, 2, ManaColor.GREEN, SubType.valueOf("Elemental")),
	ELF_WARRIOR_5(CREATURE_TOKEN, "ORI", "9", 1, 1, ManaColor.GREEN, new SubType[]{SubType.valueOf("Elf"), SubType.valueOf("Warrior")}),
	THOPTER_3(CREATURE_ARTIFACT_TOKEN, "ORI", "10", "Flying", "Vol", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Thopter")),
	THOPTER_4(CREATURE_ARTIFACT_TOKEN, "ORI", "11", "Flying", "Vol", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Thopter")),
	JACE_TELEPATH_UNBOUND(EMBLEM, "ORI", "12", "Whenever you cast a spell, target opponent puts the top five cards of his or her library into his or her graveyard.",
			"À chaque fois que vous lancez un sort, l'adversaire ciblé met les cinq cartes du dessus de sa bibliothèque dans son cimetière.", SubType.valueOf("Jace")),
	LILIANA_DEFIANT_NECROMANCER(EMBLEM, "ORI", "13", "Whenever a creature dies, return it to the battlefield under your control at the beginning of the next end step.",
			"À chaque fois qu'une créature meurt, renvoyez-la sur le champ de bataille, sous votre contrôle,au début de la prochaine étape de fin.", SubType.valueOf("Liliana")),
	CHANDRA_ROARING_FLAME(EMBLEM, "ORI", "14", "At the beginning of your upkeep, this emblem deals 3 damage to you.",
			"Au début de votre entretien, cet emblème vous inflige 3 blessures.", SubType.valueOf("Chandra")),

	//Modern Masters 2015 (MM2)
	ELDRAZI_SPAWN_4(CREATURE_TOKEN, "MM2", "1", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 0, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Spawn")}),
	ELDRAZI_SPAWN_5(CREATURE_TOKEN, "MM2", "2", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 0, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Spawn")}),
	ELDRAZI_SPAWN_6(CREATURE_TOKEN, "MM2", "3", "Sacrifice this creature:\nAdd {C} to your mana pool", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve", 0, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Spawn")}),
	SOLDIER_19(CREATURE_TOKEN, "MM2", "4", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	SPIRIT_14(CREATURE_TOKEN, "MM2", "5", "Flying", "Vl", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	FAERIE_ROGUE_4(CREATURE_TOKEN, "MM2", "6", "Flying", "Vol", 1, 1, ManaColor.BLACK, new SubType[]{SubType.valueOf("Faerie"), SubType.valueOf("Rogue")}),
	GERM_3(CREATURE_TOKEN, "MM2", "7", 0, 0, ManaColor.BLACK, SubType.valueOf("Germ")),
	THRULL(CREATURE_TOKEN, "MM2", "8", 1, 1, ManaColor.BLACK, SubType.valueOf("Thrull")),
	ELEPHANT_6(CREATURE_TOKEN, "MM2", "9", 3, 3, ManaColor.GREEN, SubType.valueOf("Elephant")),
	INSECT_5(CREATURE_TOKEN, "MM2", "10", 1, 1, ManaColor.GREEN, SubType.valueOf("Insect")),
	SAPROLING_9(CREATURE_TOKEN, "MM2", "11", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling")),
	SNAKE_4(CREATURE_TOKEN, "MM2", "12", 1, 1, ManaColor.BLACK, SubType.valueOf("Snake")),
	WOLF_12(CREATURE_TOKEN, "MM2", "13", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	WORM_3(CREATURE_TOKEN, "MM2", "14", "", "", 1, 1, new ManaColor[]{ManaColor.BLACK, ManaColor.GREEN}, false, SubType.valueOf("Worm")),
	GOLEM_5(CREATURE_ARTIFACT_TOKEN, "MM2", "15", 3, 3, ManaColor.COLORLESS, SubType.valueOf("Golem")),
	MYR_5(CREATURE_ARTIFACT_TOKEN, "MM2", "16", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Myr")),

	// Dragons de Tarkir (DTK)
	WARRIOR_5(CREATURE_TOKEN, "DTK", "1", 1, 1, ManaColor.WHITE, SubType.valueOf("Warrior")),
	DJINN_MONK(CREATURE_TOKEN, "DTK", "2", "Flying", "Vol", 1, 1, ManaColor.BLUE, new SubType[]{SubType.valueOf("Djinn"), SubType.valueOf("Monk")}),
	ZOMBIE_23(CREATURE_TOKEN, "DTK", "3", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	ZOMBIE_HORROR(CREATURE_TOKEN, "DTK", "4", 0, 0, ManaColor.BLACK, new SubType[]{SubType.valueOf("Zombie"), SubType.valueOf("Horror")}),
	DRAGON_10(CREATURE_TOKEN, "DTK", "5", "Flying", "Vol", 5, 5, ManaColor.RED, SubType.valueOf("Dragon")),
	GOBLIN_15(CREATURE_TOKEN, "DTK", "6", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	MORPH_2(CREATURE, "DTK", "7", "#_You can cover a face-down creature with this reminder card.\n A card with morph can be turned face up any time for its morph cost._#",
			"#_Vous pouvez couvrir une créature face cachée avec cette carte de rappel.\nUne carte avec la mue peut être retournée face visible à tout moment pour son coût de " +
					"mue._#", 2, 2, ManaColor.COLORLESS),
	NARSET_TRANSCENDANT(EMBLEM, "DTK", "8", "Your opponents can't cast noncreature spells.", "Vos adversaires ne peuvent pas lancer de sorts non-créature.",
			SubType.valueOf("Narset")),

	//Destin reforgé (FRF)
	MONK_1(CREATURE_TOKEN, "FRF", "1", "Prowess", "Prouesse", 1, 1, ManaColor.WHITE, SubType.valueOf("Monk")),
	SPIRIT_13(CREATURE_TOKEN, "FRF", "2", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	WARRIOR_4(CREATURE_TOKEN, "FRF", "3", 2, 1, ManaColor.BLACK, SubType.valueOf("Warrior")),
	MANIFEST(CREATURE, "FRF", "4",
			"#_You can cover a face-down manifested creature with this reminder card.\nA manifested creature card can be turned face up any time for its mana cost. A face-down " +
					"card can also be turned face up for its morph cost._#",
			"#_Vous pouvez couvrir une créature manifestée face cachée avec cette carte de rappel.\nUne carte de créature manifestée peut être retournée face visible à" +
					" tout moment pour son coût de mana. Une face cachée peut aussi être retournée face visible pour son coût de mue._#", 2, 2, ManaColor.COLORLESS),
	MONK_2(CREATURE_TOKEN, "FRF", "5", "Prowess", "Prouesse", 1, 1, ManaColor.WHITE, SubType.valueOf("Monk")),

	//Commander 2014 (C14)
	ANGEL_8(CREATURE_TOKEN, "C14", "1a", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	KOR_SOLDIER_2(CREATURE_TOKEN, "C14", "2a", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Kor"), SubType.valueOf("Soldier")}),
	PEGASUS(CREATURE_TOKEN, "C14", "2b", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Pegasus")),
	SOLDIER_18(CREATURE_TOKEN, "C14", "3a", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	SPIRIT_12(CREATURE_TOKEN, "C14", "3b", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	FISH(CREATURE_TOKEN, "C14", "4a",
			"When this creature dies, put a 6/6 blue Whale creature token onto the battlefield with \"When this creature dies, put a 9/9 blue Kraken creature token onto the " +
					"battlefield\"",
			"Quand cette créature meurt, mettez sur le champ de bataille un jeton de créature 6/6 bleue Baleine avec « Quand cette créature meurt, mettez sur le" +
					" champ de bataille un jeton de créature 9/9 bleue Kraken »", 3, 3, ManaColor.BLUE, SubType.valueOf("Fish")),
	KRAKEN_2(CREATURE_TOKEN, "C14", "5a", 9, 9, ManaColor.BLUE, SubType.valueOf("Kraken")),
	WHALE(CREATURE_TOKEN, "C14", "6a", "When this creature dies, put a 9/9 blue Kraken creature token on the battlefield.",
			"Quand cette créature meurt, mettez sur le champ de bataille un jeton de créature 9/9 bleue Kraken.", 6, 6, ManaColor.BLUE, SubType.valueOf("Fish")),
	DEMON_5(CREATURE_TOKEN, "C14", "7a", "Flying", "Vol", 0, 0, ManaColor.BLACK, SubType.valueOf("Demon")),
	DEMON_6(CREATURE_TOKEN, "C14", "8a", "Flying", "Vol", 5, 5, ManaColor.BLACK, SubType.valueOf("Demon")),
	GERM_2(CREATURE_TOKEN, "C14", "9a", 0, 0, ManaColor.BLACK, SubType.valueOf("Germ")),
	HORROR_3(CREATURE_TOKEN, "C14", "10a", 0, 0, ManaColor.BLACK, SubType.valueOf("Horror")),
	GOBLIN_14(CREATURE_TOKEN, "C14", "11a", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	APE(CREATURE_TOKEN, "C14", "12a", 3, 3, ManaColor.GREEN, SubType.valueOf("Ape")),
	ELEMENTAL_16(CREATURE_TOKEN, "C14", "13a", 5, 3, ManaColor.GREEN, SubType.valueOf("Elemental")),
	BEAST_15(CREATURE_TOKEN, "C14", "13b", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	ELEPHANT_5(CREATURE_TOKEN, "C14", "14a", 3, 3, ManaColor.GREEN, SubType.valueOf("Elephant")),
	ELF_WARRIOR_4(CREATURE_TOKEN, "C14", "14b", 1, 1, ManaColor.GREEN, new SubType[]{SubType.valueOf("Elf"), SubType.valueOf("Warrior")}),
	ELF_DRUID(CREATURE_TOKEN, "C14", "15a", "{T} : Add {G} to your mana pool", "{T} : Ajoutez {G} à votre réserve", 1, 1, ManaColor.GREEN, new SubType[]{SubType.valueOf("Elf"),
			SubType.valueOf("Druid")}),
	TREEFOLK(CREATURE_TOKEN, "C14", "16a", 0, 0, ManaColor.GREEN, SubType.valueOf("Treefolk")),
	WOLF_11(CREATURE_TOKEN, "C14", "16b", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	GARGOYLE_2(CREATURE_ARTIFACT_TOKEN, "C14", "17a", "Flying", "Vol", 3, 4, ManaColor.COLORLESS, SubType.valueOf("Gargoyle")),
	MYR_4(CREATURE_ARTIFACT_TOKEN, "C14", "18a", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Myr")),
	PENTAVITE(CREATURE_ARTIFACT_TOKEN, "C14", "18b", "Flying", "Vol", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Pentavite")),
	STONEFORGED_BLADE(ARTIFACT_TOKEN, "C14", "19a", "Indestructible\nEquipped creature gets +5/+5 and has double strike.\nEquip {0}",
			"Indestructible\nLa créature équipée gagne +5/+5 et a la double initiative.\nÉquipement {0}", SubType.valueOf("Equipment")),
	WURM_8(CREATURE_ARTIFACT_TOKEN, "C14", "20a", "Deathtouch", "Contact mortel", 3, 3, ManaColor.COLORLESS, SubType.valueOf("Wurm")),
	WURM_7(CREATURE_ARTIFACT_TOKEN, "C14", "21a", "Life Link", "Lien de vie", 3, 3, ManaColor.COLORLESS, SubType.valueOf("Wurm")),
	GOAT_4(CREATURE_TOKEN, "C14", "21b", 0, 1, ManaColor.WHITE, SubType.valueOf("Goat")),
	TEFERI(EMBLEM, "C14", "22a", "You may activate loyalty abilities of planewalkers you control on any player's turn any time you could cast an instant.",
			"Vous pouvez activer les capacités de loyauté des planewalkers que vous contrôlez pendant le tour de n'importe quel joueur à tout moment où vous pourriez " +
					"lancer un éphémère.", SubType.valueOf("Teferi")),
	ZOMBIE_22(CREATURE_TOKEN, "C14", "22b", 2, 2, ManaColor.BLUE, SubType.valueOf("Zombie")),
	NIXILIS(EMBLEM, "C14", "23a", "{1}{B}, Sacrifice a creature: You gain X life and draw X cards, where X is the sacrificed creature's power.",
			"{1}{B}, sacrifiez une créature : Vous gagnez X points de vie et piochez X cartes, X étant la force de la créature sacrifiée", SubType.valueOf("Nixilis")),
	ZOMBIE_21(CREATURE_TOKEN, "C14", "23b", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	DARETTI(EMBLEM, "C14", "24a",
			"Whenever an artifact is put into your graveyard from the battlefield, return that card to the battlefield at the beginning of the next end step.",
			"À chaque fois qu'un artefact est mis dans votre cimetière depuis le champ de bataille, renvoyez cette carte sur le champ de bataille au début de la prochaine" +
					" étape de fin.", SubType.valueOf("Daretti")),
	TUKTUK_THE_RETURNED_2(CREATURE_ARTIFACT_TOKEN, "C14", "24b", "Tuktuk the Returned is legendary.", "Tuktuk le Reparu est légendaire.", 5, 5, ManaColor.COLORLESS,
			new SubType[]{SubType.valueOf("Goblin"), SubType.valueOf("Golem")}),

	// Les Khans de Tarkir (KTK)
	BIRD_11(CREATURE_TOKEN, "KTK", "1", "Flying", "Vol", 3, 4, ManaColor.WHITE, SubType.valueOf("Bird")),
	SPIRIT_11(CREATURE_TOKEN, "KTK", "2", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	WARRIOR_1(CREATURE_TOKEN, "KTK", "3", 1, 1, ManaColor.WHITE, SubType.valueOf("Warrior")),
	WARRIOR_2(CREATURE_TOKEN, "KTK", "4", 1, 1, ManaColor.WHITE, SubType.valueOf("Warrior")),
	VAMPIRE_4(CREATURE_TOKEN, "KTK", "5", "Flying", "Vol", 2, 2, ManaColor.BLACK, SubType.valueOf("Vampire")),
	ZOMBIE_20(CREATURE_TOKEN, "KTK", "6", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	GOBLIN_13(CREATURE_TOKEN, "KTK", "7", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	BEAR_3(CREATURE_TOKEN, "KTK", "8", 4, 4, ManaColor.GREEN, SubType.valueOf("Bear")),
	SNAKE_3(CREATURE_TOKEN, "KTK", "9", 1, 1, ManaColor.GREEN, SubType.valueOf("Snake")),
	SPIRIT_WARRIOR(CREATURE_TOKEN, "KTK", "10", "", "", 0, 0, new ManaColor[]{ManaColor.BLACK, ManaColor.GREEN}, false, new SubType[]{SubType.valueOf("Spirit"), SubType.valueOf
			("Warrior")}),
	MORPH_1(CREATURE, "KTK", "11", "#_You can cover a face-down creature with this reminder card.\nA card with morph can be turned up any this for its morph cost._#",
			"#_Vous pouvez couvrir une créature face cachée avec cette carte de rappel .\nUne carte avec la mue peut être retournée face visible à tout moment pour son " +
					"coût de mue._#", 2, 2, ManaColor.COLORLESS),
	SARKHAN_THE_DRAGONSPEAKER(EMBLEM, "KTK", "12", "At the beginning of your draw step, draw two additional cards.\nAt the beginning of your end step, discard your hand.",
			"Au début de votre étape de pioche, piochez 2 cartes supplémentaires.\nAu début de votre étape de fin, défaussez-vous de votre main.", SubType.valueOf("Sarkhan")),
	SORIN_SOLEMN_VISITOR(EMBLEM, "KTK", "13", "At the beginning of each opponent's upkeep, that player sacrifices a creature.",
			"Au début de l'entretien de chaque adversaire, ce joueur sacrifie une créature.", SubType.valueOf("Sorin")),
	//WARRIOR_3(CREATURE_TOKEN, "Guerrier", "KTK", "14", 1, 1, ManaColor.WHITE, SubType.valueOf("Warrior")),

	// Magic 2015 (M15)
	SLIVER_4(CREATURE_TOKEN, "M15", "1", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Sliver")),
	SOLDIER_17(CREATURE_TOKEN, "M15", "2", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	SPIRIT_10(CREATURE_TOKEN, "M15", "3", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	SQUID_2(CREATURE_TOKEN, "M15", "4", "Islandwalk", "Traversée des îles", 1, 1, ManaColor.BLUE, SubType.valueOf("Squid")),
	BEAST_13(CREATURE_TOKEN, "M15", "5", "Deathtouch", "Contact mortel", 3, 3, ManaColor.BLACK, SubType.valueOf("Beast")),
	ZOMBIE_19(CREATURE_TOKEN, "M15", "6", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	DRAGON_9(CREATURE_TOKEN, "M15", "7", "Flying\n{R}: This creature gets +1/+0 until end of turn.", "Vol\n{R} : Cette créature gagne +1/+0 jusqu'à la fin du tour", 2, 2,
			ManaColor.RED, SubType.valueOf("Dragon")),
	GOBLIN_12(CREATURE_TOKEN, "M15", "8", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	BEAST_14(CREATURE_TOKEN, "M15", "9", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	INSECT_4(CREATURE_TOKEN, "M15", "10", "Flying, deathtouch", "Vol, contact mortel", 1, 1, ManaColor.GREEN, SubType.valueOf("Insect")),
	TREEFOLK_WARRIOR(CREATURE_TOKEN, "M15", "11", "This creature's power and toughness are each equal to the number of Forests you control.",
			"La force et l'endurance de cette créature sont chacune égales au nombre de forêts que vous contrôlez.", 0, 0, ManaColor.GREEN, new SubType[]{SubType.valueOf("Treefolk"),
			SubType.valueOf("Warrior")}),
	LAND_MINE(ARTIFACT_TOKEN, "M15", "12", "{R}, Sacrifice this artifact: This artifact deals 2 damage to target attacking creature without flying.",
			"{R}, sacrifiez cet artefact : Cet artefact inflige 2 blessures à une créature attaquante sans le vol ciblée"),
	AJANI_STEADFAST(EMBLEM, "M15", "13", "If a source would deal damage to you or a planewalker you control, prevent all but 1 of that damage.",
			"Si une source devait vous infliger des blessures à vous ou à un planeswalker que vous contrôlez, prévenez toutes ces blessures sauf 1.", SubType.valueOf("Ajani")),
	GARRUK_APEX_PREDATOR(EMBLEM, "M15", "14", "Whenever a creature attacks you, it gets +5/+5 and gains trample until end of turn.",
			"À chaque fois qu'une créature vous attaque, elle gagne +5/+5 et acquiert le piétinement jusqu'à la fin du tour.", SubType.valueOf("Garruk")),
	//WOLF_11(CREATURE_TOKEN, "Loup", "M15", "15", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	//SQUID_1(CREATURE_TOKEN, "Calamar", "M15", "16", "Islandwalk", "Traversée des îles", 1, 1, ManaColor.BLUE, SubType.valueOf("Squid")),

	// Conspiracy (CNS)
	SPIRIT_9(CREATURE_TOKEN, "CNS", "1", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	DEMON_4(CREATURE_TOKEN, "CNS", "2", "Flying", "Vol", 0, 0, ManaColor.BLACK, SubType.valueOf("Demon")),
	ZOMBIE_18(CREATURE_TOKEN, "CNS", "3", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	OGRE_2(CREATURE_TOKEN, "CNS", "4", 4, 4, ManaColor.RED, SubType.valueOf("Ogre")),
	ELEPHANT_4(CREATURE_TOKEN, "CNS", "5", 3, 3, ManaColor.GREEN, SubType.valueOf("Elephant")),
	SQUIRREL_3(CREATURE_TOKEN, "CNS", "6", 1, 1, ManaColor.GREEN, SubType.valueOf("Squirrel")),
	WOLF_10(CREATURE_TOKEN, "CNS", "7", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	CONSTRUCT_2(CREATURE_ARTIFACT_TOKEN, "CNS", "8", "Defender", "Défenseur", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Construct")),
	DACK_FAYDEN_1(EMBLEM, "CNS", "9", "Whenever you cast a spell that targets one or more permanents, gain control of those permanents.",
			"À chaque fois que vous lancez un sort qui cible un ou plusieurs permanents, acquérez le contrôle de ces permanents.", SubType.valueOf("Dack")),

	// Modern Event Deck (MD1)

	// Incursion dans Nyx (JOU)
	SPHINX(CREATURE_TOKEN, "JOU", "1", "Flying", "Vol", 1, 1, ManaColor.BLUE, SubType.valueOf("Sphinx")),
	ZOMBIE_17(CREATURE_TOKEN, "JOU", "2", 0, 0, ManaColor.BLACK, SubType.valueOf("Zombie")),
	MINOTAUR(CREATURE_TOKEN, "JOU", "3", 2, 3, ManaColor.RED, SubType.valueOf("Minotaur")),
	HYDRA(CREATURE_TOKEN, "JOU", "4", 0, 0, ManaColor.GREEN, SubType.valueOf("Hydra")),
	SPIDER_4(ENCHANTMENT_CREATURE_TOKEN, "JOU", "5", "Reach", "Portée", 1, 3, ManaColor.GREEN, SubType.valueOf("Spider")),
	SNAKE_2(ENCHANTMENT_CREATURE_TOKEN, "JOU", "6", "Deathtouch", "Contact mortel", 1, 1, new ManaColor[]{ManaColor.BLACK, ManaColor.GREEN}, false, SubType.valueOf("Snake")),

	// Créations divines (BNG)
	BIRD_10(CREATURE_TOKEN, "BNG", "1", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Bird")),
	CAT_SOLDIER(CREATURE_TOKEN, "BNG", "2", "Vigilance", "Vigilance", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Cat"), SubType.valueOf("Soldier")}),
	SOLDIER_16(ENCHANTMENT_CREATURE_TOKEN, "BNG", "3", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	BIRD_9(ENCHANTMENT_CREATURE_TOKEN, "BNG", "4", "Flying", "Vol", 2, 2, ManaColor.BLUE, SubType.valueOf("Bird")),
	KRAKEN_1(CREATURE_TOKEN, "BNG", "5", 9, 9, ManaColor.WHITE, SubType.valueOf("Kraken")),
	ZOMBIE_16(ENCHANTMENT_CREATURE_TOKEN, "BNG", "6", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	ELEMENTAL_15(ENCHANTMENT_CREATURE_TOKEN, "BNG", "7", 3, 1, ManaColor.RED, SubType.valueOf("Elemental")),
	CENTAUR_3(ENCHANTMENT_CREATURE_TOKEN, "BNG", "8", 3, 3, ManaColor.GREEN, SubType.valueOf("Centaur")),
	WOLF_9(ENCHANTMENT_CREATURE_TOKEN, "BNG", "9", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	GOLD_1(ARTIFACT_TOKEN, "BNG", "10", "Sacrifice this artifact: Add one mana of any color to your mana pool.",
			"Sacrifiez cet artefact : Ajoutez un mana de la couleur de votre choix à votre réserve."),
	KIORA_THE_CRASHING_WAVE(EMBLEM, "BNG", "11", "At the beginning of your end step, put a 9/9 blue Kraken creature token onto the battlefield.",
			"Au début de votre étape de fin, mettez sur le champ de bataille un jeton de créature 9/9 bleue Kraken.", SubType.valueOf("Kiora")),

	// Theros (THS)
	CLERIC_2(ENCHANTMENT_CREATURE_TOKEN, "THS", "1", 2, 1, ManaColor.WHITE, SubType.valueOf("Cleric")),
	SOLDIER_13(CREATURE_TOKEN, "THS", "2", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	SOLDIER_14(CREATURE_TOKEN, "THS", "3", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	BIRD_8(CREATURE_TOKEN, "THS", "4", "Flying", "Vol", 2, 2, ManaColor.BLUE, SubType.valueOf("Bird")),
	ELEMENTAL_14(CREATURE_TOKEN, "THS", "5", 1, 0, ManaColor.BLUE, SubType.valueOf("Elemental")),
	HARPY(CREATURE_TOKEN, "THS", "6", "Flying", "Vol", 1, 1, ManaColor.BLACK, SubType.valueOf("Harpy")),
	SOLDIER_15(CREATURE_TOKEN, "THS", "7", 1, 1, ManaColor.RED, SubType.valueOf("Soldier")),
	BOAR(CREATURE_TOKEN, "THS", "8", 2, 2, ManaColor.GREEN, SubType.valueOf("Boar")),
	SATYR(CREATURE_TOKEN, "THS", "9", "", "", 2, 2, new ManaColor[]{ManaColor.RED, ManaColor.GREEN}, false, SubType.valueOf("Satyr")),
	GOLEM_4(ENCHANTMENT_ARTIFACT_CREATURE_TOKEN, "THS", "10", 3, 3, ManaColor.COLORLESS, SubType.valueOf("Golem")),
	ELSPETH_SUNS_CHAMPION(EMBLEM, "THS", "11", "Creatures you control get +2/+2 and have flying.", "Les créatures que vous contrôlez gagnent +2/+2 et ont le vol.",
			SubType.valueOf("Elspeth")),

	// Magic 2014 (M14)
	SLIVER_3(CREATURE_TOKEN, "M14", "1", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Sliver")),
	ANGEL_7(CREATURE_TOKEN, "M14", "2", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	CAT_3(CREATURE_TOKEN, "M14", "3", 2, 2, ManaColor.WHITE, SubType.valueOf("Cat")),
	GOAT_3(CREATURE_TOKEN, "M14", "4", 0, 1, ManaColor.WHITE, SubType.valueOf("Goat")),
	ZOMBIE_15(CREATURE_TOKEN, "M14", "5", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	DRAGON_8(CREATURE_TOKEN, "M14", "6", "Flying\n{R}: This creature gets +1/+0 until end of turn.", "Vol\n{R} : Cette créature gagne +1/+0 jusqu'à la fin du tour.", 2, 2,
			ManaColor.RED, SubType.valueOf("Dragon")),
	ELEMENTAL_12(CREATURE_TOKEN, "M14", "7", 1, 1, ManaColor.RED, SubType.valueOf("Elemental")),
	ELEMENTAL_13(CREATURE_TOKEN, "M14", "8", 1, 1, ManaColor.RED, SubType.valueOf("Elemental")),
	BEAST_12(CREATURE_TOKEN, "M14", "9", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	SAPROLING_8(CREATURE_TOKEN, "M14", "10", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling")),
	WOLF_8(CREATURE_TOKEN, "M14", "11", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	LILIANA_OF_THE_DARK_REALMS_2(EMBLEM, "M14", "12", "Swamps you control have : \"{T}: Add {B}{B}{B}{B} to your mana pool.\"",
			"Les marais que vous contrôlez ont : « {T} : Ajoutez {B}{B}{B}{B} à votre réserve. »", SubType.valueOf("Liliana")),
	GARRUK_CALLER_OF_BEASTS(EMBLEM, "M14", "13",
			"Whenever you cast a creature spell, you may search your library for a creature card, put it onto the battlefield, then shuffle your library.",
			"À chaque fois que vous lancez un sort de créature, vous pouvez chercher dans votre bibliothèque une carte de créature, la mettre sur le champ de bataille et " +
					"mélanger ensuite votre bibliothèque.", SubType.valueOf("Garruk")),
	// SLIVER_3_2(CREATURE_TOKEN, "M14", "14", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Sliver")),

	// Modern Masters (MMA)
	ELSPETH_KNIGHT_ERRANT(EMBLEM, "MMA", "1", "Artifacts, creatures, enchantments, and lands you control have indestructible.",
			"Les artefacts, créatures, enchantements et terrains que vous contrôlez ont l'indestructible.", SubType.valueOf("Elspeth")),
	WORM(CREATURE_TOKEN, "MMA", "2", "", "", 1, 1, new ManaColor[]{ManaColor.BLACK, ManaColor.GREEN}, false, SubType.valueOf("Worm")),
	FAERIE_ROGUE_3(CREATURE_TOKEN, "MMA", "3", "Flying", "Vol", 1, 1, new ManaColor[]{ManaColor.BLUE, ManaColor.BLACK}, false, new SubType[]{SubType.valueOf("Faerie"), SubType.valueOf
			("Rogue")}),
	TREEFOLK_SHAMAN_2(CREATURE_TOKEN, "MMA", "4", 2, 5, ManaColor.GREEN, new SubType[]{SubType.valueOf("Treefolk"), SubType.valueOf("Shaman")}),
	SAPROLING_7(CREATURE_TOKEN, "MMA", "5", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling")),
	ELEMENTAL_11(CREATURE_TOKEN, "MMA", "6", 4, 4, ManaColor.GREEN, SubType.valueOf("Elemental")),
	DRAGON_7(CREATURE_TOKEN, "MMA", "7", "Flying", "Vol", 4, 4, ManaColor.RED, SubType.valueOf("Dragon")),
	GOBLIN_11(CREATURE_TOKEN, "MMA", "8", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	ZOMBIE_14(CREATURE_TOKEN, "MMA", "9", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	SPIDER_3(CREATURE_TOKEN, "MMA", "10", "Reach", "Portée", 2, 4, ManaColor.BLACK, SubType.valueOf("Spider")),
	GOBLIN_ROGUE_2(CREATURE_TOKEN, "MMA", "11", 1, 1, ManaColor.BLACK, new SubType[]{SubType.valueOf("Goblin"), SubType.valueOf("Rogue")}),
	BAT(CREATURE_TOKEN, "MMA", "12", "Flying", "Vol", 1, 1, ManaColor.BLACK, SubType.valueOf("Bat")),
	ILLUSION_2(CREATURE_TOKEN, "MMA", "13", "Flying", "Vol", 1, 1, ManaColor.BLUE, SubType.valueOf("Illusion")),
	SOLDIER_12(CREATURE_TOKEN, "MMA", "15", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	KITHKIN_SOLDIER_3(CREATURE_TOKEN, "MMA", "18", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Kithkin"), SubType.valueOf("Soldier")}),
	GIANT_WARRIOR_3(CREATURE_TOKEN, "MMA", "19", 5, 5, ManaColor.WHITE, new SubType[]{SubType.valueOf("Giant"), SubType.valueOf("Warrior")}),

	// Le labyrinthe du dragon (DGM)
	ELEMENTAL_10(CREATURE_TOKEN, "DGM", "1", "This creature's power and toughness are each equal to the number of the number of creatures you control.",
			"La force et l'endurance de cette créature sont chacune égales au nombre de créature que vous contrôlez.", 0, 0, new ManaColor[]{ManaColor.GREEN, ManaColor.WHITE},
			false, SubType.valueOf("Elemental")),
	BIRD_7(CREATURE_TOKEN, "DGM", "2", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Bird")),

	// Insurrection (GTC)
	ANGEL_6(CREATURE_TOKEN, "GTC", "1", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	RAT_2(CREATURE_TOKEN, "GTC", "2", 1, 1, ManaColor.BLACK, SubType.valueOf("Rat")),
	FROG_LIZARD_1(CREATURE_TOKEN, "GTC", "3", 3, 3, ManaColor.GREEN, new SubType[]{SubType.valueOf("Frog"), SubType.valueOf("Lizard")}),
	CLERIC_1(CREATURE_TOKEN, "GTC", "4", "{3}{W}{B}{B}, {T}, Sacrifice this creature:\nReturn a card named Deathpact Angel from your graveyard to the battlefield.",
			"{3}{W}{B}{B}, {T}, sacrifiez cette créature :\nRenvoyez sur le champ de bataille une carte nommée Ange du paxte de mort depuis votre cimetière."),
	HORROR_2(CREATURE_TOKEN, "GTC", "5", "Flying", "Vol", 1, 1, new ManaColor[]{ManaColor.BLUE, ManaColor.BLACK}, false, SubType.valueOf("Horror")),
	SOLDIER_11(CREATURE_TOKEN, "GTC", "6", "", "", 1, 1, new ManaColor[]{ManaColor.RED, ManaColor.WHITE}, false, SubType.valueOf("Soldier")),
	SPIRIT_8(CREATURE_TOKEN, "GTC", "7", "Flying", "Vol", 1, 1, new ManaColor[]{ManaColor.WHITE, ManaColor.BLACK}, false, SubType.valueOf("Spirit")),
	DOMRI_RADE(EMBLEM, "GTC", "8", "Creatures you control have double strike, trample, hexproof, and haste.",
			"Les créatures que vous contrôlez ont la double initiative, le piétinement, la défense talismanique et la célérité.", SubType.valueOf("Domri")),
	SOLDIER_10(CREATURE_TOKEN, "GTC", "9", "", "", 1, 1, new ManaColor[]{ManaColor.RED, ManaColor.WHITE}, false, SubType.valueOf("Soldier")),

	// Retour sur Ravnica (RTR)
	BIRD_6(CREATURE_TOKEN, "RTR", "1", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Bird")),
	KNIGHT_2(CREATURE_TOKEN, "RTR", "2", "Vigilance", "Vigilance", 2, 2, ManaColor.WHITE, SubType.valueOf("Knight")),
	SOLDIER_9(CREATURE_TOKEN, "RTR", "3", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	ASSASSIN(CREATURE_TOKEN, "RTR", "4", "Whenever this creature deals combat damage to a player, that player loses the game.",
			"À chaque fois que cette créature inflige des blessures de combat à un joueur, ce joueur perd la partie.", 2, 2, ManaColor.WHITE, SubType.valueOf("Assassin")),
	DRAGON_6(CREATURE_TOKEN, "RTR", "5", "Flying", "Vol", 6, 6, ManaColor.RED, SubType.valueOf("Dragon")),
	GOBLIN_10(CREATURE_TOKEN, "RTR", "6", "Flying", "Vol", 6, 6, ManaColor.RED, SubType.valueOf("Goblin")),
	CENTAUR_1(ENCHANTMENT_CREATURE_TOKEN, "RTR", "7", 3, 3, ManaColor.GREEN, SubType.valueOf("Centaur")),
	OOZE_6(CREATURE_TOKEN, "RTR", "8", 0, 0, ManaColor.GREEN, SubType.valueOf("Ooze")),
	RHINO(CREATURE_TOKEN, "RTR", "9", "Trample", "Piétinement", 4, 4, ManaColor.GREEN, SubType.valueOf("Rhino")),
	SAPROLING_6(CREATURE_TOKEN, "RTR", "10", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling")),
	WURM_6(CREATURE_TOKEN, "RTR", "11", "Trample", "Piétinement", 5, 5, ManaColor.GREEN, SubType.valueOf("Wurm")),
	ELEMENTAL_9(CREATURE_TOKEN, "RTR", "12", "Vigilance", "Vigilance", 8, 8, new ManaColor[]{ManaColor.GREEN, ManaColor.WHITE}, false, SubType.valueOf("Elemental")),
	KNIGHT_1(CREATURE_TOKEN, "RTR", "13", "Vigilance", "Vigilance", 2, 2, ManaColor.WHITE, SubType.valueOf("Knight")),
	CENTAUR_2(ENCHANTMENT_CREATURE_TOKEN, "RTR", "14", 3, 3, ManaColor.GREEN, SubType.valueOf("Centaur")),

	// Magic 2013 (M13)
	CAT_2(CREATURE_TOKEN, "M13", "1", 2, 2, ManaColor.WHITE, SubType.valueOf("Cat")),
	GOAT_2(CREATURE_TOKEN, "M13", "2", 0, 1, ManaColor.WHITE, SubType.valueOf("Goat")),
	SOLDIER_8(CREATURE_TOKEN, "M13", "3", 2, 2, ManaColor.WHITE, SubType.valueOf("Soldier")),
	DRAKE_1(CREATURE_TOKEN, "M13", "4", 2, 2, ManaColor.BLUE, SubType.valueOf("Drake")),
	ZOMBIE_13(CREATURE_TOKEN, "M13", "5", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	GOBLIN_8(CREATURE_TOKEN, "M13", "6", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	HELLION_2(CREATURE_TOKEN, "M13", "7", 4, 4, ManaColor.RED, SubType.valueOf("Hellion")),
	BEAST_11(CREATURE_TOKEN, "M13", "8", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	SAPROLING_5(CREATURE_TOKEN, "M13", "9", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling")),
	WURM_5(CREATURE_TOKEN, "M13", "10", 6, 6, ManaColor.GREEN, SubType.valueOf("Wurm")),
	LILIANA_OF_THE_DARK_REALMS_1(EMBLEM, "M13", "11", "Swamps you control have : \"{T}: Add {B}{B}{B}{B} to your mana pool.\"",
			"Les marais que vous contrôlez ont : « {T} : Ajoutez {B}{B}{B}{B} à votre réserve. »", SubType.valueOf("Liliana")),
	GOBLIN_9(CREATURE_TOKEN, "M13", "12", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),

	// Avacyn Ressucité (AVR)
	ANGEL_5(CREATURE_TOKEN, "AVR", "1", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	HUMAN_4(CREATURE_TOKEN, "AVR", "2", 1, 1, ManaColor.WHITE, SubType.valueOf("Human")),
	SPIRIT_6(CREATURE_TOKEN, "AVR", "3", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	SPIRIT_7(CREATURE_TOKEN, "AVR", "4", "Flying", "Vol", 1, 1, ManaColor.BLUE, SubType.valueOf("Spirit")),
	HUMAN_5(CREATURE_TOKEN, "AVR", "5", 1, 1, ManaColor.RED, SubType.valueOf("Human")),
	ZOMBIE_12(CREATURE_TOKEN, "AVR", "6", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	DEMON_3(CREATURE_TOKEN, "AVR", "7", "Flying", "Vol", 5, 5, ManaColor.BLACK, SubType.valueOf("Demon")),
	TAMIYO_THE_MOON_SAGE(EMBLEM, "AVR", "8", "You have no maximum hand size.\nWhenever a card is put into your graveyard from anywhere, you may return it to your hand.",
			"Il n'y a pas de limite au nombre de cartes dans votre main.\nÀ chaque fois qu'une carte est mise dans votre cimetière d'où qu'elle vienne, vous pouvez la renvoyer " +
					"dans votre main.", SubType.valueOf("Tamiyo")),

	// Obscure Ascension (DKA)
	HUMAN_3(CREATURE_TOKEN, "DKA", "1", 1, 1, ManaColor.WHITE, SubType.valueOf("Human")),
	VAMPIRE_3(CREATURE_TOKEN, "DKA", "2", "Lifelink", "Lien de vie", 1, 1, ManaColor.BLACK, SubType.valueOf("Vampire")),

	// Innistrad (ISD)
	ANGEL_4(CREATURE_TOKEN, "ISD", "1", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	SPIRIT_5(CREATURE_TOKEN, "ISD", "2", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	HOMUNCULUS_2(CREATURE_TOKEN, "ISD", "3", 2, 2, ManaColor.BLUE, SubType.valueOf("Homunculus")),
	DEMON_2(CREATURE_TOKEN, "ISD", "4", "Flying", "Vol", 5, 5, ManaColor.BLACK, SubType.valueOf("Demon")),
	VAMPIRE_2(CREATURE_TOKEN, "ISD", "5", "Flying", "Vol", 1, 1, ManaColor.BLACK, SubType.valueOf("Vampire")),
	WOLF_6(CREATURE_TOKEN, "ISD", "6", "Deathtouch", "Contact mortel", 1, 1, ManaColor.BLACK, SubType.valueOf("Wolf")),
	ZOMBIE_9(CREATURE_TOKEN, "ISD", "7", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	ZOMBIE_10(CREATURE_TOKEN, "ISD", "8", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	ZOMBIE_11(CREATURE_TOKEN, "ISD", "9", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	OOZE_5(CREATURE_TOKEN, "ISD", "10", 0, 0, ManaColor.GREEN, SubType.valueOf("Ooze")),
	SPIDER_2(CREATURE_TOKEN, "ISD", "11", "Reach", "Portée", 1, 2, ManaColor.GREEN, SubType.valueOf("Spider")),
	WOLF_7(CREATURE_TOKEN, "ISD", "12", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),

	// Magic 2012 (M12)
	BIRD_5(CREATURE_TOKEN, "M12", "1", "Flying", "Vol", 3, 3, ManaColor.WHITE, SubType.valueOf("Bird")),
	SOLDIER_7(CREATURE_TOKEN, "M12", "2", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	ZOMBIE_8(CREATURE_TOKEN, "M12", "3", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	BEAST_10(CREATURE_TOKEN, "M12", "4", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	SAPROLING_4(CREATURE_TOKEN, "M12", "5", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling")),
	WURM_4(CREATURE_TOKEN, "M12", "6", 6, 6, ManaColor.GREEN, SubType.valueOf("Wurm")),
	PENTAVITE_2(CREATURE_ARTIFACT_TOKEN, "M12", "7", "Flying", "Vol", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Pentavite")),

	// La nouvelle Phyrexia (NPH)
	BEAST_9(CREATURE_TOKEN, "NPH", "1", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	GOBLIN_7(CREATURE_TOKEN, "NPH", "2", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	GOLEM_3(CREATURE_ARTIFACT_TOKEN, "NPH", "3", 3, 3, ManaColor.COLORLESS, SubType.valueOf("Golem")),
	MYR_3(CREATURE_ARTIFACT_TOKEN, "NPH", "4", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Myr")),

	// Mirrodin assiégé (MBS)
	GERM_1(CREATURE_TOKEN, "MBS", "1", 0, 0, ManaColor.BLACK, SubType.valueOf("Germ")),
	ZOMBIE_7(CREATURE_TOKEN, "MBS", "2", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	GOLEM_2(CREATURE_ARTIFACT_TOKEN, "MBS", "3", 3, 3, ManaColor.COLORLESS, SubType.valueOf("Golem")),
	HORROR_1(CREATURE_ARTIFACT_TOKEN, "MBS", "4", 0, 0, ManaColor.COLORLESS, SubType.valueOf("Horror")),
	THOPTER_2(CREATURE_ARTIFACT_TOKEN, "MBS", "5", "Flying", "Vol", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Thopter")),

	// Les cicatrices de Mirrodin (SOM)
	CAT_1(CREATURE_TOKEN, "SOM", "1", 2, 2, ManaColor.WHITE, SubType.valueOf("Cat")),
	SOLDIER_6(CREATURE_TOKEN, "SOM", "2", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	GOBLIN_6(CREATURE_TOKEN, "SOM", "3", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	INSECT(CREATURE_TOKEN, "SOM", "4", "Infect #_(This creature deals damage to creatures in the form of -1/-1 counters and to players in the form of poison counters.)_#",
			"Infection #_(Cette créature inflige des blessures aux créatures sous la forme de marqueurs -1/-1 et aux joueurs sous la forme de marqueurs « poison ».)_#", 1, 1,
			ManaColor.GREEN, SubType.valueOf("Insect")),
	WOLF_5(CREATURE_TOKEN, "SOM", "5", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	GOLEM_1(CREATURE_ARTIFACT_TOKEN, "SOM", "6", 3, 3, ManaColor.COLORLESS, SubType.valueOf("Golem")),
	MYR_2(CREATURE_ARTIFACT_TOKEN, "SOM", "7", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Myr")),
	WURM_2(CREATURE_ARTIFACT_TOKEN, "SOM", "8", "Deathtouch", "Contact mortel", 3, 3, ManaColor.COLORLESS, SubType.valueOf("Wurm")),
	WURM_3(CREATURE_ARTIFACT_TOKEN, "SOM", "9", "Lifelink", "Lien de vie", 3, 3, ManaColor.COLORLESS, SubType.valueOf("Wurm")),

	// Magic 2011 (M11)
	AVATAR_3(CREATURE_TOKEN, "M11", "1", "This creature's power and toughness are each equal to your life total.",
			"La force et l'endurance de cette créature sont chacune égale à votre total de points de vie.", 0, 0, ManaColor.WHITE, SubType.valueOf("Avatar")),
	BIRD_4(CREATURE_TOKEN, "M11", "2", "Flying", "Vol", 3, 3, ManaColor.WHITE, SubType.valueOf("Bird")),
	ZOMBIE_6(CREATURE_TOKEN, "M11", "3", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	BEAST_8(CREATURE_TOKEN, "M11", "4", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	OOZE_3(CREATURE_TOKEN, "M11", "5", 1, 1, ManaColor.GREEN, SubType.valueOf("Ooze")),
	OOZE_4(CREATURE_TOKEN, "M11", "6", "When this creature is put into a graveyard, put two 1/1 green Ooze creature tokens onto the battlefield.",
			"Quand cette créature est mise dans un cimétière, mettez sur le champ de bataille 2 jetons de créature 1/1 verte Limon.", 2, 2, ManaColor.GREEN,
			SubType.valueOf("Ooze")),

	// L'ascension des Eldrazi (ROE)
	ELDRAZI_SPAWN_1(CREATURE_TOKEN, "ROE", "1a", "Sacrifice this creature:\nAdd {C} to your mana pool.", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve.", 0, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Spawn")}),
	ELDRAZI_SPAWN_2(CREATURE_TOKEN, "ROE", "1b", "Sacrifice this creature:\nAdd {C} to your mana pool.", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve.", 0, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Spawn")}),
	ELDRAZI_SPAWN_3(CREATURE_TOKEN, "ROE", "1c", "Sacrifice this creature:\nAdd {C} to your mana pool.", "Sacrifiez cette créature :\nAjoutez {C} à votre réserve.", 0, 1,
			ManaColor.COLORLESS, new SubType[]{SubType.valueOf("Eldrazi"), SubType.valueOf("Spawn")}),
	ELEMENTAL_8(CREATURE_TOKEN, "ROE", "2", 0, 0, ManaColor.RED, SubType.valueOf("Elemental")),
	HELLION_1(CREATURE_TOKEN, "ROE", "3", 4, 4, ManaColor.RED, SubType.valueOf("Hellion")),
	OOZE_2(CREATURE_TOKEN, "ROE", "4", 0, 0, ManaColor.GREEN, SubType.valueOf("Ooze")),
	TUKTUK_THE_RETURNED_1(CREATURE_ARTIFACT_TOKEN, "ROE", "5", "", "", 5, 5, ManaColor.COLORLESS, true, new SubType[]{SubType.valueOf("Goblin"), SubType.valueOf("Golem")}),

	// Worldwake (WWK)
	SOLDIER_ALLY(CREATURE_TOKEN, "WWK", "1", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Soldier"), SubType.valueOf("Ally")}),
	DRAGON_5(CREATURE_TOKEN, "WWK", "2", "Flying", "Vol", 5, 5, ManaColor.RED, SubType.valueOf("Dragon")),
	OGRE_1(CREATURE_TOKEN, "WWK", "3", 3, 3, ManaColor.RED, SubType.valueOf("Ogre")),
	ELEPHANT_3(CREATURE_TOKEN, "WWK", "4", 3, 3, ManaColor.GREEN, SubType.valueOf("Elephant")),
	PLANT_1(CREATURE_TOKEN, "WWK", "5", 0, 1, ManaColor.GREEN, SubType.valueOf("Plant")),
	CONSTRUCT_1(CREATURE_TOKEN, "WWK", "6", "Trample", "Piétinement", 6, 12, ManaColor.COLORLESS, SubType.valueOf("Construct")),

	// Zendikar (ZEN)
	ANGEL_3(CREATURE_TOKEN, "ZEN", "1", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	BIRD_3(CREATURE_TOKEN, "ZEN", "2", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Bird")),
	KOR_SOLDIER(CREATURE_TOKEN, "ZEN", "3", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Kor"), SubType.valueOf("Soldier")}),
	ILLUSION_1(CREATURE_TOKEN, "ZEN", "4", 2, 2, ManaColor.BLUE, SubType.valueOf("Illusion")),
	MERFOLK(CREATURE_TOKEN, "ZEN", "5", 1, 1, ManaColor.BLUE, SubType.valueOf("Merfolk")),
	VAMPIRE_1(CREATURE_TOKEN, "ZEN", "6", 0, 0, ManaColor.BLACK, SubType.valueOf("Vampire")),
	ZOMBIE_GIANT(CREATURE_TOKEN, "ZEN", "7", 5, 5, ManaColor.BLACK, new SubType[]{SubType.valueOf("Zombie"), SubType.valueOf("Giant")}),
	ELEMENTAL_7(CREATURE_TOKEN, "ZEN", "8", "Trample, haste", "Piétinement, célérité", 7, 1, ManaColor.RED, SubType.valueOf("Elemental")),
	BEAST_7(CREATURE_TOKEN, "ZEN", "9", 4, 4, ManaColor.GREEN, SubType.valueOf("Beast")),
	SNAKE_1(CREATURE_TOKEN, "ZEN", "10", 1, 1, ManaColor.GREEN, SubType.valueOf("Snake")),
	WOLF_4(CREATURE_TOKEN, "ZEN", "11", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),

	// Magic 2010 (M10)
	AVATAR_2(CREATURE_TOKEN, "M10", "1", "This creature's power and toughness are each equal to your life total.",
			"La force et l'endurance de cette créature sont chacune égale à votre total de points de vie.", 0, 0, ManaColor.WHITE, SubType.valueOf("Avatar")),
	SOLDIER_5(CREATURE_TOKEN, "M10", "2", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	ZOMBIE_5(CREATURE_TOKEN, "M10", "3", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	GOBLIN_5(CREATURE_TOKEN, "M10", "4", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	BEAST_6(CREATURE_TOKEN, "M10", "5", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	INSECT_2(CREATURE_TOKEN, "M10", "6", 1, 1, ManaColor.GREEN, SubType.valueOf("Insect")),
	WOLF_3(CREATURE_TOKEN, "M10", "7", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	GARGOYLE_1(CREATURE_ARTIFACT_TOKEN, "M10", "8", "Flying", "Vol", 3, 4, ManaColor.COLORLESS, SubType.valueOf("Gargoyle")),

	// La Renaissance d'Alara (ARB)
	BIRD_SOLDIER(CREATURE_TOKEN, "ARB", "1", "Flying", "Vol", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Bird"), SubType.valueOf("Soldier")}),
	LIZARD(CREATURE_TOKEN, "ARB", "2", 2, 2, ManaColor.GREEN, SubType.valueOf("Lizard")),
	DRAGON_4(CREATURE_TOKEN, "ARB", "3", "Flying, devour 2", "Vol, dévorement 2", 1, 1, new ManaColor[]{ManaColor.RED, ManaColor.GREEN}, false, SubType.valueOf("Dragon")),
	ZOMBIE_WIZARD(CREATURE_TOKEN, "ARB", "4", "", "", 1, 1, new ManaColor[]{ManaColor.BLUE, ManaColor.BLACK}, false, new SubType[]{SubType.valueOf("Zombie"),
			SubType.valueOf("Wizard")}),

	// Conflux (CON)
	ANGEL_2(CREATURE_TOKEN, "CON", "1", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),
	ELEMENTAL_6(CREATURE_TOKEN, "CON", "2", 3, 1, ManaColor.RED, SubType.valueOf("Elemental")),

	// Les Éclats d'Alara (ALA)
	SOLDIER_4(CREATURE_TOKEN, "ALA", "1", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	HOMUNCULUS_1(CREATURE_TOKEN, "ALA", "2", 0, 1, ManaColor.BLUE, SubType.valueOf("Homunculus")),
	THOPTER_1(CREATURE_TOKEN, "ALA", "3", "Flying", "Vol", 1, 1, ManaColor.BLUE, SubType.valueOf("Thopter")),
	SKELETON(CREATURE_TOKEN, "ALA", "4", "{B}: Regenerate this creature.", "{B} : Régénérez cette créature.", 1, 1, ManaColor.BLACK, SubType.valueOf("Skeleton")),
	ZOMBIE_4(CREATURE_TOKEN, "ALA", "5", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	DRAGON_3(CREATURE_TOKEN, "ALA", "6", "Flying", "Vol", 4, 4, ManaColor.RED, SubType.valueOf("Dragon")),
	GOBLIN_4(CREATURE_TOKEN, "ALA", "7", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	OOZE_1(CREATURE_TOKEN, "ALA", "8", 0, 0, ManaColor.GREEN, SubType.valueOf("Ooze")),
	SAPROLING_3(CREATURE_TOKEN, "ALA", "9", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling")),
	BEAST_5(CREATURE_TOKEN, "ALA", "10", "", "", 8, 8, new ManaColor[]{ManaColor.RED, ManaColor.GREEN, ManaColor.WHITE}, false, SubType.valueOf("Beast")),

	// Coucheciel (EVE)
	GOAT_1(CREATURE_TOKEN, "EVE", "1", 0, 1, ManaColor.WHITE, SubType.valueOf("Goat")),
	BIRD_2(CREATURE_TOKEN, "EVE", "2", "Flying", "Vol", 1, 1, ManaColor.BLUE, SubType.valueOf("Bird")),
	BEAST_4(CREATURE_TOKEN, "EVE", "3", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	SPIRIT_4(CREATURE_TOKEN, "EVE", "4", "Flying", "Vol", 1, 1, new ManaColor[]{ManaColor.BLACK, ManaColor.WHITE}, false, SubType.valueOf("Spirit")),
	ELEMENTAL_5(CREATURE_TOKEN, "EVE", "5", "Flying", "Vol", 5, 5, new ManaColor[]{ManaColor.BLUE, ManaColor.RED}, false, SubType.valueOf("Elemental")),
	WORM_1(CREATURE_TOKEN, "EVE", "6", "", "", 1, 1, new ManaColor[]{ManaColor.BLACK, ManaColor.GREEN}, false, SubType.valueOf("Worm")),
	GOBLIN_SOLDIER_2(CREATURE_TOKEN, "EVE", "7", "", "", 1, 1, new ManaColor[]{ManaColor.RED, ManaColor.WHITE}, false, new SubType[]{SubType.valueOf("Goblin"),
			SubType.valueOf("Soldier")}),

	// Sombrelande (SHM)
	KITHKIN_SOLDIER_2(CREATURE_TOKEN, "SHM", "1", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Kithkin"), SubType.valueOf("Soldier")}),
	SPIRIT_3(CREATURE_TOKEN, "SHM", "2", "Flying", "Vol", 1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),
	RAT_1(CREATURE_TOKEN, "SHM", "3", 1, 1, ManaColor.BLACK, SubType.valueOf("Rat")),
	ELEMENTAL_3(CREATURE_TOKEN, "SHM", "4", "Haste", "Célérité", 1, 1, ManaColor.RED, SubType.valueOf("Elemental")),
	ELF_WARRIOR_2(CREATURE_TOKEN, "SHM", "5", 1, 1, ManaColor.GREEN, new SubType[]{SubType.valueOf("Elf"), SubType.valueOf("Warrior")}),
	SPIDER_1(CREATURE_TOKEN, "SHM", "6", "Reach", "Portée", 1, 2, ManaColor.GREEN, SubType.valueOf("Spider")),
	WOLF_2(CREATURE_TOKEN, "SHM", "7", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	FAERIE_ROGUE_2(CREATURE_TOKEN, "SHM", "8", "Flying", "Vol", 1, 1, new ManaColor[]{ManaColor.BLUE, ManaColor.BLACK}, false, new SubType[]{SubType.valueOf("Faerie"), SubType.valueOf
			("Rogue")}),
	ELEMENTAL_4(CREATURE_TOKEN, "SHM", "9", "", "", 5, 5, new ManaColor[]{ManaColor.BLACK, ManaColor.RED}, false, SubType.valueOf("Elemental")),
	GIANT_WARRIOR_2(CREATURE_TOKEN, "SHM", "10", "Haste", "Célérité", 4, 4, new ManaColor[]{ManaColor.RED, ManaColor.GREEN}, false, new SubType[]{SubType.valueOf("Giant"),
			SubType.valueOf("Warrior")}),
	GOBLIN_WARRIOR(CREATURE_TOKEN, "SHM", "11", "", "", 1, 1, new ManaColor[]{ManaColor.RED, ManaColor.GREEN}, false, new SubType[]{SubType.valueOf("Goblin"),
			SubType.valueOf("Warrior")}),
	ELF_WARRIOR_3(CREATURE_TOKEN, "SHM", "12", "", "", 1, 1, new ManaColor[]{ManaColor.GREEN, ManaColor.WHITE}, false, new SubType[]{SubType.valueOf("Elf"),
			SubType.valueOf("Warrior")}),

	// Lèveciel (MOR)
	GIANT_WARRIOR_1(CREATURE_TOKEN, "MOR", "1", "", "", 5, 5, new ManaColor[]{ManaColor.RED, ManaColor.GREEN}, false, new SubType[]{SubType.valueOf("Giant"),
			SubType.valueOf("Warrior")}),
	FAERIE_ROGUE_1(CREATURE_TOKEN, "MOR", "2", "Flying", "Vol", 1, 1, ManaColor.BLACK, new SubType[]{SubType.valueOf("Faerie"), SubType.valueOf("Rogue")}),
	TREEFOLK_SHAMAN(CREATURE_TOKEN, "MOR", "3", 2, 5, ManaColor.GREEN, new SubType[]{SubType.valueOf("Treefolk"), SubType.valueOf("Shaman")}),

	// Lorwyn (LRW)
	AVATAR_1(CREATURE_TOKEN, "LRW", "1", "This creature's power and toughness are each equal to your life total.",
			"La force et l'endurance de cette créature sont chacune égale à votre total de points de vie.", 0, 0, ManaColor.WHITE, SubType.valueOf("Avatar")),
	ELEMENTAL_1(CREATURE_TOKEN, "LRW", "2", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Elemental")),
	KITHKIN_SOLDIER_1(CREATURE_TOKEN, "LRW", "3", 1, 1, ManaColor.WHITE, new SubType[]{SubType.valueOf("Kithkin"), SubType.valueOf("Soldier")}),
	MERFOLK_WIZARD(CREATURE_TOKEN, "LRW", "4", 1, 1, ManaColor.BLUE, new SubType[]{SubType.valueOf("Merfolk"), SubType.valueOf("Wizard")}),
	GOBLIN_ROGUE_1(CREATURE_TOKEN, "LRW", "5", 1, 1, ManaColor.BLACK, new SubType[]{SubType.valueOf("Goblin"), SubType.valueOf("Rogue")}),
	ELEMENTAL_SHAMAN_1(CREATURE_TOKEN, "LRW", "6", 3, 1, ManaColor.RED, new SubType[]{SubType.valueOf("Elemental"), SubType.valueOf("Shaman")}),
	BEAST_3(CREATURE_TOKEN, "LRW", "7", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	ELEMENTAL_2(CREATURE_TOKEN, "LRW", "8", 4, 4, ManaColor.GREEN, SubType.valueOf("Elemental")),
	ELF_WARRIOR_1(CREATURE_TOKEN, "LRW", "9", 1, 1, ManaColor.GREEN, new SubType[]{SubType.valueOf("Elf"), SubType.valueOf("Warrior")}),
	WOLF_1(CREATURE_TOKEN, "LRW", "10", 2, 2, ManaColor.GREEN, SubType.valueOf("Wolf")),
	SHAPESHIFTER(CREATURE_TOKEN, "LRW", "11", "Changelin #_(This token is every creature type.)_#", "Changelin #_(Ce jeton a tous les types de créature.)_#", 1, 1,
			ManaColor.COLORLESS, SubType.valueOf("Shapeshifter")),

	// Xème édition (10E)
	SOLDIER_3(CREATURE_TOKEN, "10E", "1", 1, 1, ManaColor.WHITE, SubType.valueOf("Soldier")),
	ZOMBIE_3(CREATURE_TOKEN, "10E", "2", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),
	DRAGON_2(CREATURE_TOKEN, "10E", "3", "Flying", "Vol", 5, 5, ManaColor.RED, SubType.valueOf("Dragon")),
	GOBLIN_3(CREATURE_TOKEN, "10E", "4", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),
	SAPROLING_2(CREATURE_TOKEN, "10E", "5", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling")),
	WASP(CREATURE_TOKEN, "10E", "6", "Flying", "Vol", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Insect")),

	// Souffle glaciaire (CSP)
	MARIT_LAGE(CREATURE_TOKEN, "CSP", "1", "Flying\nMarit Lage is indestructible.", "Vol\nMarit Lage est indestructible.", 20, 20, ManaColor.BLACK, true, SubType.valueOf
			("Avatar")),

	// Champions de Kamigawa (CHK)
	SPIRIT_2(CREATURE_TOKEN, "CHK", "1", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Spirit")),

	// Sombracier (DST)
	BEAST_2(CREATURE_TOKEN, "DST", "1", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),

	// Mirrodin (MRD)
	DEMON_1(CREATURE_TOKEN, "MRD", "1", "Flying", "Vol", 0, 0, ManaColor.BLACK, SubType.valueOf("Demon")),
	PENTAVITE_1(CREATURE_ARTIFACT_TOKEN, "MRD", "2", "Flying", "Vol", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Pentavite")),
	MYR(CREATURE_ARTIFACT_TOKEN, "MRD", "3", 1, 1, ManaColor.COLORLESS, SubType.valueOf("Myr")),

	// 8ème édition (8ED)
	RUKH(CREATURE_TOKEN, "8ED", "1", "Flying", "Vol", 4, 4, ManaColor.RED, SubType.valueOf("Rukh")),

	// Fléau (SCG)
	ANGEL_1(CREATURE_TOKEN, "SCG", "1", "Flying", "Vol", 4, 4, ManaColor.WHITE, SubType.valueOf("Angel")),

	// Légions (LGN)
	SLIVER_1(CREATURE_TOKEN, "LGN", "1", "#_Slivers don't learn. They just know._#", "Les silvoïdes n'apprennent pas. Elles savent juste._#", 1, 1, ManaColor.COLORLESS,
			SubType.valueOf("Sliver")),
	GOBLIN_1(CREATURE_TOKEN, "LGN", "2", "#_Never underestimate the power of overwhelming stupidity in overwhelming numbers._#",
			"#_Ne jamais sous-estimer la puissance de la stupidité écrasante en nombre écrasant._#", 1, 1, ManaColor.RED, SubType.valueOf("Goblin")),

	// Carnage (ONS)
	DRAGON_1(CREATURE_TOKEN, "ONS", "1", "Flying\n#_Stars shine, winds blow, dragons rage._#", "Vol\n#_Les étoiles brillent, le vent souffle, le dragon fait rage._#", 5, 5,
			ManaColor.RED, SubType.valueOf("Dragon")),
	SOLDIER_1(CREATURE_TOKEN, "ONS", "2", "#_Duty brings them together.\nLoyalty keeps them together._#", "#_Le devoir les rassemble.\nLa fidélité les maintient ensemble._#", 1, 1,
			ManaColor.WHITE, SubType.valueOf("Soldier")),
	INSECT_1(CREATURE_TOKEN, "ONS", "3", "#_If insects could talk, the world would be deafened._#", "#_Si les insectes pouvaient parler, le monde serait assourdi._#", 1, 1,
			ManaColor.GREEN, SubType.valueOf("Insect")),
	BEAR_2(CREATURE_TOKEN, "ONS", "4", "#_Bears get vicious when they don't sleep. This one hasn't slept in ages._#",
			"#_Les ours sont vicieux quand ils ne dorment pas. Celui-ci n'a pas dormi depuis des lustres._#", 2, 2, ManaColor.GREEN, SubType.valueOf("Bear")),

	// Odyssée (ODY)
	BEAR_1(CREATURE_TOKEN, "ODY", "1", "#_The last thing a poacher in the Krosan Forest hears is the brutal sound of claws tearing flesh._#",
			"#_La dernière chose dont un braconnier dans la forêt de Krosan entend est le son brutal de griffes déchirant la chair._#", 2, 2, ManaColor.GREEN,
			SubType.valueOf("Bear")),
	BEAST_1(CREATURE_TOKEN, "ODY", "2", "#_When the prey in Krosa no longer satisfied them, the beasts began to hunt their hunters._#",
			"#_Quand la proie dans Krosia ne les satisfait, les bêtes commencent à chasser leurs chasseurs._#", 3, 3, ManaColor.GREEN, SubType.valueOf("Beast")),
	ELEPHANT_2(CREATURE_TOKEN, "ODY", "3", "#_When elephants thunder, death is lightning quick._#", "#_Quand les éléphants retentissent, la mort est aussi rapide que l'éclair._#",
			3, 3, ManaColor.GREEN, SubType.valueOf("Elephant")),
	SQUIRREL_2(CREATURE_TOKEN, "ODY", "4", "#_It's surprising how well cute and vicious go together._#",
			"#_Il est surprenant de constater à quel point mignon et vicieux vont de pair._#", 1, 1, ManaColor.GREEN, SubType.valueOf("Squirrel")),
	WURM_1(CREATURE_TOKEN, "ODY", "5", "#_When wurm aren't hungry\n— Nantuko expression meaning \"never\"_#",
			"#_Quand les guivres n'ont pas faim\n—Expression de Nantuko signifiant « jamais »_#"),
	ZOMBIE_2(CREATURE_TOKEN, "ODY", "6", "#_The most battle-worn zombies are equal parts stitch and stench._#",
			"#_La plupart des combats contre les zombies sont parties égales entre points de suture et puanteur._#", 2, 2, ManaColor.BLACK, SubType.valueOf("Zombie")),

	// Apocalypse (APC)
	GOBLIN_SOLDIER_1(CREATURE_TOKEN, "APC", "1", "#_They rarely question their orders. Of course, they rarely understand them._#",
			"#_Ils interrogent rarement leurs ordres. Bien sûr, ils les comprennent rarement._#", 1, 1, new ManaColor[]{ManaColor.RED, ManaColor.WHITE}, false,
			new SubType[]{SubType.valueOf("Goblin"), SubType.valueOf("Soldier")}),

	// Planeshift (PLS)
	//SPIRIT_1(CREATURE_TOKEN, "PLS", "1", "Flying\n#_The joy of rebirth overshadows the sadness of death._#", "Vol\n#_La joie de la renaissance éclipse la tristesse de la mort._#",
	//		1, 1, ManaColor.WHITE, SubType.valueOf("Spirit")),

	// Invasion (INV)
	BIRD_1(CREATURE_TOKEN, "INV", "1", "Flying\n#_The landbound envy the bird's ultimate freedom in the sky._#",
			"Vol\n#_La terre consolidée envie la liberté ultime de l'oiseau dans le ciel._#", 1, 1, ManaColor.BLUE, SubType.valueOf("Bird")),
	ELEPHANT_1(CREATURE_TOKEN, "INV", "2", "#_It's not slow. It's deciding whether to stomp you or gore you._#",
			"#_Il n'est pas lent. Il est en train de décider de vous piétiner ou de vous encorner._#", 3, 3, ManaColor.GREEN, SubType.valueOf("Elephant")),
	SAPROLING_1(CREATURE_TOKEN, "INV", "3", "#_The nauseating wriggling of a saproling is exceeded only by the nauseating of its prey._#",
			"#_Le frétillement nauséabond d'un saprobionte est seulement dépassé par la puanteur de sa proie._#", 1, 1, ManaColor.GREEN, SubType.valueOf("Saproling"));

	private final CardType type;
	private final Set set;
	private final String ability_EN;
	private final String ability_FR;
	private final ManaColor[] color;
	private final SubType[] subtypes;
	private final String number;
	private final int power;
	private final int toughness;
	private final boolean legendary;
	private List<Card> related;

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, String... relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, new SubType[0], relatedCards);
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, SubType[] subtypes, String... relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, Integer.MIN_VALUE, Integer.MIN_VALUE, ManaColor.COLORLESS, subtypes, relatedCards);
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, int power, int toughness, ManaColor color, SubType[] subtypes, String... relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, power, toughness, color, false, subtypes, relatedCards);
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, int power, int toughness, ManaColor color, boolean legendary, SubType[] subtypes, String...
			relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, power, toughness, new ManaColor[]{color}, legendary, subtypes, relatedCards);
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, int power, int toughness, ManaColor[] color, boolean legendary, SubType[] subtypes, String...
			relatedCards)
	{
		this.type = type;
		this.set = MySQL.getSet(set);
		if (this.set == null)
			System.out.println("Unknown set: " + set);
		this.ability_EN = ability_EN;
		this.ability_FR = ability_FR;
		this.power = power;
		this.toughness = toughness;
		this.color = color;
		this.subtypes = subtypes;
		this.number = number;
		this.legendary = legendary;
		for (SubType st : subtypes)
			st.setCanApplicate(type);
		related = RefStreams.of(relatedCards).map(MySQL::getCard).collect(Collectors.toList());
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, SubType subtype, String... relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, Integer.MIN_VALUE, Integer.MIN_VALUE, ManaColor.COLORLESS, subtype, relatedCards);
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, int power, int toughness, ManaColor color, String... relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, power, toughness, color, false, new SubType[0], relatedCards);
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, int power, int toughness, ManaColor color, SubType subtype, String... relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, power, toughness, color, false, subtype, relatedCards);
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, int power, int toughness, ManaColor color, boolean legendary, SubType subtype,
		  String... relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, power, toughness, new ManaColor[]{color}, legendary, new SubType[]{subtype}, relatedCards);
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, int power, int toughness, ManaColor[] color, boolean legendary, SubType subtype, String...
			relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, power, toughness, color, legendary, new SubType[]{subtype}, relatedCards);
	}

	Token(CardType type, String set, String number, String ability_EN, String ability_FR, int power, int toughness, ManaColor[] color, SubType[] subtypes, String... relatedCards)
	{
		this(type, set, number, ability_EN, ability_FR, power, toughness, color, false, subtypes, relatedCards);
	}

	Token(CardType type, String set, String number, int power, int toughness, ManaColor color, SubType subtype, String... relatedCards)
	{
		this(type, set, number, "", "", power, toughness, color, subtype, relatedCards);
	}

	Token(CardType type, String set, String number, int power, int toughness, ManaColor color, SubType[] subtypes, String... relatedCards)
	{
		this(type, set, number, "", "", power, toughness, color, subtypes, relatedCards);
	}

	public String getEnglishName()
	{
		return I18n.entr("token." + name().toLowerCase().replaceAll("\\d|_", ""));
	}

	@Override
	public String toString()
	{
		String str = getTranslatedName().get();
		if (type != EMBLEM)
		{
			if (power >= 0)
				str += " (" + power + "/" + toughness + " ";
			else
				str += " (";
			if (!ability_FR.isEmpty() && ability_FR.length() < 42)
				str += ability_FR + " ";
			str += Joiner.on('/').join(color) + ")";
		}
		else
			str += " : " + subtypes[0];
		str += " (" + set.getCode() + ")";
		return str;
	}

	public StringBinding getTranslatedName() { return I18n.tr("token." + name().toLowerCase().replaceAll("\\d|_", "")); }

	public CardType getType()
	{
		return type;
	}

	public Set getSet()
	{
		return set;
	}

	public String getAbility_EN()
	{
		return ability_EN;
	}

	public String getAbility_FR()
	{
		return ability_FR;
	}

	public ManaColor[] getColor()
	{
		return color;
	}

	public SubType[] getSubtypes()
	{
		return subtypes;
	}

	public String getNumber()
	{
		return number;
	}

	public int getPower()
	{
		return power;
	}

	public int getToughness()
	{
		return toughness;
	}

	public boolean isLegendary()
	{
		return legendary;
	}

	public List<Card> getRelated()
	{
		return related;
	}
}