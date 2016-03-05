package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.Player;
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
		token = Token.values()[buf.readByte()];
		p = player.runningParty.getPlayer(readUUID(buf));
		PlayedCard card = new PlayedCard(token, p);
		player.runningParty.getData(p).getPlayed().add(card);
		if (Utils.getSide() == Side.SERVER)
		{
			if (token.type != CardType.EMBLEM)
				Server.sendChat(player.runningParty, "chat.invoketoken", "color: blue;", player.name, "token." + token.name().toLowerCase().replaceAll("\\d|_", ""));
			else
				Server.sendChat(player.runningParty, "chat.invokeemblem", "color: blue;", player.name);
			sendToParty();
		}
		else
		{
			Platform.runLater(() -> {
				CardShower shower = new CardShower(card);
				card.power.set(token.power);
				card.toughness.set(token.toughness);
				if (token.type.is(CardType.CREATURE))
				{
					if (player == p)
						GameMenu.INSTANCE.creatures.getChildren().add(shower);
					else
						GameMenu.INSTANCE.adverseCreatures.getChildren().add(shower);
				}
				else
				{
					if (player == p)
						GameMenu.INSTANCE.enchants.getChildren().add(shower);
					else
						GameMenu.INSTANCE.adverseEnchants.getChildren().add(shower);
				}
				HBox.setMargin(shower, new Insets(0.0D, 10.5D, 0.0D, 10.5D));
			});
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(token.ordinal());
		writeUUID(p.uuid, buf);
	}
}