package duelmonster.superminer.network;

import duelmonster.superminer.network.packets.SMPacketBase;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
