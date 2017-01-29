package duelmonster.superminer.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cpw.mods.fml.client.FMLClientHandler;
import duelmonster.superminer.util.BlockPos;
import duelmonster.superminer.util.ClickType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class SubstitutionHelper {
	private static final Minecraft mc = FMLClientHandler.instance().getClient();
	
	private static ItemStack	prevHeldItem	= null;
	private static Random		prevWorldRandom	= null;
	
	public static float getDigSpeed(ItemStack itemstack, Block block, int metadata) {
		return (itemstack == null ? 1.0F : itemstack.getItem().getDigSpeed(itemstack, block, metadata));
	}
	
	public static float getBlockHardness(World world, BlockPos oPos) {
		Block block = world.getBlock(oPos.getX(), oPos.getY(), oPos.getZ());
		if (block == null) return 0;
		else return block.getBlockHardness(world, oPos.getX(), oPos.getY(), oPos.getZ());
	}
	
	public static float getBlockStrength(ItemStack itemstack, World world, BlockPos oPos) {
		Block block = world.getBlock(oPos.getX(), oPos.getY(), oPos.getZ());
		ChangeHeldItem(itemstack);
		float strength = block.getPlayerRelativeBlockHardness(mc.thePlayer, world, oPos.getX(), oPos.getY(), oPos.getZ());
		RestoreHeldItem();
		return strength;
	}
	
	public static int getAdjustedBlockStrength(double blockStrength) {
		return (blockStrength <= 0 ? Integer.MIN_VALUE : -MathHelper.ceiling_double_int(1D / blockStrength));
	}
	
	public static float getEfficiency(float digSpeed, ItemStack itemstack) {
		if (digSpeed <= 1.5F) return digSpeed;
		
		ChangeHeldItem(itemstack);
		float efficiencyLevel = EnchantmentHelper.getEfficiencyModifier(mc.thePlayer);
		RestoreHeldItem();
		
		if (efficiencyLevel == 0) return digSpeed;
		
		return digSpeed + efficiencyLevel * efficiencyLevel + 1;
	}
	
	public static int getHarvestLevel(ItemStack itemstack, Block block, int metadata) {
		
		ChangeHeldItem(null);
		boolean noTool = mc.thePlayer.canHarvestBlock(block);
		RestoreHeldItem();
		if (noTool) return 0;
		
		ChangeHeldItem(itemstack);
		boolean canHarvest = block.canHarvestBlock(mc.thePlayer, metadata);
		RestoreHeldItem();
		return (canHarvest ? 1 : -1);
	}
	
	public static boolean isBlockSilkTouchable(World world, BlockPos oPos) {
		Block block = world.getBlock(oPos.getX(), oPos.getY(), oPos.getZ());
		if (block == null) return false;
		
		int metadata = world.getBlockMetadata(oPos.getX(), oPos.getY(), oPos.getZ());
		
		boolean silkHarvest = block.canSilkHarvest(world, mc.thePlayer, oPos.getX(), oPos.getY(), oPos.getZ(), metadata);
		if (!silkHarvest) return false;
		
		Random zeroRandom = new NotSoRandom(true);
		Random maxRandom = new NotSoRandom(false);
		
		ItemStack stackedBlock = createBlockStack(block, metadata);
		List<ItemStack> stackedBlockList = Collections.singletonList(stackedBlock);
		
		ChangeWorldRandom(world, maxRandom);
		List<ItemStack> maxRandomDrops = block.getDrops(world, oPos.getX(), oPos.getY(), oPos.getZ(), metadata, 0);
		ChangeWorldRandom(world, zeroRandom);
		List<ItemStack> zeroRandomDrops = block.getDrops(world, oPos.getX(), oPos.getY(), oPos.getZ(), metadata, 0);
		RestoreWorldRandom(world);
		
		return (!areItemStackListsIdentical(stackedBlockList, maxRandomDrops) || !areItemStackListsIdentical(stackedBlockList, zeroRandomDrops));
	}
	
	public static boolean isBlockFortunable(World world, BlockPos oPos) {
		Block block = world.getBlock(oPos.getX(), oPos.getY(), oPos.getZ());
		if (block == null) return false;
		
		int metadata = world.getBlockMetadata(oPos.getX(), oPos.getY(), oPos.getZ());
		
		Random zeroRandom = new NotSoRandom(true);
		Random maxRandom = new NotSoRandom(false);
		
		ChangeWorldRandom(world, maxRandom);
		List<ItemStack> defaultMaxDrops = block.getDrops(world, oPos.getX(), oPos.getY(), oPos.getZ(), metadata, 0);
		List<ItemStack> fortuneMaxDrops = block.getDrops(world, oPos.getX(), oPos.getY(), oPos.getZ(), metadata, 3);
		ChangeWorldRandom(world, zeroRandom);
		List<ItemStack> defaultZeroDrops = block.getDrops(world, oPos.getX(), oPos.getY(), oPos.getZ(), metadata, 0);
		List<ItemStack> fortuneZeroDrops = block.getDrops(world, oPos.getX(), oPos.getY(), oPos.getZ(), metadata, 3);
		RestoreWorldRandom(world);
		
		return (!areItemStackListsIdentical(defaultMaxDrops, fortuneMaxDrops) || !areItemStackListsIdentical(defaultZeroDrops, fortuneZeroDrops));
	}
	
	private static void ChangeHeldItem(ItemStack itemstack) {
		int iSlot = mc.thePlayer.inventory.currentItem;
		
		prevHeldItem = mc.thePlayer.inventory.mainInventory[iSlot];
		mc.thePlayer.inventory.mainInventory[iSlot] = itemstack;
		
		if (prevHeldItem != null) mc.thePlayer.getAttributeMap().removeAttributeModifiers(prevHeldItem.getAttributeModifiers());
		if (itemstack != null) mc.thePlayer.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers());
	}
	
	private static void RestoreHeldItem() {
		int iSlot = mc.thePlayer.inventory.currentItem;
		
		ItemStack itemstack = mc.thePlayer.inventory.mainInventory[iSlot];
		mc.thePlayer.inventory.mainInventory[iSlot] = prevHeldItem;
		
		if (itemstack != null) mc.thePlayer.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers());
		if (prevHeldItem != null) mc.thePlayer.getAttributeMap().applyAttributeModifiers(prevHeldItem.getAttributeModifiers());
	}
	
	private static void ChangeWorldRandom(World world, Random random) {
		if (prevWorldRandom == null) prevWorldRandom = world.rand;
		world.rand = random;
	}
	
	private static void RestoreWorldRandom(World world) {
		world.rand = prevWorldRandom;
		prevWorldRandom = null;
	}
	
	/**
	 * Returns an item stack containing a single instance of the block. 'metadata' is the block's subtype/damage and is
	 * ignored for blocks which do not support subtypes. Blocks which cannot be harvested should return null.
	 */
	private static ItemStack createBlockStack(Block block, int metadata) {
		int itemMetadata = 0;
		Item item = Item.getItemFromBlock(block);
		
		if (item != null && item.getHasSubtypes()) itemMetadata = metadata;
		
		return new ItemStack(item, 1, itemMetadata);
	}
	
	/**
	 * Detects whether two collections of ItemStack contain the same items. It depends on multiplicity, but doesn't
	 * depend on order. Note that it will return false if comparing 2 Dirt + 2 Dirt to 1 Dirt + 3 * Dirt.
	 * 
	 * @param stack_1
	 *            - The first collection
	 * @param stack_2
	 *            - The second collection
	 * @return true - If they contain the same items (multiplicity matters, order doesn't), false otherwise
	 */
	public static boolean areItemStackListsIdentical(Collection<? extends ItemStack> stackCollection_1, Collection<? extends ItemStack> stackCollection_2) {
		if (stackCollection_1.size() != stackCollection_2.size()) return false;
		
		List<ItemStack> stackList_1 = new ArrayList<ItemStack>(stackCollection_1);
		List<ItemStack> stackList_2 = new ArrayList<ItemStack>(stackCollection_2);
		
		Iterator<ItemStack> stackIterator_1 = stackList_1.iterator();
		
		outerLoop:
		while (stackIterator_1.hasNext()) {
			ItemStack stack_1 = stackIterator_1.next();
			
			Iterator<ItemStack> stackIterator_2 = stackList_2.iterator();
			
			while (stackIterator_2.hasNext()) {
				ItemStack stack_2 = stackIterator_2.next();
				
				if (ItemStack.areItemStacksEqual(stack_1, stack_2)) {
					try {
						if (stackList_1.size() > 0) stackIterator_1.remove();
						if (stackList_2.size() > 0) stackIterator_2.remove();
					} catch (ConcurrentModificationException e) {}
					
					continue outerLoop;
				}
			}
			return false;
		}
		
		return true;
	}
	
	public static boolean isItemStackDamageable(ItemStack itemstack) {
		return (itemstack != null && itemstack.getItem().isDamageable());
	}
	
	public static boolean isSameTool(ItemStack stack_1, ItemStack stack_2) {
		return (stack_1 != null && stack_2 != null && stack_2.getItem().getClass() == stack_1.getItem().getClass());
	}
	
	public static boolean isSword(ItemStack itemstack) {
		if (itemstack != null &&
				(itemstack.getItem() instanceof ItemSword ||
						itemstack.getUnlocalizedName().toLowerCase().contains("sword")))
			return true;
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static Set<Enchantment> getMultiToolEnchantments(ItemStack Tool_1, ItemStack Tool_2) {
		Set<Integer> enchantments = new HashSet<Integer>();
		Set<Enchantment> returnSet = new HashSet<Enchantment>();
		
		if (Tool_1 != null) enchantments.addAll(EnchantmentHelper.getEnchantments(Tool_1).keySet());
		if (Tool_2 != null) enchantments.addAll(EnchantmentHelper.getEnchantments(Tool_2).keySet());
		
		Iterator<Integer> iterator = enchantments.iterator();
		while (iterator.hasNext()) {
			Integer enchant = iterator.next();
			
			if (enchant == Enchantment.efficiency.effectId
					|| enchant == Enchantment.silkTouch.effectId
					|| enchant == Enchantment.fortune.effectId
					|| enchant == Enchantment.unbreaking.effectId
					|| enchant == Enchantment.looting.effectId
					|| enchant == Enchantment.knockback.effectId
					|| enchant == Enchantment.fireAspect.effectId) continue;
			
			Enchantment enchantment = Enchantment.enchantmentsList[enchant];
			if (enchantment.getName().startsWith("enchantment.damage.")) continue;
			
			returnSet.add(enchantment);
		}
		
		return returnSet;
	}
	
	public static double getVanillaStackDamage(ItemStack itemStack, EntityLivingBase entity) {
		ChangeHeldItem(itemStack);
		
		double attackDamage = mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		// getEnchantmentModifierDamage or getEnchantmentModifierLiving
		double enchantModifier = EnchantmentHelper.getEnchantmentModifierLiving(mc.thePlayer, entity);
		
		if (attackDamage > 0.0D || enchantModifier > 0.0D) {
			boolean isCritical = mc.thePlayer.fallDistance > 0.0F &&
					!mc.thePlayer.onGround &&
					!mc.thePlayer.isOnLadder() &&
					!mc.thePlayer.isInWater() &&
					!mc.thePlayer.isPotionActive(Potion.blindness) &&
					mc.thePlayer.ridingEntity == null;
			
			if (isCritical && attackDamage > 0) attackDamage *= 1.5D;
			
			attackDamage += enchantModifier;
		}
		
		RestoreHeldItem();
		
		return attackDamage;
	}
	
	/**
	 * Try move items from slotFrom to slotTo
	 * 
	 * @return true if at least some items were moved
	 */
	public static boolean transferStackFromTo(Container container, Minecraft mc, int slotFrom, int slotTo) {
		EntityPlayer player = mc.thePlayer;
		
		// Left click to take items
		mc.playerController.windowClick(container.windowId, slotFrom, 0, ClickType.PICKUP.getIndex(), player);
		
		// Couldn't take the items, bail out now
		if (player.inventory.getItemStack() == null) return false;
		
		boolean bRtrn = true;
		int size = player.inventory.getItemStack().stackSize;
		
		// Left click on the target slot to put the items to it
		mc.playerController.windowClick(container.windowId, slotTo, 0, ClickType.PICKUP.getIndex(), player);
		
		// If there are items left in the cursor, then return them back to the original slot
		if (player.inventory.getItemStack() != null) {
			bRtrn = player.inventory.getItemStack().stackSize != size;
			
			// Left click again on the from-slot to return the rest of the items to it
			mc.playerController.windowClick(container.windowId, slotFrom, 0, ClickType.PICKUP.getIndex(), player);
		}
		
		return bRtrn;
	}
}
