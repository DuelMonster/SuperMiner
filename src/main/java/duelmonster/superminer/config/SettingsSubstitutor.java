package duelmonster.superminer.config;

import duelmonster.superminer.network.packets.PacketIDs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class SettingsSubstitutor {
	public static final int packetID = PacketIDs.Settings_Substitutor.value();

	public final static boolean bEnabledDefault = true;
	public final static boolean bSwitchbackEnabledDefault = true;
	public final static boolean bFavourSilkTouchDefault = true;
	public final static boolean bIgnoreIfValidToolDefault = true;
	public final static boolean bIgnorePassiveMobsDefault = true;
	public final static String[] saExcludedBlockIDDefaults = { 
													"minecraft:chest", 
													"minecraft:trapped_chest", 
													"JABBA:barrel", 
													"StorageDrawers:fullDrawers1", 
													"StorageDrawers:fullDrawers2", 
													"StorageDrawers:fullDrawers4", 
													"StorageDrawers:halfDrawers2", 
													"StorageDrawers:halfDrawers4", 
													"StorageDrawers:trim", 
													"StorageDrawers:controller", 
													"StorageDrawers:controllerSlave", 
													"StorageDrawers:compDrawers"
												};
	
	public static boolean bEnabled = bEnabledDefault;
	public static boolean bSwitchbackEnabled = bSwitchbackEnabledDefault;
	public static boolean bFavourSilkTouch = bFavourSilkTouchDefault;
	public static boolean bIgnoreIfValidTool = bIgnoreIfValidToolDefault;
	public static boolean bIgnorePassiveMobs = bIgnorePassiveMobsDefault;
	public static String[] saExcludedBlockIDs = saExcludedBlockIDDefaults;
			
	public static void readPacketData(PacketBuffer oBuffer) {
		@SuppressWarnings("unused")
		int id = oBuffer.readInt();
		
		SettingsSubstitutor.bEnabled = oBuffer.readBoolean();
		SettingsSubstitutor.bSwitchbackEnabled = oBuffer.readBoolean();
		SettingsSubstitutor.bFavourSilkTouch = oBuffer.readBoolean();
		SettingsSubstitutor.bIgnoreIfValidTool = oBuffer.readBoolean();
		SettingsSubstitutor.bIgnorePassiveMobs = oBuffer.readBoolean();
		
		int iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsSubstitutor.saExcludedBlockIDs = new String[iCount];
			for (int i = 0; i < iCount; i++)
				SettingsSubstitutor.saExcludedBlockIDs[i] = oBuffer.readString(32767);
		}
	}

	public static PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(packetID);
		
		oBuffer.writeBoolean(SettingsSubstitutor.bEnabled);
		oBuffer.writeBoolean(SettingsSubstitutor.bSwitchbackEnabled);
		oBuffer.writeBoolean(SettingsSubstitutor.bFavourSilkTouch);
		oBuffer.writeBoolean(SettingsSubstitutor.bIgnoreIfValidTool);
		oBuffer.writeBoolean(SettingsSubstitutor.bIgnorePassiveMobs);
		
		int iCount = SettingsSubstitutor.saExcludedBlockIDs.length;
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
				oBuffer.writeString(SettingsSubstitutor.saExcludedBlockIDs[i]);

		return oBuffer;
	}
}
