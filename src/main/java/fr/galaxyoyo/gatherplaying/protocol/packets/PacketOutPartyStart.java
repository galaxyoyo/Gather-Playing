package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.Phase;
import fr.galaxyoyo.gatherplaying.Player;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.client.gui.PlayerInfos;
import io.netty.buffer.ByteBuf;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.util.StringConverter;
import javafx.util.converter.FormatStringConverter;

import java.text.ChoiceFormat;

public class PacketOutPartyStart extends Packet
{
	public Player starter;

	@Override
	public void read(ByteBuf buf)
	{
		for (Player ignored : Client.getRunningParty().getOnlinePlayers())
		{
			Player p = Client.getRunningParty().getPlayer(readUUID(buf));
			Client.getRunningParty().getData(p).setLibrary(null);
			if (p != Client.localPlayer)
				GameMenu.instance().adverseInfos.setPlayer(p);
			PlayerInfos.getInfos(p).setLibrary(buf.readByte());
		}
		GameMenu.instance().setPhase(Phase.MAIN);
		starter = Client.getRunningParty().getPlayer(readUUID(buf));
		if (starter == Client.localPlayer)
		{
			ChoiceDialog<Player> dialog = new ChoiceDialog<>(Client.localPlayer, Client.getRunningParty().getOnlinePlayers());
			dialog.setTitle("Début de partie");
			dialog.setHeaderText("Choisissez qui doit commencer :");
			((ChoiceBox<Player>) dialog.getDialogPane().getContent()).setConverter(new StringConverter<Player>() {
				@Override
				public String toString(Player object)
				{
					return object.name;
				}

				@Override
				public Player fromString(String string)
				{
					return null;
				}
			});
			dialog.showAndWait().ifPresent(player -> {
				PacketMixSelectStarter pkt = PacketManager.createPacket(PacketMixSelectStarter.class);
				pkt.player = player;
				PacketManager.sendPacketToServer(pkt);
			});
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		for (Player p : starter.runningParty.getOnlinePlayers())
		{
			writeUUID(p.uuid, buf);
			buf.writeByte(player.runningParty.getData(p).getLibrary().getSortedCards().size());
		}
		writeUUID(starter.uuid, buf);
	}
}