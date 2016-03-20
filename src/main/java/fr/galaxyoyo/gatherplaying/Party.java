package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.protocol.packets.*;
import fr.galaxyoyo.gatherplaying.server.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

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

	public static int getNextID() { return NEXT_ID.getAndIncrement(); }

	public void start()
	{
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
			//	GameMenu.INSTANCE.setPhase(Phase.MAIN);
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
		} else if (phase == Phase.UPKEEP)
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
		} else if (phase == Phase.END)
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

	public enum EventType
	{
		CAST_SPELL, CARD_JOIN, CARD_DESTROY
	}
}