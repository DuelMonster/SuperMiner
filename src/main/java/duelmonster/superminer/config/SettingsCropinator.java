package duelmonster.superminer.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import duelmonster.superminer.network.packets.PacketIDs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class SettingsCropinator {
	public static final int packetID = PacketIDs.Settings_Cropinator.value();
	
	public final static boolean				bEnabledDefault			= true;
	public final static boolean				bHarvestSeedsDefault	= false;
	public final static ArrayList<String>	lHoeIDDefaults			= new ArrayList<String>(Arrays.asList("minecraft:wooden_hoe", "minecraft:stone_hoe", "minecraft:iron_hoe", "minecraft:golden_hoe", "minecraft:diamond_hoe"));
	
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
				try {
					SettingsCropinator.lHoeIDs.add(i, oBuffer.readStringFromBuffer(32767));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
			try {
			oBuffer.writeStringToBuffer(SettingsCropinator.lHoeIDs.get(i));
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		return oBuffer;
	}
}
