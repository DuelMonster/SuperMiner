package duelmonster.superminer.config;

import duelmonster.superminer.network.packets.PacketIDs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class SettingsIlluminator {
	public static final int packetID = PacketIDs.Settings_Illuminator.value();
	
	public final static boolean	bEnabledDefault				= true;
	public final static int		iLowestLightLevelDefault	= 7;
	
	public static boolean	bEnabled			= bEnabledDefault;
	public static int		iLowestLightLevel	= iLowestLightLevelDefault;
	
	public static void readPacketData(PacketBuffer oBuffer) {
		@SuppressWarnings("unused")
		int id = oBuffer.readInt();
		
		SettingsIlluminator.bEnabled = oBuffer.readBoolean();
		SettingsIlluminator.iLowestLightLevel = oBuffer.readInt();
	}
	
	public static PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(packetID);
		
		oBuffer.writeBoolean(SettingsIlluminator.bEnabled);
		oBuffer.writeInt(SettingsIlluminator.iLowestLightLevel);
		
		return oBuffer;
	}
}
