package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.scene.layout.HBox;

public class PacketMixAttachCard extends Packet
{
	public Action action;
	public PlayedCard attached, attacher; // Attacher is the enchantment / equipment and Attached is the enchanted / equipped

	@Override
	public void read(ByteBuf buf)
	{
		action = Action.values()[buf.readByte()];
		Player attachedController = player.runningParty.getPlayer(readUUID(buf));
		System.out.println("ATTACHED CONTROLLER : " + attachedController);
		attached = attachedController.getData().getPlayed().get(buf.readShort());
		Player attacherController = player.runningParty.getPlayer(readUUID(buf));
		attacher = attacherController.getData().getPlayed().get(buf.readShort());

		if (action == Action.ATTACH)
			attached.getAssociatedCards().add(attacher);
		else
		{
			if (Utils.getSide() == Side.CLIENT)
			{
				Platform.runLater(() -> {
					CardShower shower = CardShower.getShower(attacher);
					((HBox) CardShower.getShower(attached).getParent()).getChildren().add(shower);
					shower.setTranslateX(0.0D);
					shower.setTranslateY(0.0D);
				});
			}
			attacher.setAssociatedCard(null);
			attached.getAssociatedCards().remove(attacher);
		}

		if (Utils.getSide() == Side.SERVER)
			sendToParty();
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(action.ordinal());
		System.out.println("WROTE ATTACHED CONTROLLER : " + attached.getController());
		writeUUID(attached.getController().uuid, buf);
		buf.writeShort(attached.getController().getData().getPlayed().indexOf(attached));
		writeUUID(attacher.getController().uuid, buf);
		buf.writeShort(attacher.getController().getData().getPlayed().indexOf(attacher));
	}

	public enum Action
	{
		ATTACH, DETACH
	}
}
