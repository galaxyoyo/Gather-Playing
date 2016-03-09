package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Party;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.I18n;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.ArrayUtils;

public class PacketMixChat extends Packet
{
	public Party party;
	public String message;
	public String style = "";
	public String[] args = new String[0];

	@Override
	public void read(ByteBuf buf)
	{
		party = Server.getParty(buf.readInt());
		message = readUTF(buf);
		style = readUTF(buf);
		while (buf.isReadable())
			args = ArrayUtils.add(args, readUTF(buf));
		message = I18n.strTr(message, args);
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
		else
		{
			if (!style.isEmpty())
				message = "<span style=\"" + style + "\">" + message + "</span>";
			GameMenu.chat(message);
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeInt(party.getId());
		writeUTF(message, buf);
		writeUTF(style, buf);
		for (String arg : args)
			writeUTF(arg, buf);
	}
}