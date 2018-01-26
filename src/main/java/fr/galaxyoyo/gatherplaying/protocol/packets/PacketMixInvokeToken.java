package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

public class PacketMixInvokeToken extends Packet
{
	public Token token;
	public Player p;

	@Override
	public void read(ByteBuf buf)
	{
		token = Token.values()[buf.readShort()];
		p = player.runningParty.getPlayer(readUUID(buf));
		PlayedCard card = new PlayedCard(token, p);
		player.runningParty.getData(p).getPlayed().add(card);
		if (Utils.getSide() == Side.SERVER)
		{
			if (token.getType() != CardType.EMBLEM)
				Server.sendChat(player.runningParty, "chat.invoketoken", "color: blue;", player.name, "token." + token.name().toLowerCase().replaceAll("\\d|_", ""));
			else
				Server.sendChat(player.runningParty, "chat.invokeemblem", "color: blue;", player.name);
			sendToParty();
		} else
		{
			Platform.runLater(() -> {
				CardShower shower = CardShower.getShower(card);
				card.setPower(token.getPower());
				card.setToughness(token.getToughness());
				if (token.getType().is(CardType.CREATURE))
				{
					if (player == p)
						GameMenu.instance().creatures.getChildren().add(shower);
					else
						GameMenu.instance().adverseCreatures.getChildren().add(shower);
				} else
				{
					if (player == p)
						GameMenu.instance().enchants.getChildren().add(shower);
					else
						GameMenu.instance().adverseEnchants.getChildren().add(shower);
				}
				HBox.setMargin(shower, new Insets(0.0D, 10.5D, 0.0D, 10.5D));
			});
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeShort(token.ordinal());
		writeUUID(p.uuid, buf);
	}
}