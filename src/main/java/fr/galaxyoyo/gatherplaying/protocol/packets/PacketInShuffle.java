package fr.galaxyoyo.gatherplaying.protocol.packets;

import io.netty.buffer.ByteBuf;

public class PacketInShuffle extends Packet
{
	@Override
	public void read(ByteBuf buf) { player.getData().getLibrary().shuffle(); }

	@Override
	public void write(ByteBuf buf) { }
}