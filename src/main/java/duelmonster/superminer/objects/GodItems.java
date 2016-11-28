package duelmonster.superminer.objects;

import org.lwjgl.input.Keyboard;

import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.submods.Excavator;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
						
						Globals.sendPacket(new CPacketCustomPayload(Excavator.ChannelName, packetData));
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
		ItemStack oItemStack = new ItemStack(Items.DIAMOND_SWORD);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.SHARPNESS, 10);
		oItemStack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
		oItemStack.addEnchantment(Enchantments.LOOTING, 10);
		oItemStack.setStackDisplayName("Soulblade");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));

		// Sword: "/give @p 276 1 0 {Unbreakable:1,ench:[{id:16,lvl:10},{id:21,lvl:10}]}"
		oItemStack = new ItemStack(Items.DIAMOND_SWORD);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.SHARPNESS, 10);
		oItemStack.addEnchantment(Enchantments.LOOTING, 10);
		oItemStack.setStackDisplayName("Peacekeeper");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Pickaxe: "/give @p 278 1 0 {Unbreakable:1,ench:[{id:32,lvl:6},{id:35,lvl:10}]}"
		oItemStack = new ItemStack(Items.DIAMOND_PICKAXE);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
		oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
		oItemStack.setStackDisplayName("Minora");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Pickaxe: "/give @p 278 1 0 {Unbreakable:1,ench:[{id:32,lvl:6},{id:33,lvl:1}]}"
		oItemStack = new ItemStack(Items.DIAMOND_PICKAXE);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5); // Six too fast...
		oItemStack.addEnchantment(Enchantments.SILK_TOUCH, 1);
		oItemStack.setStackDisplayName("Silkar");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Bow: "/give @p 261 1 0 {Unbreakable:1,ench:[{id:48,lvl:10},{id:50,lvl:10},{id:51,lvl:10}]}"   -   KnockBack = ",{id:49,lvl:10}"
		oItemStack = new ItemStack(Items.BOW);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.POWER, 10);
		oItemStack.addEnchantment(Enchantments.FLAME, 1);
		oItemStack.addEnchantment(Enchantments.INFINITY, 1);
		oItemStack.setStackDisplayName("Firestarter");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		// Single Arrow: /give @p 262 1
		oItemStack = new ItemStack(Items.ARROW);
		oItemStack.setCount(1);
		oItemStack.setStackDisplayName("Firestarter Ammo");
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Shovel: "/give @p 277 1 0 {Unbreakable:1,ench:[{id:32,lvl:10},{id:35,lvl:10}]}"
		oItemStack = new ItemStack(Items.DIAMOND_SHOVEL);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5);
		oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
		oItemStack.setStackDisplayName("Diggle");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Axe: "/give @p 279 1 0 {Unbreakable:1,ench:[{id:32,lvl:10},{id:35,lvl:10}]}"
		oItemStack = new ItemStack(Items.DIAMOND_AXE);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.EFFICIENCY, 5);
		oItemStack.addEnchantment(Enchantments.FORTUNE, 10);
		oItemStack.setStackDisplayName("Whirlwind");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Helmet: "/give @p 310 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10},{id:5,lvl:10},{id:6,lvl:1}]}"
		oItemStack = new ItemStack(Items.DIAMOND_HELMET);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
		oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
		oItemStack.addEnchantment(Enchantments.RESPIRATION, 10);
		oItemStack.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
		oItemStack.setStackDisplayName("Headguard of the Mage");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Chestplate: "/give @p 311 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10}]}"
		oItemStack = new ItemStack(Items.DIAMOND_CHESTPLATE);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
		oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
		oItemStack.setStackDisplayName("Breastplate of Eternal Protection");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Leggings: "/give @p 312 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10}]}"
		oItemStack = new ItemStack(Items.DIAMOND_LEGGINGS);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
		oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
		oItemStack.setStackDisplayName("Legplates of the Bear");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Boots: "/give @p 313 1 0 {Unbreakable:1,ench:[{id:0,lvl:10},{id:1,lvl:10},{id:2,lvl:10}]}"
		oItemStack = new ItemStack(Items.DIAMOND_BOOTS);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.PROTECTION, 10);
		oItemStack.addEnchantment(Enchantments.FIRE_PROTECTION, 10);
		oItemStack.addEnchantment(Enchantments.FEATHER_FALLING, 10);
		oItemStack.addEnchantment(Enchantments.DEPTH_STRIDER, 10);
		oItemStack.setStackDisplayName("Walkers of Broken Visions");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));

		// Fishing Rod: "/give @p 346 64 0 {Unbreakable:1,ench:[{id:61,lvl:100},{id:62,lvl:8}]}"
		oItemStack = new ItemStack(Items.FISHING_ROD);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.LUCK_OF_THE_SEA, 100); // 61 = Luck of the Sea
		oItemStack.addEnchantment(Enchantments.LURE, 8); // 62 = Lure
		oItemStack.setStackDisplayName("Raider of Poseidons Stash");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));

		// Fishing Rod: "/give @p 346 64 0 {Unbreakable:1,ench:[{id:62,lvl:8}]}"
		oItemStack = new ItemStack(Items.FISHING_ROD);
		oItemStack.setCount(1);
		oItemStack.addEnchantment(Enchantments.LURE, 8); // 62 = Lure
		oItemStack.setStackDisplayName("Dagons Rod");
		if (!oItemStack.hasTagCompound())
			oItemStack.setTagCompound(new NBTTagCompound());
		oItemStack.getTagCompound().setBoolean("Unbreakable", true);
		
		world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, oItemStack));
		
		// Hoe: "/give @p 293 1 0 {Unbreakable:1}"
		// Shears: "/give @p 359 1 0 {Unbreakable:1,ench:[{id:32,lvl:10}]}"
		// Flint & Steel: "/give @p 259 1 0 {Unbreakable:1}"
	}
}
