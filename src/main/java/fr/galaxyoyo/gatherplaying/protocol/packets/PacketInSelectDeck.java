package fr.galaxyoyo.gatherplaying.protocol.packets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.Library;
import fr.galaxyoyo.gatherplaying.OwnedCard;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;

public class PacketInSelectDeck extends Packet
{
	public Library library;

	@Override
	public void read(ByteBuf buf)
	{
		Library library = new Library(player);
		while (buf.isReadable())
			library.addCard(new OwnedCard(readCard(buf), player, buf.readBoolean()));
		library.shuffle();
		player.getData().setLibrary(library);
		if (player.runningParty.getOnlinePlayers().size() == player.runningParty.getSize())
		{
			Player starter = Lists.newArrayList(player.runningParty.getOnlinePlayers()).get(Utils.RANDOM.nextInt(player.runningParty.getOnlinePlayers().size()));
			PacketOutPartyStart pkt = PacketManager.createPacket(PacketOutPartyStart.class);
			pkt.starter = starter;
			PacketManager.sendPacketToParty(player.runningParty, pkt);
			Server.sendChat(player.runningParty, "Démarrage de la partie", "color: red;");
			Server.sendChat(player.runningParty, starter.name + " choisit qui commence", "color: green;");
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		for (OwnedCard card : library.getSortedCards())
		{
			writeCard(card.getCard(), buf);
			buf.writeBoolean(card.isFoiled());
		}
	}
}