package duelmonster.superminer.config;

import java.util.ArrayList;
import java.util.Arrays;

import duelmonster.superminer.network.packets.PacketIDs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class SettingsVeinator {
	public static final int packetID = PacketIDs.Settings_Veinator.value();

	public final static boolean bEnabledDefault = true;
	public final static boolean bGatherDropsDefault = false;
	public final static ArrayList<String> lToolIDDefaults = new ArrayList<String>(Arrays.asList( "wooden_pickaxe", "stone_pickaxe", "iron_pickaxe", "golden_pickaxe", "diamond_pickaxe" ));
	public final static ArrayList<String> lOreIDDefaults = new ArrayList<String>(Arrays.asList( "coal_ore", "iron_ore", "gold_ore", "lapis_ore", "emerald_ore", "diamond_ore", "quartz_ore", "redstone_ore", "lit_redstone_ore" ));
		
	public static boolean bEnabled = bEnabledDefault;
	public static boolean bGatherDrops = bGatherDropsDefault;
	public static ArrayList<String> lToolIDs = lToolIDDefaults;
	public static ArrayList<String> lOreIDs = lOreIDDefaults;
	
	public static void readPacketData(PacketBuffer oBuffer) {
		@SuppressWarnings("unused")
		int id = oBuffer.readInt();
		
		SettingsVeinator.bEnabled = oBuffer.readBoolean();
		SettingsVeinator.bGatherDrops = oBuffer.readBoolean();
		
		int iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsVeinator.lToolIDs = new ArrayList<String>(iCount);
			for (int i = 0; i < iCount; i++)
				SettingsVeinator.lToolIDs.add(i, oBuffer.readStringFromBuffer(32767));
		}
		
		iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsVeinator.lOreIDs = new ArrayList<String>(iCount);
			for (int i = 0; i < iCount; i++)
				SettingsVeinator.lOreIDs.add(i, oBuffer.readStringFromBuffer(32767));
		}
	}

	public static PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(packetID);
		
		oBuffer.writeBoolean(SettingsVeinator.bEnabled);
		oBuffer.writeBoolean(SettingsVeinator.bGatherDrops);
		
		int iCount = SettingsVeinator.lToolIDs.size();
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
				oBuffer.writeString(SettingsVeinator.lToolIDs.get(i));
		
		iCount = SettingsVeinator.lOreIDs.size();
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
				oBuffer.writeString(SettingsVeinator.lOreIDs.get(i));

		return oBuffer;
	}
}
