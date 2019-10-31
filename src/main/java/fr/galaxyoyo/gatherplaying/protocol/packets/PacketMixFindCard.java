package fr.galaxyoyo.gatherplaying.protocol.packets;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.client.gui.FoundCardShower;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PacketMixFindCard extends Packet
{
	public FilterType type;
	public Object filter;

	@Override
	public void read(ByteBuf buf)
	{
		type = FilterType.values()[buf.readByte()];
		if (Utils.getSide() == Side.SERVER)
		{
			Library lib = player.getData().getLibrary();
			List<OwnedCard> list = null;
			Stream<OwnedCard> allCards = lib.getSortedCards().stream();
			switch (type)
			{
				case RARITY:
					filter = Rarity.values()[buf.readByte()];
					list = allCards.filter(card -> card.getCard().getRarity() == filter).collect(Collectors.toList());
					break;
				case SUBTYPE:
					filter = SubType.valueOf(readUTF(buf));
					list = allCards.filter(card -> ArrayUtils.contains(card.getCard().getSubtypes(), filter)).collect(Collectors.toList());
					break;
				case TYPE:
					filter = CardType.values()[buf.readByte()];
					list = allCards.filter(card -> card.getCard().getType().is((CardType) filter)).collect(Collectors.toList());
					break;
			}
			PacketMixFindCard pkt = createPacket();
			pkt.type = type;
			pkt.filter = list;
			PacketManager.sendPacketToPlayer(player, pkt);
		} else
		{
			List<OwnedCard> allCards = Lists.newArrayList();
			while (buf.isReadable())
				allCards.add(new OwnedCard(readCard(buf), player, buf.readBoolean()));

			Platform.runLater(() -> {
				Dialog<Object> dialog = new Dialog<>();
				HBox box = new HBox();
				for (OwnedCard c : allCards)
					box.getChildren().add(new FoundCardShower(c));
				ScrollPane pane = new ScrollPane(box);
				pane.setPrefHeight(355.0D);
				pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				pane.setStyle("-fx-background-color: null;");
				dialog.getDialogPane().setContent(pane);
				dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
				dialog.showAndWait();
			});
		}
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(type.ordinal());
		if (Utils.getSide() == Side.CLIENT)
		{
			switch (type)
			{
				case RARITY:
					buf.writeByte(((Rarity) filter).ordinal());
					break;
				case SUBTYPE:
					writeUTF(((SubType) filter).name, buf);
					break;
				case TYPE:
					buf.writeByte(((CardType) filter).ordinal());
					break;
			}
		} else
		{
			@SuppressWarnings("unchecked") List<OwnedCard> allCards = (List<OwnedCard>) filter;
			allCards.forEach(card -> {
				writeCard(card.getCard(), buf);
				buf.writeBoolean(card.isFoiled());
			});
		}
	}

	public enum FilterType
	{
		TYPE(CardType.class, "Card type", "Type de carte", "", ""), SUBTYPE(SubType.class, "Subtype", "Sous-type", "", ""), RARITY(Rarity.class, "Rarity", "Rareté", "", "");
		public final Class<?> filter;
		public final String name_EN, name_FR, name_DE, name_IT;

		FilterType(Class<?> filter, String name_EN, String name_FR, String name_DE, String name_IT)
		{
			this.filter = filter;
			this.name_EN = name_EN;
			this.name_FR = name_FR;
			this.name_DE = name_DE;
			this.name_IT = name_IT;
		}

		@Override
		public String toString() { return getTranslatedName(); }

		public String getTranslatedName() { return name_FR; }
	}
}