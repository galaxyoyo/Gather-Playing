package fr.galaxyoyo.gatherplaying.protocol.packets;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.MySQL;
import fr.galaxyoyo.gatherplaying.Player;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public abstract class Packet implements Cloneable
{
	protected Player player;

	protected Packet() { }

	public abstract void read(ByteBuf buf);

	public abstract void write(ByteBuf buf);

	protected void sendToParty() { PacketManager.sendPacketToParty(player.runningParty, this); }

	public final UUID readUUID(ByteBuf buf) { return new UUID(buf.readLong(), buf.readLong()); }

	public final void writeUUID(UUID uuid, ByteBuf buf)
	{
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
	}

	public final Card readCard(ByteBuf buf) { return MySQL.getCard(readUTF(buf)); }

	public final String readUTF(ByteBuf buf)
	{
		short length = buf.readShort();
		if (length < 0)
			return null;
		byte[] array = new byte[length];
		buf.readBytes(array);
		return new String(array, StandardCharsets.UTF_8);
	}

	public final void writeCard(Card card, ByteBuf buf) { writeUTF(card.getMuId("en"), buf); }

	public final void writeUTF(String utf, ByteBuf buf)
	{
		if (utf == null)
		{
			buf.writeShort(-1);
			return;
		}
		byte[] array = utf.getBytes(StandardCharsets.UTF_8);
		if (array.length > Short.MAX_VALUE)
			throw new IllegalArgumentException("length must be <= " + Short.MAX_VALUE);
		buf.writeShort(array.length);
		buf.writeBytes(array);
	}

	public Player getPlayer() { return player; }

	public <T extends Packet> T createPacket() { return (T) PacketManager.createPacket(getClass()); }

	@Override
	public String toString()
	{
		ToStringHelper helper = MoreObjects.toStringHelper(getClass());
		for (Field field : getClass().getDeclaredFields())
		{
			try
			{
				helper.add(field.getName(), field.get(this));
			} catch (IllegalArgumentException | IllegalAccessException ex)
			{
				ex.printStackTrace();
			}
		}
		return helper.toString();
	}
}