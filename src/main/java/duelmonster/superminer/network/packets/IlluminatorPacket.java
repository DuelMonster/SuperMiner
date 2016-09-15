package duelmonster.superminer.network.packets;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;

public class IlluminatorPacket {
	public static final int PACKETID_ILLUMINATOR = 1;
	
	public int packetID = IlluminatorPacket.PACKETID_ILLUMINATOR;
	public BlockPos oPos;
	public int playerID = 0;
	
	public IlluminatorPacket() {}
	public IlluminatorPacket(BlockPos oPos) { this.oPos = oPos; }
		
	public void readPacketData(PacketBuffer oBuffer) {
		this.packetID = oBuffer.readInt();
		this.oPos = BlockPos.fromLong(oBuffer.readLong());
		this.playerID = oBuffer.readInt();
	}

	public PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(this.packetID);
		oBuffer.writeLong(this.oPos.toLong());
		oBuffer.writeInt(this.playerID);

		return oBuffer;
	}
	
	public IlluminatorPacket getClone(){
		IlluminatorPacket pClone = new IlluminatorPacket();
		pClone.readPacketData(writePacketData());
		return pClone;
	}
}
