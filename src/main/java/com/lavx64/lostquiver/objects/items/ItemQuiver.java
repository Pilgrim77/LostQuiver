package com.lavx64.lostquiver.objects.items;

import java.util.List;

import com.lavx64.lostquiver.Main;
import com.lavx64.lostquiver.init.ItemInit;
import com.lavx64.lostquiver.util.interfaces.IHasModel;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemQuiver extends Item implements IHasModel {

	public ItemQuiver(String name) {
		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(CreativeTabs.COMBAT);

		ItemInit.ITEMS.add(this);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add("Arrows: 0");
	}

	@Override
	public void registerModels() {
		Main.proxy.registerItemRenderer(this, 0, "inventory");
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
		if (armorType == EntityEquipmentSlot.CHEST
		||  armorType == EntityEquipmentSlot.LEGS
		||  armorType == EntityEquipmentSlot.MAINHAND
		||  armorType == EntityEquipmentSlot.OFFHAND)
			return true;
		return false;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (handIn != EnumHand.OFF_HAND) {
			// Get player block in range by +x +y +z
			BlockPos pos = playerIn.getPosition();
			BlockPos range1 = pos.add(-1, -1, -1);
			BlockPos range2 = pos.add(1, 3, 1);

			// Check for all arrows in this area
			List<EntityArrow> arrows = worldIn.getEntitiesWithinAABB(EntityArrow.class,
					new AxisAlignedBB(range1, range2));

			// If there's any arrows in this area
			if (!arrows.isEmpty()) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("Arrows", 0);
				ItemStack quiverWithArrowsStack = new ItemStack(ItemInit.QUIVER_WITH_ARROWS);

				for (EntityArrow a : arrows) {
					// Of course until it has reached it's max size
					if (nbt.getInteger("Arrows") < ItemQuiverWithArrows.MAX_SIZE) {
						worldIn.removeEntity(a);
						nbt.setInteger("Arrows", nbt.getInteger("Arrows") + 1);
					} else {
						quiverWithArrowsStack.setTagCompound(nbt);
						return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, quiverWithArrowsStack);
					}
				}

				// And return QuiverWithArrows with this much arrows
				quiverWithArrowsStack.setTagCompound(nbt);
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, quiverWithArrowsStack);
			} else {
				// If there's any arrows in the inventory
				if (playerIn.inventory.hasItemStack(new ItemStack(Items.ARROW))) {
					NBTTagCompound nbt = new NBTTagCompound();

					int arrowsSlot = playerIn.inventory.getSlotFor(new ItemStack(Items.ARROW));
					ItemStack arrowStack = playerIn.inventory.getStackInSlot(arrowsSlot);
					int arrowStackSize = arrowStack.getCount();
					nbt.setInteger("Arrows", arrowStackSize);

					ItemStack quiverWithArrowsStack = new ItemStack(ItemInit.QUIVER_WITH_ARROWS);
					quiverWithArrowsStack.setTagCompound(nbt);
					// Remove them from inventory and add to QuiverWithArrows
					playerIn.inventory.removeStackFromSlot(arrowsSlot);
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, quiverWithArrowsStack);
				} else {
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
				}
			}
		} else {
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		}
	}
}