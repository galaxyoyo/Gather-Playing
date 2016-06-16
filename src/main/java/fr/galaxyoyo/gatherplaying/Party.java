package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.DeckEditor;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.client.gui.PlayerInfos;
import fr.galaxyoyo.gatherplaying.protocol.packets.*;
import fr.galaxyoyo.gatherplaying.server.Server;
import java8.util.stream.StreamSupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Party
{
	private static final AtomicInteger NEXT_ID = new AtomicInteger(0);
	private int id;
	private int size;
	private String name;
	private String desc;
	private Rules rules;
	private Player player;
	private SpellTimer currentSpell;
	private ObservableMap<Player, PlayerData> datas = FXCollections.observableHashMap();
	private Phase currentPhase = Phase.MAIN;
	private boolean started = false;
	private List<Set> boosters;
	private int draftBoosterSize;
	private Map<Player, List<Card>> draftBoosters;
	private Map<Player, List<Card>> draftPicked;
	private Map<Player, Card> draftSelected;
	private List<Player> draftTable;

	public static int getNextID() { return NEXT_ID.getAndIncrement(); }

	public void start()
	{
		if (rules.isLimited())
		{
			if (Utils.getSide() == Side.CLIENT && Client.getCurrentController() instanceof DeckEditor)
				return;
			else if (Utils.getSide() == Side.SERVER && Lists.newArrayList(datas.values()).get(0).getLibrary() == null)
			{
				if (rules == Rules.SEALED)
				{
					for (Player player : getOnlinePlayers())
					{
						List<Card> list = Lists.newArrayList();
						for (Set set : boosters)
							list.addAll(Lists.newArrayList(set.generateBooster()));
						for (String basicLandName : new String[] {"Plains", "Island", "Swamp", "Forest", "Mountain"})
							list.add(StreamSupport.stream(MySQL.getAllCards()).filter(card -> card.isBasic() && card.getName().get("en").equals(basicLandName)).sorted(Card::compareTo)
									.findFirst().get());
						PacketOutOpenBooster pkt = PacketManager.createPacket(PacketOutOpenBooster.class);
						pkt.cards = list;
						PacketManager.sendPacketToPlayer(player, pkt);
					}
					return;
				}
				else
				{
					if (!boosters.isEmpty())
						sendNextDraftBooster();
					else
					{
						for (Player player : getOnlinePlayers())
						{
							List<Card> list = draftPicked.get(player);
							for (String basicLandName : new String[] {"Plains", "Island", "Swamp", "Forest", "Mountain"})
								list.add(StreamSupport.stream(MySQL.getAllCards()).filter(card -> card.isBasic() && card.getName().get("en").equals(basicLandName)).sorted(Card::compareTo)
										.findFirst().get());
							PacketOutOpenBooster pkt = PacketManager.createPacket(PacketOutOpenBooster.class);
							pkt.cards = list;
							PacketManager.sendPacketToPlayer(player, pkt);
						}
					}
					return;
				}
			}
		}

		assert !started;
		started = true;
		if (Utils.getSide() == Side.SERVER)
		{
			for (Player p : getOnlinePlayers())
			{
				PacketMixDrawCard draw = PacketManager.createPacket(PacketMixDrawCard.class);
				draw.count = 7;
				draw.cards = Lists.newArrayList();
				for (int i = 0; i < 7; ++i)
				{
					OwnedCard card = getData(p).getLibrary().drawCard();
					getData(p).getHand().add(card);
					draw.cards.add(card);
				}
				PacketManager.sendPacketToParty(this, draw);
			}
		}
		else
		{
			System.out.println("Partie démarrée !");
			setCurrentPhase(Phase.MAIN);
			GameMenu.instance().setPhase(Phase.MAIN);
			for (Player p : getOnlinePlayers())
				PlayerInfos.getInfos(p).updateLife();
		}
	}

	private void sendNextDraftBooster()
	{
		if (draftBoosters == null)
		{
			draftBoosters = Maps.newHashMap();
			draftPicked = Maps.newHashMap();
			draftSelected = Maps.newHashMap();
			for (Player player : getOnlinePlayers())
				draftPicked.put(player, Lists.newArrayList());
			draftBoosterSize = 0;
			draftTable = Lists.newArrayList(getOnlinePlayers());
		}

		if (--draftBoosterSize <= 0)
		{
			if (boosters.isEmpty())
			{
				start();
				return;
			}
			Collections.reverse(draftTable);
			Set nextBoosterSet = boosters.remove(0);
			draftBoosterSize = nextBoosterSet.booster.length;
			for (Player player : getOnlinePlayers())
			{
				Card[] booster = nextBoosterSet.generateBooster();
				draftBoosters.put(player, Lists.newArrayList(booster));
				PacketOutOpenBooster pkt = PacketManager.createPacket(PacketOutOpenBooster.class);
				pkt.cards = draftBoosters.get(player);
				PacketManager.sendPacketToPlayer(player, pkt);
			}
			return;
		}

		Map<Player, List<Card>> copy = Maps.newHashMap(draftBoosters);
		for (Player player : getOnlinePlayers())
		{
			int index = draftTable.indexOf(player) + 1;
			if (index == draftTable.size())
				index = 0;
			Player giveTo = draftTable.get(index);
			draftBoosters.put(giveTo, copy.get(player));
			PacketOutOpenBooster pkt = PacketManager.createPacket(PacketOutOpenBooster.class);
			pkt.cards = draftBoosters.get(giveTo);
			PacketManager.sendPacketToPlayer(giveTo, pkt);
		}
	}

	public void selectDraft(Player player, Card selected)
	{
		draftSelected.put(player, selected);
		if (draftSelected.size() == getOnlinePlayers().size())
		{
			for (Player p : getOnlinePlayers())
			{
				draftPicked.get(p).add(draftSelected.get(p));
				draftBoosters.get(p).remove(draftSelected.get(p));
			}
			draftSelected.clear();
			sendNextDraftBooster();
		}
	}

	public void addPlayer(Player player)
	{
		datas.put(player, new PlayerData(player));
	}

	public void removePlayer(Player player)
	{
		datas.remove(player);
		if (datas.isEmpty())
			Server.endParty(this);
	}

	public boolean isStarted() { return started; }

	public Phase getCurrentPhase() { return currentPhase; }

	public void setCurrentPhase(Phase phase)
	{
		this.currentPhase = phase;
		if (phase == Phase.END)
		{
			for (PlayedCard card : getData(player).getPlayed())
				card.setSummoningSickness(false);
		}
		if (Utils.getSide() == Side.CLIENT)
			return;
		Server.sendChat(this, "text.phase.prefix", null, "text.phase." + phase.name().toLowerCase());
		if (phase == Phase.UNTAP)
		{
			for (int i = 0; i < getData(player).getPlayed().size(); ++i)
			{
				PlayedCard card = getData(player).getPlayed().get(i);
				card.untap();
				PacketMixTapCard pkt = PacketManager.createPacket(PacketMixTapCard.class);
				pkt.shouldTap = false;
				pkt.index = i;
				pkt.card = card;
				PacketManager.sendPacketToParty(this, pkt);
			}
			nextPhase();
		}
		else if (phase == Phase.UPKEEP)
			nextPhase();
		else if (phase == Phase.DRAW)
		{
			PacketMixDrawCard pkt = PacketManager.createPacket(PacketMixDrawCard.class);
			pkt.count = 1;
			pkt.cards = Lists.newArrayList();
			OwnedCard card = getData(player).getLibrary().drawCard();
			getData(player).getHand().add(card);
			pkt.cards.add(card);
			PacketManager.sendPacketToParty(this, pkt);
			nextPhase();
		}
		else if (phase == Phase.END)
		{
			PacketMixSetPhase pkt = PacketManager.createPacket(PacketMixSetPhase.class);
			pkt.phase = Phase.UNTAP;
			player = getOnlinePlayers().stream().filter(pl -> !pl.uuid.equals(player.uuid)).findAny().get();
			pkt.p = player;
			PacketManager.sendPacketToParty(this, pkt);
			Server.sendChat(this, "C'est désormais au tour de " + player.name, null);
			setCurrentPhase(Phase.UNTAP);
		}
	}

	private void nextPhase()
	{
		PacketMixSetPhase pkt = PacketManager.createPacket(PacketMixSetPhase.class);
		pkt.phase = Phase.values()[currentPhase.ordinal() + 1];
		pkt.p = player;
		PacketManager.sendPacketToParty(this, pkt);
		setCurrentPhase(pkt.phase);
	}

	public PlayerData getData(UUID uuid)
	{
		return getData(getPlayer(uuid));
	}

	public PlayerData getData(Player player)
	{
		return datas.get(player);
	}

	public Player getPlayer(UUID uuid) { return getOnlinePlayers().stream().filter(player -> player.uuid.equals(uuid)).findAny().orElse(null); }

	public java.util.Set<Player> getOnlinePlayers() { return datas.keySet(); }

	@SuppressWarnings("UnusedParameters")
	public void broadcastEvent(EventType type, PlayedCard... parameters)
	{
		/*for (PlayerData data : datas.values())
		{
			for (PlayedCard card : data.getPlayed())
			{
				for (Capacity c : card.capacities)
				{
					switch (type)
					{
						case CAST_SPELL:
							c.onSpellCast(card, parameters[0]);
							break;
						case CARD_JOIN:
							if (parameters.length == 0)
								c.onCardJoinBattlefield(card);
							else
								c.onCardJoinBattlefield(card, parameters[0]);
							break;
						case CARD_DESTROY:
							if (parameters.length == 0)
								c.onCardDestroyed(card);
							else
								c.onCardDestroyed(card, parameters[0]);
							break;
					}
				}
			}
		}*/
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	public Rules getRules()
	{
		return rules;
	}

	public void setRules(Rules rules)
	{
		this.rules = rules;
	}

	public Player getPlayer()
	{
		return player;
	}

	public void setPlayer(Player player)
	{
		this.player = player;
	}

	public SpellTimer getCurrentSpell()
	{
		return currentSpell;
	}

	public void setCurrentSpell(SpellTimer currentSpell)
	{
		this.currentSpell = currentSpell;
	}

	public void setBoosters(List<Set> boosters)
	{
		this.boosters = boosters;
	}

	public enum EventType
	{
		CAST_SPELL, CARD_JOIN, CARD_DESTROY
	}
}