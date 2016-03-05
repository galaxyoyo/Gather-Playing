package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.OwnedCard;
import fr.galaxyoyo.gatherplaying.client.gui.LoginDialog;
import io.netty.buffer.ByteBuf;

public class PacketOutConnectResponse extends Packet
{
	private boolean error = false;
	private String errorMessage;

	@Override
	public void read(ByteBuf buf)
	{
		error = buf.readBoolean();
		if (error)
			errorMessage = readUTF(buf);
		else
		{
			player.uuid = readUUID(buf);
			player.name = readUTF(buf);
			player.money = buf.readInt();
			player.cards.clear();
			while (buf.readByte() != -1)
				player.cards.add(new OwnedCard(readCard(buf), player, buf.readBoolean()));
		}
		LoginDialog.connectResponse(this);
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeBoolean(error);
		if (error)
			writeUTF(errorMessage, buf);
		else
		{
			writeUUID(player.uuid, buf);
			writeUTF(player.name, buf);
			buf.writeInt(player.money);
			for (OwnedCard card : player.cards)
			{
				buf.writeByte(0);
				writeCard(card.getCard(), buf);
				buf.writeBoolean(card.isFoiled());
			}
			buf.writeByte(-1);
		}
	}

	public void error(String message)
	{
		error = true;
		errorMessage = message;
	}

	public String getErrorMessage() { return errorMessage; }

	public boolean isErrored() { return error; }
}