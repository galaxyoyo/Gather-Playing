package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.scene.layout.HBox;

public class PacketMixMoveCard extends Packet
{
	public short dataPos;
	public short newPos;
	public String parentId;
	public Player p;

	@Override
	public void read(ByteBuf buf)
	{
		p = player.runningParty.getPlayer(readUUID(buf));
		parentId = readUTF(buf);
		dataPos = buf.readShort();
		newPos = buf.readShort();
		if (Utils.getSide() == Side.SERVER)
			sendToParty();
		else
		{
			Platform.runLater(() -> {
				if (player != p)
					parentId = "adverse" + Character.toUpperCase(parentId.charAt(0)) + parentId.substring(1);
				HBox box = (HBox) GameMenu.INSTANCE.getParent().lookup("#" + parentId);
				PlayedCard card = p.getData().getPlayed().get(dataPos);
				CardShower shower = CardShower.getShower(card);
				box.getChildren().remove(shower);
				box.getChildren().add(newPos, shower);
			});
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		writeUUID(p.uuid, buf);
		writeUTF(parentId, buf);
		buf.writeShort(dataPos);
		buf.writeShort(newPos);
	}
}