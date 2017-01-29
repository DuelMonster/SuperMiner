package duelmonster.superminer.network.packets;

import duelmonster.superminer.util.BlockPos;
import duelmonster.superminer.util.EnumFacing;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class IlluminatorPacket {
	public static final int PACKETID_ILLUMINATOR = 1;
	
	public int			packetID	= IlluminatorPacket.PACKETID_ILLUMINATOR;
	public BlockPos		oPos;
	public EnumFacing	sideHit		= EnumFacing.UP;
	public int			playerID	= 0;
	
	public IlluminatorPacket() {}
	
	public IlluminatorPacket(BlockPos oPos) {
		this.oPos = oPos;
	}
	
	public IlluminatorPacket(BlockPos oPos, EnumFacing sideHit) {
		this.oPos = oPos;
		this.sideHit = sideHit;
	}
	
	public void readPacketData(PacketBuffer oBuffer) {
		this.packetID = oBuffer.readInt();
		this.oPos = BlockPos.fromLong(oBuffer.readLong());
		this.sideHit = EnumFacing.getFront(oBuffer.readInt());
		this.playerID = oBuffer.readInt();
	}
	
	public PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(this.packetID);
		oBuffer.writeLong(this.oPos.toLong());
		oBuffer.writeInt(this.sideHit.getIndex());
		oBuffer.writeInt(this.playerID);
		
		return oBuffer;
	}
	
	public IlluminatorPacket getClone() {
		IlluminatorPacket pClone = new IlluminatorPacket();
		pClone.readPacketData(writePacketData());
		return pClone;
	}
}
