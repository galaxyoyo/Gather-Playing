package fr.galaxyoyo.gatherplaying.protocol.packets;

import fr.galaxyoyo.gatherplaying.*;
import fr.galaxyoyo.gatherplaying.MySQL.Condition;
import fr.galaxyoyo.gatherplaying.MySQL.Value;
import io.netty.buffer.ByteBuf;

public class PacketMixDeck extends Packet
{
	public Type type;
	public Deck deck;

	@Override
	public void read(ByteBuf buf)
	{
		type = Type.values()[buf.readByte()];
		deck = new Deck();
		deck.setUuid(readUUID(buf));
		if (type == Type.DELETING)
		{
			player.decks.remove(deck);
			MySQL.delete("decks", Condition.equals(new Value("uuid", deck.getUuid())));
			return;
		}
		deck.setFree(buf.readBoolean());
		deck.setName(readUTF(buf));
		deck.setDesc(readUTF(buf));
		int size = buf.readInt();
		for (int i = 0; i < size; ++i)
			deck.getCards().add(new OwnedCard(readCard(buf), player, buf.readBoolean()));
		size = buf.readByte();
		for (int i = 0; i < size; ++i)
			deck.getSideboard().add(new OwnedCard(readCard(buf), player, buf.readBoolean()));
		size = buf.readByte();
		deck.setColors(new ManaColor[size]);
		for (int i = 0; i < size; ++i)
			deck.getColors()[i] = ManaColor.values()[buf.readByte()];
		size = buf.readByte();
		for (int i = 0; i < size; ++i)
			deck.getLegalities().add(Rules.values()[buf.readByte()]);
		deck.setOwner(player);
		if (type == Type.CREATING)
			player.decks.add(deck);
		else
			player.decks.stream().filter(deck -> deck.getUuid().equals(this.deck.getUuid())).findAny().get().importDeck(deck);
		if (Utils.getSide() == Side.SERVER)
			MySQL.saveDeck(deck);
	}

	@Override
	public void write(ByteBuf buf)
	{
		buf.writeByte(type.ordinal());
		writeUUID(deck.getUuid(), buf);
		if (type == Type.DELETING)
			return;
		buf.writeBoolean(deck.isFree());
		writeUTF(deck.getName(), buf);
		writeUTF(deck.getDesc(), buf);
		buf.writeInt(deck.getCards().size());
		for (OwnedCard card : deck.getCards())
		{
			writeCard(card.getCard(), buf);
			buf.writeBoolean(card.isFoiled());
		}
		buf.writeByte(deck.getSideboard().size());
		for (OwnedCard card : deck.getSideboard())
		{
			writeCard(card.getCard(), buf);
			buf.writeBoolean(card.isFoiled());
		}
		buf.writeByte(deck.getColors().length);
		for (ManaColor color : deck.getColors())
			buf.writeByte(color.ordinal());
		buf.writeByte(deck.getLegalities().size());
		for (Rules rule : deck.getLegalities())
			buf.writeByte(rule.ordinal());
	}

	public enum Type
	{
		CREATING, EDITING, DELETING
	}
}