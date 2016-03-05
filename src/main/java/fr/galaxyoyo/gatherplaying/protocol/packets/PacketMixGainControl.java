package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.server.Server;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;

public class PacketMixGainControl extends Packet
{
	public Player oldController, newController;
	public int index;

	@Override
	public void read(ByteBuf buf)
	{
		oldController = player.runningParty.getPlayer(readUUID(buf));
		newController = player.runningParty.getPlayer(readUUID(buf));
		index = buf.readInt();
		PlayedCard card = player.runningParty.getData(oldController).getPlayed().remove(index);
		player.runningParty.getData(newController).getPlayed().add(card);
		card.controller = newController;
		if (Utils.getSide() == Side.SERVER)
		{
			sendToParty();
			if (newController != card.owner)
				Server.sendChat(player.runningParty, "chat.gaincontrol", null, newController.name, "<i>" + card.getTranslatedName().get() + "</i>",
						card.owner.name);
			else
				Server.sendChat(player.runningParty, "chat.recovercontrol", null, newController.name, "<i>" + card.getTranslatedName().get() + "</i>");
		}
		else
		{
			CardShower shower = CardShower.getShower(card);
			if (player == newController)
			{
				Platform.runLater(() -> GameMenu.INSTANCE.adverseCreatures.getChildren().remove(shower));
				Platform.runLater(() -> GameMenu.INSTANCE.creatures.getChildren().add(shower));
			}
			else
			{
				Platform.runLater(() -> GameMenu.INSTANCE.creatures.getChildren().remove(shower));
				Platform.runLater(() -> GameMenu.INSTANCE.adverseCreatures.getChildren().add(shower));
			}
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(oldController.uuid, buf);
		writeUUID(newController.uuid, buf);
		buf.writeInt(index);
	}
}