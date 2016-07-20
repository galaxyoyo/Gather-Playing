package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.Arrow;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;

public class PacketMixDrawLine extends Packet
{
	public Targetable from;
	public Targetable to;

	@Override
	public void read(ByteBuf buf)
	{
		PlayerData fromData = player.runningParty.getData(readUUID(buf));
		byte type = buf.readByte();
		if (type == 0)
			from = fromData.getPlayed().get(buf.readInt());
		else if (type == 1)
			from = fromData.getHand().get(buf.readInt());
		else if (type == 2)
			from = fromData;

		PlayerData toData = player.runningParty.getData(readUUID(buf));
		type = buf.readByte();
		if (type == 0)
			to = toData.getPlayed().get(buf.readInt());
		else if (type == 1)
			to = toData.getHand().get(buf.readInt());
		else if (type == 2)
			to = toData;

		if (Utils.getSide() == Side.SERVER)
			sendToParty();
		else
		{
			Platform.runLater(() -> {
				Node fromShower = from.getVisible();
				Node toShower = to.getVisible();
				Arrow arrow = new Arrow();
				ChangeListener<Object> listener = (observable, oldValue, newValue) -> {
					Bounds fromBounds = fromShower.localToScene(fromShower.getBoundsInLocal());
					Bounds toBounds = toShower.localToScene(toShower.getBoundsInLocal());
					arrow.setX1(fromBounds.getMinX() + 37.0D);
					arrow.setY1(fromBounds.getMinY() + 51.5D);
					arrow.setX2(toBounds.getMinX() + 37.0D);
					arrow.setY2(toBounds.getMinY() + 51.5D);
				};
				Client.getStage().widthProperty().addListener(listener);
				Client.getStage().heightProperty().addListener(listener);
				Client.getStage().maximizedProperty().addListener(listener);
				listener.changed(null, 0, 0);
			});
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		if (from instanceof PlayedCard)
		{
			if (!((PlayedCard) from).isHand())
			{
				writeUUID(((PlayedCard) from).getController().uuid, buf);
				buf.writeByte(0);
				buf.writeInt(player.runningParty.getData(((PlayedCard) from).getController()).getPlayed().indexOf(from));
			}
			else
			{
				writeUUID(((OwnedCard) from).getOwner().uuid, buf);
				buf.writeByte(1);
				buf.writeInt(((OwnedCard) from).getOwner().getData().getHand().indexOf(from));
			}
		}
		else if (from instanceof PlayerData)
		{
			writeUUID(((PlayerData) from).getPlayer().uuid, buf);
			buf.writeByte(2);
		}

		if (to instanceof PlayedCard)
		{
			if (!((PlayedCard) to).isHand())
			{
				writeUUID(((PlayedCard) to).getController().uuid, buf);
				buf.writeByte(0);
				buf.writeInt(player.runningParty.getData(((PlayedCard) to).getController()).getPlayed().indexOf(to));
			}
			else
			{
				writeUUID(((OwnedCard) to).getOwner().uuid, buf);
				buf.writeByte(1);
				buf.writeInt(((OwnedCard) to).getOwner().getData().getHand().indexOf(to));
			}
		}
		else if (to instanceof PlayerData)
		{
			writeUUID(((PlayerData) to).getPlayer().uuid, buf);
			buf.writeByte(2);
		}
	}
}
