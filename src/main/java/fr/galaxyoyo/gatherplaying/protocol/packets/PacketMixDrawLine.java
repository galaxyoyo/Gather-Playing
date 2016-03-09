package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.PlayedCard;
import fr.galaxyoyo.gatherplaying.Side;
import fr.galaxyoyo.gatherplaying.Utils;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.Arrow;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;

public class PacketMixDrawLine extends Packet
{
	public PlayedCard from;
	public PlayedCard to;

	@Override
	public void read(ByteBuf buf)
	{
		from = player.runningParty.getData(readUUID(buf)).getPlayed().get(buf.readInt());
		to = player.runningParty.getData(readUUID(buf)).getPlayed().get(buf.readInt());

		if (Utils.getSide() == Side.SERVER)
			sendToParty();
		else
		{
			Platform.runLater(() -> {
				CardShower fromShower = CardShower.getShower(from);
				CardShower toShower = CardShower.getShower(to);
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
		writeUUID(from.getController().uuid, buf);
		buf.writeInt(player.runningParty.getData(from.getController()).getPlayed().indexOf(from));
		writeUUID(to.getController().uuid, buf);
		buf.writeInt(player.runningParty.getData(to.getController()).getPlayed().indexOf(to));
	}
}
