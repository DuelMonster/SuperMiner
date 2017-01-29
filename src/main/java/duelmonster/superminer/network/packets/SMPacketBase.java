package duelmonster.superminer.network.packets;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class SMPacketBase {
	public abstract void writeData(ByteBuf paramByteBuf) throws Exception;
	
	public void writeVec(ByteBuf data, Vec3d Vec3d) {
		data.writeFloat((float) Vec3d.xCoord);
		data.writeFloat((float) Vec3d.yCoord);
		data.writeFloat((float) Vec3d.zCoord);
	}
	
	public void writeChatComponent(ByteBuf data, ITextComponent chatComponent) {
		writeString(data, ITextComponent.Serializer.componentToJson(chatComponent));
	}
	
	public ITextComponent readChatComponent(ByteBuf data) {
		return ITextComponent.Serializer.jsonToComponent(readString(data));
	}
	
	public abstract void readData(EntityPlayer paramEntityPlayer, ByteBuf paramByteBuf);
	
	public Vec3d readVec(ByteBuf data) {
		return new Vec3d(data.readFloat(), data.readFloat(), data.readFloat());
	}
	
	public abstract void doStuffServer(ChannelHandlerContext paramChannelHandlerContext);
	
	@SideOnly(Side.CLIENT)
	public abstract void doStuffClient();
	
	public abstract boolean isValidSenderSide(Side paramSide);
	
	public void writeString(ByteBuf data, String string) {
		byte[] stringData = string.getBytes(Charset.forName("UTF-8"));
		
		data.writeShort(stringData.length);
		data.writeBytes(stringData);
	}
	
	public String readString(ByteBuf data) {
		short length = data.readShort();
		byte[] bytes = new byte[length];
		
		data.readBytes(bytes);
		return new String(bytes, Charset.forName("UTF-8"));
	}
}
