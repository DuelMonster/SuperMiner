package duelmonster.superminer.network.packets;

import java.util.LinkedList;

import duelmonster.superminer.util.BlockPos;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class AutoFurrowPacket {
	public int packetID = -99;
	
	public BlockPos				oPos;
	public LinkedList<BlockPos>	lstPositions	= new LinkedList<BlockPos>();
	
	public AutoFurrowPacket() {}
	
	public AutoFurrowPacket(int iPacketID, BlockPos oPos) {
		this.packetID = iPacketID;
		this.oPos = oPos;
	}
	
	public void readPacketData(PacketBuffer oBuffer) {
		this.packetID = oBuffer.readInt();
		this.oPos = BlockPos.fromLong(oBuffer.readLong());
	}
	
	public PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(this.packetID);
		oBuffer.writeLong(this.oPos.toLong());
		
		return oBuffer;
	}
	
	public AutoFurrowPacket getClone() {
		AutoFurrowPacket pClone = new AutoFurrowPacket(this.packetID, this.oPos);
		pClone.readPacketData(writePacketData());
		return pClone;
	}
	
	public boolean isPositionConnected(BlockPos sourcePos) {
		if (lstPositions.size() > 0) {
			for (int xOffset = -1; xOffset <= 1; xOffset++)
				for (int zOffset = -1; zOffset <= 1; zOffset++)
					for (int yOffset = -1; yOffset <= 1; yOffset++) {
						BlockPos comparePos = new BlockPos(sourcePos.getX() + xOffset, sourcePos.getY() + yOffset, sourcePos.getZ() + zOffset);
						if (!sourcePos.equals(comparePos))
							for (BlockPos oPos : lstPositions)
							if (comparePos.equals(oPos))
								return true;
					}
				
			return false;
		} else
			return true;
	}
}
