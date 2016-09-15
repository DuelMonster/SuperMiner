package duelmonster.superminer.network.packets;

import java.util.LinkedList;

import duelmonster.superminer.util.BlockPos;
import duelmonster.superminer.util.EnumFacing;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;

public class SMPacket {
	public int packetID = PacketIDs.BLOCKINFO.value();
	public long nanoTime = 0;
	public BlockPos oPos;
	public EnumFacing sideHit;
	public Block prevBlock = null;
	public int prevMetadata = 0;
	public Block block = null;
	public int metadata = 0;
	public int count = 0;
	public boolean flag_rs = false;
	public boolean bLayerOnlyToggled = false;
	public int playerID = 0;
	public LinkedList<BlockPos> positions = new LinkedList<BlockPos>();

	public String sWoodName = "";
	public String sLeafName = "";
	
	public void readPacketData(PacketBuffer oBuffer) {
		this.packetID = oBuffer.readInt();
		this.oPos = BlockPos.fromLong(oBuffer.readLong());
		this.sideHit = EnumFacing.getFront(oBuffer.readInt());
		this.prevBlock = Block.getBlockById(oBuffer.readInt());
		this.block = Block.getBlockById(oBuffer.readInt());
		this.metadata = oBuffer.readInt();
		this.flag_rs = oBuffer.readBoolean();
		this.bLayerOnlyToggled = oBuffer.readBoolean();
		this.playerID = oBuffer.readInt();
	}

	public PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());

		oBuffer.writeInt(this.packetID);
		oBuffer.writeLong(this.oPos.toLong());
		oBuffer.writeInt(this.sideHit.getIndex());
		oBuffer.writeInt(Block.getIdFromBlock(this.prevBlock));
		oBuffer.writeInt(Block.getIdFromBlock(this.block));
		oBuffer.writeInt(this.metadata);
		oBuffer.writeBoolean(this.flag_rs);
		oBuffer.writeBoolean(this.bLayerOnlyToggled);
		oBuffer.writeInt(this.playerID);

		return oBuffer;
	}
	
	public SMPacket getClone(){
		SMPacket pClone = new SMPacket();
		pClone.readPacketData(writePacketData());
		return pClone;
	}
}
