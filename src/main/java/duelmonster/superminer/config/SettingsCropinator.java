package duelmonster.superminer.config;

import java.util.ArrayList;
import java.util.Arrays;

import duelmonster.superminer.network.packets.PacketIDs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class SettingsCropinator {
	public static final int packetID = PacketIDs.Settings_Cropinator.value();
	
	public final static boolean				bEnabledDefault			= true;
	public final static boolean				bHarvestSeedsDefault	= false;
	public final static ArrayList<String>	lHoeIDDefaults			= new ArrayList<String>(Arrays.asList("Minecraft:wooden_hoe", "Minecraft:stone_hoe", "Minecraft:iron_hoe", "Minecraft:golden_hoe", "Minecraft:diamond_hoe"));
	
	public static boolean			bEnabled		= bEnabledDefault;
	public static boolean			bHarvestSeeds	= bHarvestSeedsDefault;
	public static ArrayList<String>	lHoeIDs			= lHoeIDDefaults;
	
	public static void readPacketData(PacketBuffer oBuffer) {
		@SuppressWarnings("unused")
		int id = oBuffer.readInt();
		
		SettingsCropinator.bEnabled = oBuffer.readBoolean();
		SettingsCropinator.bHarvestSeeds = oBuffer.readBoolean();
		
		int iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsCropinator.lHoeIDs = new ArrayList<String>(iCount);
			for (int i = 0; i < iCount; i++)
				SettingsCropinator.lHoeIDs.add(i, oBuffer.readString(32767));
		}
	}
	
	public static PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(packetID);
		
		oBuffer.writeBoolean(SettingsCropinator.bEnabled);
		oBuffer.writeBoolean(SettingsCropinator.bHarvestSeeds);
		
		int iCount = SettingsCropinator.lHoeIDs.size();
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
			oBuffer.writeString(SettingsCropinator.lHoeIDs.get(i));
		
		return oBuffer;
	}
}
