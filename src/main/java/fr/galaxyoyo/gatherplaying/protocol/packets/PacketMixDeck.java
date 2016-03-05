package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.MySQL.Condition;
import fr.galaxyoyo.gatherplaying.MySQL.Value;
import io.netty.buffer.ByteBuf;
import java8.util.stream.StreamSupport;

public class PacketMixDeck extends Packet
{
	public Type type;
	public Deck deck;

	@Override
	public void read(ByteBuf buf)
	{
		type = Type.values()[buf.readByte()];
		deck = new Deck();
		deck.uuid = readUUID(buf);
		if (type == Type.DELETING)
		{
			player.decks.remove(deck);
			MySQL.delete("decks", Condition.equals(new Value("uuid", deck.uuid)));
			return;
		}
		deck.free = buf.readBoolean();
		deck.name.setValue(readUTF(buf));
		deck.desc = readUTF(buf);
		int size = buf.readInt();
		for (int i = 0; i < size; ++i)
			deck.cards.add(new OwnedCard(readCard(buf), player, buf.readBoolean()));
		size = buf.readByte();
		for (int i = 0; i < size; ++i)
			deck.sideboard.add(new OwnedCard(readCard(buf), player, buf.readBoolean()));
		size = buf.readByte();
		deck.colors = new ManaColor[size];
		for (int i = 0; i < size; ++i)
			deck.colors[i] = ManaColor.values()[buf.readByte()];
		size = buf.readByte();
		for (int i = 0; i < size; ++i)
			deck.legalities.add(Rules.values()[buf.readByte()]);
		deck.owner = player;
		if (type == Type.CREATING)
			player.decks.add(deck);
		else
			StreamSupport.stream(player.decks).filter(deck -> deck.uuid.equals(this.deck.uuid)).findAny().get().importDeck(deck);
		if (Utils.getSide() == Side.SERVER)
			MySQL.saveDeck(deck);
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(type.ordinal());
		writeUUID(deck.uuid, buf);
		if (type == Type.DELETING)
			return;
		buf.writeBoolean(deck.free);
		writeUTF(deck.name.getValue(), buf);
		writeUTF(deck.desc, buf);
		buf.writeInt(deck.cards.size());
		for (OwnedCard card : deck.cards)
		{
			writeCard(card.getCard(), buf);
			buf.writeBoolean(card.isFoiled());
		}
		buf.writeByte(deck.sideboard.size());
		for (OwnedCard card : deck.sideboard)
		{
			writeCard(card.getCard(), buf);
			buf.writeBoolean(card.isFoiled());
		}
		buf.writeByte(deck.colors.length);
		for (ManaColor color : deck.colors)
			buf.writeByte(color.ordinal());
		buf.writeByte(deck.legalities.size());
		for (Rules rule : deck.legalities)
			buf.writeByte(rule.ordinal());
	}

	public enum Type
	{
		CREATING, EDITING, DELETING
	}
}