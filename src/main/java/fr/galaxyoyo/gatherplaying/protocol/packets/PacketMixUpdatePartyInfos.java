package fr.galaxyoyo.gatherplaying.protocol.packets;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.SelectPartyMenu;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PacketMixUpdatePartyInfos extends Packet
{
	public Type type;
	public Party party;
	public List<Set> boosters;

	@Override
	public void read(ByteBuf buf)
	{
		type = Type.values()[buf.readByte()];
		if (Utils.getSide() == Side.SERVER)
		{
			if (type == Type.GET)
			{
				PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(getClass());
				pkt.type = type;
				PacketManager.sendPacketToPlayer(player, pkt);
			}
			else if (type == Type.CREATE)
			{
				party = new Party();
				party.setId(Party.getNextID());
				party.setName(readUTF(buf));
				party.setDesc(readUTF(buf));
				party.setRules(Rules.values()[buf.readByte()]);
				party.setSize(buf.readByte());
				int boosterCount = buf.readByte();
				if (boosterCount > 0)
					boosters = Lists.newArrayList();
				for (int i = 0; i < boosterCount; ++i)
					boosters.add(MySQL.getSet(readUTF(buf)));
				party.setBoosters(boosters);

				party.addPlayer(player);
				player.runningParty = party;
				Server.createParty(party);
				PacketMixUpdatePartyInfos pkt = createPacket();
				pkt.type = type;
				pkt.party = party;
				PacketManager.sendPacketToPlayer(player, pkt);
			}
			else if (type == Type.JOIN)
			{
				party = Server.getParty(buf.readInt());
				player.runningParty = party;
				party.addPlayer(player);
				for (Player p : party.getOnlinePlayers())
				{
					PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(getClass());
					pkt.type = Type.JOIN;
					pkt.party = party;
					PacketManager.sendPacketToPlayer(p, pkt);
				/*	for (Player pl : party.getOnlinePlayers())
					{
						PacketInSelectDeck pckt = PacketManager.createPacket(PacketInSelectDeck.class);
						pckt.library = party.getData(pl).getLibrary();
						PacketManager.sendPacketToPlayer(p, pckt);
					}*/
				}
				if (party.getRules().isLimited() && party.getOnlinePlayers().size() == party.getSize())
					party.start();
			}
			else if (type == Type.LEAVE)
			{
				if (player.runningParty == null)
					return;
				int numbers = 0;
				player.runningParty.removePlayer(player);
				for (Player p : player.runningParty.getOnlinePlayers())
				{
					PacketMixUpdatePartyInfos pkt = PacketManager.createPacket(getClass());
					pkt.type = Type.LEAVE;
					pkt.party = player.runningParty;
					PacketManager.sendPacketToPlayer(p, pkt);
					++numbers;
				}
				if (numbers == 0)
					Server.endParty(player.runningParty);
				player.runningParty = null;
			}
		}
		else
		{
			if (type == Type.GET)
			{
				SelectPartyMenu.getParties().clear();
				while (buf.isReadable())
				{
					Party party = new Party();
					party.setId(buf.readInt());
					party.setName(readUTF(buf));
					party.setDesc(readUTF(buf));
					party.setRules(Rules.values()[buf.readByte()]);
					party.setSize(buf.readByte());
					int numPlayers = buf.readByte();
					for (int i = 0; i < numPlayers; ++i)
					{
						UUID uuid = readUUID(buf);
						Player p = new Player();
						p.uuid = uuid;
						p.name = readUTF(buf);
						p.runningParty = party;
						party.addPlayer(p);
					}
					SelectPartyMenu.getParties().add(party);
				}
			}
			else if (type == Type.CREATE)
			{
				party = Client.getRunningParty();
				party.setId(buf.readInt());
			}
			else if (type == Type.JOIN || type == Type.LEAVE)
			{
				party = Client.getRunningParty();
				HashSet<UUID> connecteds = Sets.newHashSet();
				while (buf.isReadable())
				{
					UUID uuid = readUUID(buf);
					Player p = new Player();
					p.uuid = uuid;
					p.name = readUTF(buf);
					p.runningParty = party;
					connecteds.add(uuid);
					if (uuid.equals(Client.localPlayer.uuid))
						p = Client.localPlayer;
					if (party.getPlayer(uuid) == null)
						party.addPlayer(p);
				}
				party.getOnlinePlayers().stream().filter(p -> !connecteds.contains(p.uuid)).forEach(p -> party.removePlayer(p));
			}
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(type.ordinal());
		if (Utils.getSide() == Side.CLIENT)
		{
			if (type == Type.CREATE)
			{
				writeUTF(party.getName(), buf);
				writeUTF(party.getDesc(), buf);
				buf.writeByte(party.getRules().ordinal());
				buf.writeByte(party.getSize());

				if (boosters != null)
				{
					buf.writeByte(boosters.size());
					for (Set booster : boosters)
						writeUTF(booster.getCode(), buf);
				}
				else
					buf.writeByte(-1);
			}
			else if (type == Type.JOIN)
				buf.writeInt(party.getId());
		}
		else
		{
			if (type == Type.GET)
			{
				for (Party party : Server.getParties())
				{
					if (party.getOnlinePlayers().size() == party.getSize())
						continue;
					buf.writeInt(party.getId());
					writeUTF(party.getName(), buf);
					writeUTF(party.getDesc(), buf);
					buf.writeByte(party.getRules().ordinal());
					buf.writeByte(party.getSize());
					buf.writeByte(party.getOnlinePlayers().size());
					for (Player player : party.getOnlinePlayers())
					{
						writeUUID(player.uuid, buf);
						writeUTF(player.name, buf);
					}
				}
			}
			else if (type == Type.CREATE)
				buf.writeInt(party.getId());
			else if (type == Type.JOIN || type == Type.LEAVE)
			{
				for (Player p : party.getOnlinePlayers())
				{
					writeUUID(p.uuid, buf);
					writeUTF(p.name, buf);
				}
			}
		}
	}

	public enum Type
	{
		GET, CREATE, JOIN, LEAVE
	}
}