package duelmonster.superminer.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import duelmonster.superminer.network.packets.SMPacketBase;

@ChannelHandler.Sharable
public class PacketHandler extends SimpleChannelInboundHandler<SMPacketBase> {
	protected void channelRead0(ChannelHandlerContext ctx, SMPacketBase msg) throws Exception {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			msg.doStuffServer(ctx);
	}
}
