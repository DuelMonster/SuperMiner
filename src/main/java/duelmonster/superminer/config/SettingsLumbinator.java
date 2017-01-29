package duelmonster.superminer.config;

import java.util.ArrayList;
import java.util.Arrays;

import duelmonster.superminer.network.packets.PacketIDs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class SettingsLumbinator {
	public static final int packetID = PacketIDs.Settings_Lumbinator.value();
	
	public final static boolean				bEnabledDefault					= true;
	public final static boolean				bGatherDropsDefault				= false;
	public final static boolean				bChopTreeBelowDefault			= false;
	public final static boolean				bDestroyLeavesDefault			= true;
	public final static boolean				bLeavesAffectDurabilityDefault	= false;
	public final static int					iLeafRangeDefault				= 3;
	public final static ArrayList<String>	lToolIDDefaults					= new ArrayList<String>(Arrays.asList("wooden_axe", "stone_axe", "iron_axe", "golden_axe", "diamond_axe"));
	public final static ArrayList<String>	lWoodIDDefaults					= new ArrayList<String>(Arrays.asList("log", "log2", "brown_mushroom_block", "red_mushroom_block"));
	public final static ArrayList<String>	lLeafIDDefaults					= new ArrayList<String>(Arrays.asList("leaves", "leaves2"));
	
	public static boolean			bEnabled				= bEnabledDefault;
	public static boolean			bGatherDrops			= bGatherDropsDefault;
	public static boolean			bChopTreeBelow			= bChopTreeBelowDefault;
	public static boolean			bDestroyLeaves			= bDestroyLeavesDefault;
	public static boolean			bLeavesAffectDurability	= bLeavesAffectDurabilityDefault;
	public static int				iLeafRange				= iLeafRangeDefault;
	public static ArrayList<String>	lToolIDs				= lToolIDDefaults;
	public static ArrayList<String>	lWoodIDs				= lWoodIDDefaults;
	public static ArrayList<String>	lLeafIDs				= lLeafIDDefaults;
	
	public static void readPacketData(PacketBuffer oBuffer) {
		@SuppressWarnings("unused")
		int id = oBuffer.readInt();
		
		SettingsLumbinator.bEnabled = oBuffer.readBoolean();
		SettingsLumbinator.bGatherDrops = oBuffer.readBoolean();
		SettingsLumbinator.bChopTreeBelow = oBuffer.readBoolean();
		SettingsLumbinator.bDestroyLeaves = oBuffer.readBoolean();
		SettingsLumbinator.bLeavesAffectDurability = oBuffer.readBoolean();
		SettingsLumbinator.iLeafRange = oBuffer.readInt();
		
		int iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsLumbinator.lToolIDs = new ArrayList<String>(iCount);
			for (int i = 0; i < iCount; i++)
				SettingsLumbinator.lToolIDs.add(i, oBuffer.readString(32767));
		}
		
		iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsLumbinator.lWoodIDs = new ArrayList<String>(iCount);
			for (int i = 0; i < iCount; i++)
				SettingsLumbinator.lWoodIDs.add(i, oBuffer.readString(32767));
		}
		
		iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsLumbinator.lLeafIDs = new ArrayList<String>(iCount);
			for (int i = 0; i < iCount; i++)
				SettingsLumbinator.lLeafIDs.add(i, oBuffer.readString(32767));
		}
	}
	
	public static PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(packetID);
		
		oBuffer.writeBoolean(SettingsLumbinator.bEnabled);
		oBuffer.writeBoolean(SettingsLumbinator.bGatherDrops);
		oBuffer.writeBoolean(SettingsLumbinator.bChopTreeBelow);
		oBuffer.writeBoolean(SettingsLumbinator.bDestroyLeaves);
		oBuffer.writeBoolean(SettingsLumbinator.bLeavesAffectDurability);
		oBuffer.writeInt(SettingsLumbinator.iLeafRange);
		
		int iCount = SettingsLumbinator.lToolIDs.size();
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
			oBuffer.writeString(SettingsLumbinator.lToolIDs.get(i));
		
		iCount = SettingsLumbinator.lWoodIDs.size();
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
			oBuffer.writeString(SettingsLumbinator.lWoodIDs.get(i));
		
		iCount = SettingsLumbinator.lLeafIDs.size();
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
			oBuffer.writeString(SettingsLumbinator.lLeafIDs.get(i));
		
		return oBuffer;
	}
}
