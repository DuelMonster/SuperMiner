package duelmonster.superminer.config;

import java.util.ArrayList;
import java.util.Arrays;

import duelmonster.superminer.network.packets.PacketIDs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class SettingsExcavator {
	public static final int packetID = PacketIDs.Settings_Excavator.value();

	public static final int MIN_BlockRadius = 3;
	public static final int MAX_BlockRadius = 16;
	
	public final static boolean bEnabledDefault = true;
	public final static boolean bGatherDropsDefault = true;
	public final static boolean bAutoIlluminateDefault = true;
	//public final static boolean bMineVeinsDefault = true;
	public final static boolean bDetectVariantsDefault = true;
	public final static ArrayList<String> lToolIDDefaults = new ArrayList<String>();
	public final static ArrayList<String> lShovelIDDefaults = new ArrayList<String>(Arrays.asList( "Minecraft:wooden_shovel", "Minecraft:stone_shovel", "Minecraft:iron_shovel", "Minecraft:golden_shovel", "Minecraft:diamond_shovel" ));
	public final static int iAffectDurabilityDefault = 0;
	public final static int iBlockRadiusDefault = SettingsExcavator.MIN_BlockRadius;
	public final static int iBlockLimitDefault = (SettingsExcavator.MIN_BlockRadius * SettingsExcavator.MIN_BlockRadius) * SettingsExcavator.MIN_BlockRadius;
	public final static int iPathWidthDefault = 3;
	public final static int iPathLengthDefault = 6;
	
	public static boolean bEnabled = bEnabledDefault;
	public static boolean bGatherDrops = bGatherDropsDefault;
	public static boolean bAutoIlluminate = bAutoIlluminateDefault;
	//public static boolean bMineVeins = bMineVeinsDefault;
	public static boolean bDetectVariants = bDetectVariantsDefault;
	public static ArrayList<String> lToolIDs = lToolIDDefaults;
	public static ArrayList<String> lShovelIDs = lShovelIDDefaults;
	public static int iAffectDurability = iAffectDurabilityDefault;
	public static int iBlockRadius = iBlockRadiusDefault;
	public static int iBlockLimit = iBlockLimitDefault;
	public static int iPathWidth = iPathWidthDefault;
	public static int iPathLength = iPathLengthDefault;
	
	public static void readPacketData(PacketBuffer oBuffer) {
		@SuppressWarnings("unused")
		int id = oBuffer.readInt();
		
		SettingsExcavator.bEnabled = oBuffer.readBoolean();
		SettingsExcavator.bGatherDrops = oBuffer.readBoolean();
		SettingsExcavator.bAutoIlluminate = oBuffer.readBoolean();
		//SettingsExcavator.bMineVeins = oBuffer.readBoolean();
		SettingsExcavator.bDetectVariants = oBuffer.readBoolean();
		SettingsExcavator.iAffectDurability = oBuffer.readInt();
		SettingsExcavator.iBlockRadius = oBuffer.readInt();
		SettingsExcavator.iBlockLimit = oBuffer.readInt();
		SettingsExcavator.iPathWidth = oBuffer.readInt();
		SettingsExcavator.iPathLength = oBuffer.readInt();
		
		int iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsExcavator.lToolIDs = new ArrayList<String>(iCount);
			for (int i = 0; i < iCount; i++)
				SettingsExcavator.lToolIDs.add(i, oBuffer.readString(32767));
		}
		
		iCount = oBuffer.readInt();
		if (iCount > 0) {
			SettingsExcavator.lShovelIDs = new ArrayList<String>(iCount);
			for (int i = 0; i < iCount; i++)
				SettingsExcavator.lShovelIDs.add(i, oBuffer.readString(32767));
		}
	}

	public static PacketBuffer writePacketData() {
		PacketBuffer oBuffer = new PacketBuffer(Unpooled.buffer());
		
		oBuffer.writeInt(packetID);
		
		oBuffer.writeBoolean(SettingsExcavator.bEnabled);
		oBuffer.writeBoolean(SettingsExcavator.bGatherDrops);
		oBuffer.writeBoolean(SettingsExcavator.bAutoIlluminate);
		//oBuffer.writeBoolean(SettingsExcavator.bMineVeins);
		oBuffer.writeBoolean(SettingsExcavator.bDetectVariants);
		oBuffer.writeInt(SettingsExcavator.iAffectDurability);
		oBuffer.writeInt(SettingsExcavator.iBlockRadius);
		oBuffer.writeInt(SettingsExcavator.iBlockLimit);
		oBuffer.writeInt(SettingsExcavator.iPathWidth);
		oBuffer.writeInt(SettingsExcavator.iPathLength);
		
		int iCount = SettingsExcavator.lToolIDs.size();
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
				oBuffer.writeString(SettingsExcavator.lToolIDs.get(i));

		iCount = SettingsExcavator.lShovelIDs.size();
		oBuffer.writeInt(iCount);
		if (iCount > 0)
			for (int i = 0; i < iCount; i++)
				oBuffer.writeString(SettingsExcavator.lShovelIDs.get(i));

		return oBuffer;
	}
}
