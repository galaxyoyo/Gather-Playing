package fr.galaxyoyo.gatherplaying.server;

import com.google.common.collect.Maps;
import fr.galaxyoyo.gatherplaying.Party;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketManager;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixChat;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixUpdatePartyInfos;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixUpdatePartyInfos.Type;
import io.netty.channel.Channel;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class Server
{
	private static final Map<Channel, Player> connectings = Maps.newHashMap();
	private static final Map<UUID, Player> players = Maps.newHashMap();
	private static final Map<Integer, Party> parties = Maps.newHashMap();

	public static Collection<Player> getPlayers() { return players.values(); }

	public static Player getPlayer(UUID uuid) { return players.get(uuid); }

	public static void addTempPlayer(Player player) { connectings.put(player.connection, player); }

	public static void connectSuccess(Player player)
	{
		connectings.remove(player.connection);
		players.put(player.uuid, player);
	}

	public static void disconnect(Channel channel)
	{
		Player player = getPlayer(channel);
		if (player == null)
		{
			connectings.remove(channel);
			return;
		}
		if (player.runningParty != null)
		{
			int numbers = 0;
			player.runningParty.removePlayer(player);
			for (int i = 0; i < player.runningParty.getSize(); ++i)
			{
				PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(PacketMixUpdatePartyInfos.class);
				pkt.type = Type.LEAVE;
				pkt.party = player.runningParty;
				PacketManager.sendPacketToPlayer(player, pkt);
				++numbers;
			}
			if (numbers == 0)
				endParty(player.runningParty);
		}
		players.remove(player.uuid);
		connectings.remove(channel);
	}

	public static Player getPlayer(Channel channel)
	{
		return players.values().stream().filter(p -> p.connection.remoteAddress().equals(channel.remoteAddress())).findAny().orElse(connectings.get(channel));
	}

	public static void endParty(Party party) { parties.remove(party.getId()); }

	public static Party getParty(int id) { return parties.get(id); }

	public static void createParty(Party party) { parties.put(party.getId(), party); }

	public static Collection<Party> getParties() { return parties.values(); }

	public static void sendChat(Party p, String msg, String style, String... args)
	{
		for (Player pl : p.getOnlinePlayers())
		{
			if (pl != null)
				sendChat(pl, msg, style, args);
		}
	}

	public static void sendChat(Player p, String msg, String style, String... args)
	{
		PacketMixChat pkt = PacketManager.createPacket(PacketMixChat.class);
		pkt.party = p.runningParty;
		pkt.message = msg;
		pkt.args = args;
		if (style != null)
			pkt.style = style;
		PacketManager.sendPacketToPlayer(p, pkt);
	}
}