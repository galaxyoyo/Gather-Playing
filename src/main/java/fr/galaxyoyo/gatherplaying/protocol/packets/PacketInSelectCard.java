package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.Party;
import io.netty.buffer.ByteBuf;

public class PacketInSelectCard extends Packet
{
	public Card selected;

	@Override
	public void read(ByteBuf buf)
	{
		selected = readCard(buf);
		Party party = player.runningParty;
		party.selectDraft(player, selected);
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeCard(selected, buf);
	}
}
