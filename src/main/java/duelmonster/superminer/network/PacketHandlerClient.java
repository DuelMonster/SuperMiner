package duelmonster.superminer.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import duelmonster.superminer.network.packets.SMPacketBase;

@io.netty.channel.ChannelHandler.Sharable
@SideOnly(Side.CLIENT)
public class PacketHandlerClient extends PacketHandler {
	protected void channelRead0(ChannelHandlerContext ctx, SMPacketBase msg) throws Exception {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			msg.doStuffServer(ctx);
		else
			msg.doStuffClient();
	}
}
