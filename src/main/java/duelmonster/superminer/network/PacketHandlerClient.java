package duelmonster.superminer.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import duelmonster.superminer.network.packets.SMPacketBase;
import io.netty.channel.ChannelHandlerContext;

@io.netty.channel.ChannelHandler.Sharable
@SideOnly(Side.CLIENT)
public class PacketHandlerClient extends PacketHandler {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SMPacketBase msg) throws Exception {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			msg.doStuffServer(ctx);
		else
			msg.doStuffClient();
	}
}
