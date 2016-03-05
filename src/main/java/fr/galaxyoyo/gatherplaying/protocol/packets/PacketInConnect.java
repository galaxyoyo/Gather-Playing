package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Deck;
import fr.galaxyoyo.gatherplaying.MySQL;
import fr.galaxyoyo.gatherplaying.Set;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class PacketInConnect extends Packet
{
	public Type type;

	@Override
	public void read(ByteBuf buf)
	{
		type = Type.values()[buf.readByte()];
		player.email = readUTF(buf);
		player.sha1Pwd = readUTF(buf);
		if (type == Type.REGISTERING)
			player.name = readUTF(buf);
		Player remote = MySQL.getPlayer(player.email);
		PacketOutConnectResponse resp = PacketManager.createPacket(PacketOutConnectResponse.class);
		if (type == Type.REGISTERING && remote != null)
			resp.error("Un compte existe déjà avec cette adresse");
		else if (type == Type.REGISTERING && MySQL.getPlayer(player.name) != null)
			resp.error("Un compte existe déjà avec ce pseudo");
		else if (type == Type.LOGGING && remote == null)
			resp.error("Le compte n'existe pas");
		else if (type == Type.REGISTERING)
		{
			if (player.name.length() < 6)
				resp.error("Le pseudo doit faire au moins 6 caractères");
			else if (!player.email.contains("@"))
				resp.error("Le format de l'adresse mail est invalide");
			else
			{
				player.uuid = UUID.randomUUID();
				MySQL.savePlayer(player);
			}
		}
		else if (type == Type.LOGGING)
		{
			assert remote != null;
			if (!remote.sha1Pwd.equals(player.sha1Pwd))
				resp.error("Le mot de passe est incorrect");
			else
				player.importFrom(remote);
		}
		PacketManager.sendPacketToPlayer(player, resp);
		if (!resp.isErrored())
		{
			Server.connectSuccess(player);
			for (Set set : MySQL.getAllSets())
			{
				if (!set.buyable)
					continue;
				PacketOutCosts pkt = PacketManager.createPacket(PacketOutCosts.class);
				pkt.set = set;
				PacketManager.sendPacketToPlayer(getPlayer(), pkt);
			}
			MySQL.readDecks(player);
			for (Deck deck : player.decks)
			{
				PacketMixDeck pkt = PacketManager.createPacket(PacketMixDeck.class);
				pkt.type = PacketMixDeck.Type.CREATING;
				pkt.deck = deck;
				PacketManager.sendPacketToPlayer(player, pkt);
			}
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(type.ordinal());
		writeUTF(player.email, buf);
		writeUTF(player.sha1Pwd, buf);
		if (type == Type.REGISTERING)
			writeUTF(player.name, buf);
	}

	public enum Type
	{
		LOGGING, REGISTERING
	}
}