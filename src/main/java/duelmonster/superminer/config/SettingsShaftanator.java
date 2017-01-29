package duelmonster.superminer.config;

import duelmonster.superminer.network.packets.PacketIDs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class SettingsShaftanator {
	public static final int packetID = PacketIDs.Settings_Shaftanator.value();
	
	public final static boolean	bEnabledDefault			= true;
	public final static boolean	bGatherDropsDefault		= true;
	public final static boolean	bAutoIlluminateDefault	= true;
	public final static boolean	bMineVeinsDefault		= false;
	public final static int		iShaftLengthDefault		= 16;
	public final static int		iShaftHeightDefault		= 2;
	public final static int		iShaftWidthDefault		= 1;
	
	public static boolean	bEnabled		= bEnabledDefault;
	public static boolean	bGatherDrops	= bGatherDropsDefault;
	public static boolean	bAutoIlluminate	= bAutoIlluminateDefault;
	public static boolean	bMineVeins		= bMineVeinsDefault;
	public static int		iShaftLength	= iShaftLengthDefault;
	public static int		iShaftHeight	= iShaftHeightDefault;
	public static int		iShaftWidth		= iShaftWidthDefault;
	
	public static void readPacketData(PacketBuffer oBuffer) {
		@SuppressWarnings("unused")
		int id = oBuffer.readInt();
		
		SettingsShaftanator.bEnabled = oBuffer.readBoolean();
		SettingsShaftanator.bGatherDrops = oBuffer.readBoolean();
		SettingsShaftanator.bAutoIlluminate = oBuffer.readBoolean();
		SettingsShaftanator.bMineVeins = oBuffer.readBoolean();
		SettingsShaftanator.iShaftLength = oBuffer.readInt();
		SettingsShaftanator.iShaftHeight = oBuffer.readInt();
		SettingsShaftanator.iShaftWidth = oBuffer.readInt();
	}
	
	public static PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(packetID);
		
		oBuffer.writeBoolean(SettingsShaftanator.bEnabled);
		oBuffer.writeBoolean(SettingsShaftanator.bGatherDrops);
		oBuffer.writeBoolean(SettingsShaftanator.bAutoIlluminate);
		oBuffer.writeBoolean(SettingsShaftanator.bMineVeins);
		oBuffer.writeInt(SettingsShaftanator.iShaftLength);
		oBuffer.writeInt(SettingsShaftanator.iShaftHeight);
		oBuffer.writeInt(SettingsShaftanator.iShaftWidth);
		
		return oBuffer;
	}
}
