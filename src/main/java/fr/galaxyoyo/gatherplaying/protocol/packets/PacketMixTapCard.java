package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.Player;
import io.netty.buffer.ByteBuf;

public class PacketMixTapCard extends Packet
{
	public PlayedCard card;
	public int index;
	public boolean shouldTap;

	@Override
	public void read(ByteBuf buf)
	{
		Player p = player.runningParty.getPlayer(readUUID(buf));
		index = buf.readInt();
		card = player.runningParty.getData(p).getPlayed().get(index);
		shouldTap = buf.readBoolean();
		if (shouldTap)
			card.tap();
		else
			card.untap();
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
		else
			CardShower.getShower(card).updateTap();
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(card.controller.uuid, buf);
		buf.writeInt(index);
		buf.writeBoolean(shouldTap);
	}
}