package duelmonster.superminer.config;

import duelmonster.superminer.network.packets.PacketIDs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class SettingsCaptivator {
	public static final int packetID = PacketIDs.Settings_Captivator.value();
	
	public final static boolean		bEnabledDefault		= true;
	public final static boolean		bAllowInGUIDefault	= true;
	public final static float		fHorizontalDefault	= 16.0F;
	public final static float		fVerticalDefault	= 16.0F;
	public final static boolean		bIsWhitelistDefault	= true;
	public final static String[]	lItemIDDefaults		= {};
	
	public static boolean	bEnabled		= bEnabledDefault;
	public static boolean	bAllowInGUI		= bAllowInGUIDefault;
	public static float		fHorizontal		= fHorizontalDefault;
	public static float		fVertical		= fVerticalDefault;
	public static boolean	bIsWhitelist	= bIsWhitelistDefault;
	public static String[]	lItemIDs		= lItemIDDefaults;
	
	public static void readPacketData(PacketBuffer oBuffer) {
		@SuppressWarnings("unused")
		int id = oBuffer.readInt();
		
		SettingsCaptivator.bEnabled = oBuffer.readBoolean();
		SettingsCaptivator.bAllowInGUI = oBuffer.readBoolean();
		SettingsCaptivator.fHorizontal = oBuffer.readFloat();
		SettingsCaptivator.fVertical = oBuffer.readFloat();
		
		int iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsCaptivator.lItemIDs = new String[iCount];
			for (int i = 0; i < iCount; i++)
				SettingsCaptivator.lItemIDs[i] = oBuffer.readString(32767);
		}
	}
	
	public static PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(packetID);
		
		oBuffer.writeBoolean(SettingsCaptivator.bEnabled);
		oBuffer.writeBoolean(SettingsCaptivator.bAllowInGUI);
		oBuffer.writeFloat(SettingsCaptivator.fHorizontal);
		oBuffer.writeFloat(SettingsCaptivator.fVertical);
		
		int iCount = SettingsCaptivator.lItemIDs.length;
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
			oBuffer.writeString(SettingsCaptivator.lItemIDs[i]);
		
		return oBuffer;
	}
}
