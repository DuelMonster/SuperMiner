package duelmonster.superminer.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import duelmonster.superminer.network.packets.SMPacketBase;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class PacketHandler extends SimpleChannelInboundHandler<SMPacketBase> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SMPacketBase msg) throws Exception {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			msg.doStuffServer(ctx);
	}
}
