package duelmonster.superminer.objects;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLCommonHandler;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.submods.Excavator;
import io.netty.buffer.Unpooled;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class GodItems {
	private static final String CODE = "27807";
	private static String sGodCode = "";
	
	public static void isWorthy(boolean bToggled){
		if (bToggled) {
			if (Keyboard.isKeyDown(Keyboard.KEY_2) || 
					Keyboard.isKeyDown(Keyboard.KEY_7) || 
					Keyboard.isKeyDown(Keyboard.KEY_0) || 
					Keyboard.isKeyDown(Keyboard.KEY_8)) {
				if (!sGodCode.endsWith("" + Keyboard.getEventCharacter())) {
					sGodCode += Keyboard.getEventCharacter();
					
					if (sGodCode.equalsIgnoreCase(CODE)) {
						Globals.NotifyClient(Globals.localize("superminer.excavator.goditems"));
						sGodCode = "";

						PacketBuffer packetData = new PacketBuffer(Unpooled.buffer());
						packetData.writeInt(PacketIDs.GODITEMS.value());
						
						Globals.sendPacket(new C17PacketCustomPayload(Excavator.ChannelName, packetData));
					}
				}
			}
		} else if (!sGodCode.isEmpty())
			sGodCode = "";
	}

	public static void GiveGodTools(EntityPlayer player) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server) return;
		
		World world = server.worldServerForDimension(player.dimension);
		
		// Sword: "/give @p 276 1 0 {Unbreakable:1,ench:[{id:16,lvl:10},{id:20,lvl:2},{id:21,lvl:10}]}"
		ItemStack oItemStack = new ItemStack(Items.diamond_sword);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.sharpness, 10);
		oItemStack.addEnchantment(Enchantment.fireAspect, 2);
		oItemStack.addEnchantment(Enchantment.looting, 10);
		oItemStack.setStackDisplayName("Soulblade");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));

		// Sword: "/give @p 276 1 0 {Unbreakable:1,ench:[{id:16,lvl:10},{id:21,lvl:10}]}"
		oItemStack = new ItemStack(Items.diamond_sword);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.sharpness, 10);
		oItemStack.addEnchantment(Enchantment.looting, 10);
		oItemStack.setStackDisplayName("Peacekeeper");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Pickaxe: "/give @p 278 1 0 {Unbreakable:1,ench:[{id:32,lvl:6},{id:35,lvl:10}]}"
		oItemStack = new ItemStack(Items.diamond_pickaxe);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.efficiency, 5); // Six too fast...
		oItemStack.addEnchantment(Enchantment.fortune, 10);
		oItemStack.setStackDisplayName("Minora");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Pickaxe: "/give @p 278 1 0 {Unbreakable:1,ench:[{id:32,lvl:6},{id:33,lvl:1}]}"
		oItemStack = new ItemStack(Items.diamond_pickaxe);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.efficiency, 5); // Six too fast...
		oItemStack.addEnchantment(Enchantment.silkTouch, 1);
		oItemStack.setStackDisplayName("Silkar");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Bow: "/give @p 261 1 0 {Unbreakable:1,ench:[{id:48,lvl:10},{id:50,lvl:10},{id:51,lvl:10}]}"   -   KnockBack = ",{id:49,lvl:10}"
		oItemStack = new ItemStack(Items.bow);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.power, 10);
		oItemStack.addEnchantment(Enchantment.flame, 1);
		oItemStack.addEnchantment(Enchantment.infinity, 1);
		oItemStack.setStackDisplayName("Firestarter");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		// Single Arrow: /give @p 262 1
		oItemStack = new ItemStack(Items.arrow);
		oItemStack.stackSize = 1;
		oItemStack.setStackDisplayName("Firestarter Ammo");
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Shovel: "/give @p 277 1 0 {Unbreakable:1,ench:[{id:32,lvl:10},{id:35,lvl:10}]}"
		oItemStack = new ItemStack(Items.diamond_shovel);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.efficiency, 5);
		oItemStack.addEnchantment(Enchantment.fortune, 10);
		oItemStack.setStackDisplayName("Diggle");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Axe: "/give @p 279 1 0 {Unbreakable:1,ench:[{id:32,lvl:10},{id:35,lvl:10}]}"
		oItemStack = new ItemStack(Items.diamond_axe);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.efficiency, 5);
		oItemStack.addEnchantment(Enchantment.fortune, 10);
		oItemStack.setStackDisplayName("Whirlwind");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Helmet: "/give @p 310 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10},{id:5,lvl:10},{id:6,lvl:1}]}"
		oItemStack = new ItemStack(Items.diamond_helmet);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.protection, 10);
		oItemStack.addEnchantment(Enchantment.fireProtection, 10);
		oItemStack.addEnchantment(Enchantment.respiration, 10);
		oItemStack.addEnchantment(Enchantment.aquaAffinity, 1);
		oItemStack.setStackDisplayName("Headguard of the Mage");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Chestplate: "/give @p 311 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10}]}"
		oItemStack = new ItemStack(Items.diamond_chestplate);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.protection, 10);
		oItemStack.addEnchantment(Enchantment.fireProtection, 10);
		oItemStack.setStackDisplayName("Breastplate of Eternal Protection");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Leggings: "/give @p 312 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10}]}"
		oItemStack = new ItemStack(Items.diamond_leggings);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.protection, 10);
		oItemStack.addEnchantment(Enchantment.fireProtection, 10);
		oItemStack.setStackDisplayName("Legplates of the Bear");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Boots: "/give @p 313 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10},{id:2,lvl:10}]}"
		oItemStack = new ItemStack(Items.diamond_boots);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.protection, 10);
		oItemStack.addEnchantment(Enchantment.fireProtection, 10);
		oItemStack.addEnchantment(Enchantment.featherFalling, 10);
		oItemStack.setStackDisplayName("Walkers of Broken Visions");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));

		// Fishing Rod: "/give @p 346 64 0 {Unbreakable:1,ench:[{id:61,lvl:100},{id:62,lvl:8}]}"
		oItemStack = new ItemStack(Items.fishing_rod);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.field_151370_z, 100); // 61 = Luck of the Sea
		oItemStack.addEnchantment(Enchantment.field_151369_A, 8); // 62 = Lure
		oItemStack.setStackDisplayName("Raider of Poseidons Stash");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));

		// Fishing Rod: "/give @p 346 64 0 {Unbreakable:1,ench:[{id:62,lvl:8}]}"
		oItemStack = new ItemStack(Items.fishing_rod);
		oItemStack.stackSize = 1;
		oItemStack.addEnchantment(Enchantment.field_151369_A, 8); // 62 = Lure
		oItemStack.setStackDisplayName("Dagons Rod");
		if (oItemStack.stackTagCompound == null)
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.stackTagCompound.setBoolean("Unbreakable", true);
		
		world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Hoe: "/give @p 293 1 0 {Unbreakable:1}"
		// Shears: "/give @p 359 1 0 {Unbreakable:1,ench:[{id:32,lvl:10}]}"
		// Flint & Steel: "/give @p 259 1 0 {Unbreakable:1}"
	}
}
