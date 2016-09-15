package duelmonster.superminer.network.packets;

import java.nio.charset.Charset;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;

public abstract class SMPacketBase {
	public abstract void writeData(ByteBuf paramByteBuf) throws Exception;
	
	public void writeVec(ByteBuf data, Vec3 vec3) {
		data.writeFloat((float)vec3.xCoord);
		data.writeFloat((float)vec3.yCoord);
		data.writeFloat((float)vec3.zCoord);
	}
	
	public void writeChatComponent(ByteBuf data, IChatComponent chatComponent) {
		writeString(data, IChatComponent.Serializer.func_150696_a(chatComponent));
	}
	
	public IChatComponent readChatComponent(ByteBuf data) {
		return IChatComponent.Serializer.func_150699_a(readString(data));
	}
	
	public abstract void readData(EntityPlayer paramEntityPlayer, ByteBuf paramByteBuf);
	
	public Vec3 readVec(ByteBuf data) {
		return Vec3.createVectorHelper(data.readFloat(), data.readFloat(), data.readFloat());
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
