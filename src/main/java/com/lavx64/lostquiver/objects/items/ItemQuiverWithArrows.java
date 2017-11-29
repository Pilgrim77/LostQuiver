package com.lavx64.lostquiver.objects.items;

import java.util.List;

import com.lavx64.lostquiver.Main;
import com.lavx64.lostquiver.init.ItemInit;
import com.lavx64.lostquiver.util.interfaces.IHasModel;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemQuiverWithArrows extends ItemArrow implements IHasModel {

	public static int MAX_SIZE;

	public ItemQuiverWithArrows(String name, int size) {
		setUnlocalizedName(name);
		setRegistryName(name);
		this.MAX_SIZE = size;

		ItemInit.ITEMS.add(this);
	}

	@Override
	public void registerModels() {
		Main.proxy.registerItemRenderer(this, 0, "inventory");
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
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound())
			if (stack.getTagCompound().hasKey("Arrows"))
				if (stack.getTagCompound().getInteger("Arrows") < MAX_SIZE)
					tooltip.add("Arrows: " + stack.getTagCompound().getInteger("Arrows"));
				else
					tooltip.add("Arrows: " + stack.getTagCompound().getInteger("Arrows") + " (Full)");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (handIn != EnumHand.OFF_HAND) {
			if (!playerIn.isSneaking()) {
				// Get player block in range by +x +y +z
				BlockPos pos = playerIn.getPosition();
				BlockPos range1 = pos.add(-1, -1, -1);
				BlockPos range2 = pos.add(1, 3, 1);

				// Check for all arrows in this area
				List<EntityArrow> arrows = worldIn.getEntitiesWithinAABB(EntityArrow.class,
						new AxisAlignedBB(range1, range2));

				// If there's any arrows in this area
				if (!arrows.isEmpty()) {
					int arrowCount = arrows.size();
					ItemStack quiverWithArrowsStack = playerIn.getHeldItem(handIn);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt = quiverWithArrowsStack.getTagCompound();

					// Remove all arrows from within range
					for (EntityArrow a : arrows) {
						// Of course until it has reached it's max size
						if (nbt.getInteger("Arrows") < MAX_SIZE) {
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
					ItemStack quiverWithArrowsStack = playerIn.getHeldItem(handIn);
					NBTTagCompound nbt;

					if (playerIn.inventory.hasItemStack(new ItemStack(Items.ARROW))) {
						nbt = quiverWithArrowsStack.getTagCompound();
						int arrowsSlot = playerIn.inventory.getSlotFor(new ItemStack(Items.ARROW));
						ItemStack arrowStack = playerIn.inventory.getStackInSlot(arrowsSlot);
						int arrowStackSize = arrowStack.getCount();
						int quiverArrowsCount = nbt.getInteger("Arrows");
						if (quiverArrowsCount + arrowStackSize > MAX_SIZE) {
							if (quiverArrowsCount != MAX_SIZE) {
								int arrowsSet = (arrowStackSize + quiverArrowsCount) - MAX_SIZE;
								int inQuiver = arrowStackSize - arrowsSet;

								playerIn.inventory.setInventorySlotContents(arrowsSlot,
										new ItemStack(Items.ARROW, arrowsSet));
								nbt.setInteger("Arrows", nbt.getInteger("Arrows") + inQuiver);
								quiverWithArrowsStack.setTagCompound(nbt);
								return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, quiverWithArrowsStack);
							} else {
								return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, quiverWithArrowsStack);
							}
						} else {
							nbt.setInteger("Arrows", nbt.getInteger("Arrows") + arrowStackSize);
							quiverWithArrowsStack.setTagCompound(nbt);
							playerIn.inventory.removeStackFromSlot(arrowsSlot);
						}
					}
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, quiverWithArrowsStack);
				}
			} else {
				// If player is sneaking e.g. get items from quiver
				ItemStack quiverWithArrowsStack = playerIn.getHeldItem(handIn);
				NBTTagCompound nbt;

				nbt = quiverWithArrowsStack.getTagCompound();
				int arrowsCount = nbt.getInteger("Arrows");

				if (arrowsCount >= 64) {
					nbt.setInteger("Arrows", nbt.getInteger("Arrows") - 64);
					playerIn.inventory.addItemStackToInventory(new ItemStack(Items.ARROW, 64));
				} else {
					nbt.setInteger("Arrows", 0);
					playerIn.inventory.addItemStackToInventory(new ItemStack(Items.ARROW, arrowsCount));
				}

				quiverWithArrowsStack.setTagCompound(nbt);

				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, quiverWithArrowsStack);
			}
		} else {
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		}
	}

	@Override
	public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
		EntityTippedArrow entitytippedarrow = new EntityTippedArrow(worldIn, shooter);
		return entitytippedarrow;
	}

	@Override
	public boolean isInfinite(ItemStack stack, ItemStack bow, net.minecraft.entity.player.EntityPlayer player) {
		int enchant = net.minecraft.enchantment.EnchantmentHelper
				.getEnchantmentLevel(net.minecraft.init.Enchantments.INFINITY, bow);
		if (enchant <= 0) {
			NBTTagCompound nbt;
			nbt = stack.getTagCompound();
			nbt.setInteger("Arrows", nbt.getInteger("Arrows") - 1);
			stack.setTagCompound(nbt);
			return true;
		} else {
			return true;
		}
		
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (entityIn instanceof EntityPlayer) {
			// Offhand check
			if (((EntityPlayer) entityIn).getHeldItemOffhand().getItem() instanceof ItemArrow) {
				ItemStack newStack = ((EntityPlayer) entityIn).getHeldItemOffhand();
				// Set it to normal Quiver if it has no tag, or arrow tag, or arrow <= 0
				if (newStack.hasTagCompound()) {
					NBTTagCompound nbt = newStack.getTagCompound();
					if (nbt.hasKey("Arrows")) {
						if (nbt.getInteger("Arrows") <= 0) {
							((EntityPlayer) entityIn).setHeldItem(EnumHand.OFF_HAND, new ItemStack(ItemInit.QUIVER));
							return;
						}
					} else {
						((EntityPlayer) entityIn).setHeldItem(EnumHand.OFF_HAND, new ItemStack(ItemInit.QUIVER));
						return;
					}
				} else {
					((EntityPlayer) entityIn).setHeldItem(EnumHand.OFF_HAND, new ItemStack(ItemInit.QUIVER));
					return;
				}
			}

			// Armor slot 2(chest) check
			if (((EntityPlayer) entityIn).inventory.armorItemInSlot(2).getItem() instanceof ItemArrow) {
				ItemStack newStack = ((EntityPlayer) entityIn).inventory.armorItemInSlot(2);
				// Set it to normal Quiver if it has no tag, or arrow tag, or arrow <= 0
				if (newStack.hasTagCompound()) {
					NBTTagCompound nbt = newStack.getTagCompound();
					if (nbt.hasKey("Arrows")) {
						if (nbt.getInteger("Arrows") <= 0) {
							((EntityPlayer) entityIn).inventory.armorInventory.set(2, new ItemStack(ItemInit.QUIVER));
							return;
						}
					} else {
						((EntityPlayer) entityIn).inventory.armorInventory.set(2, new ItemStack(ItemInit.QUIVER));
						return;
					}
				} else {
					((EntityPlayer) entityIn).inventory.armorInventory.set(2, new ItemStack(ItemInit.QUIVER));
					return;
				}
			}

			// Armor slot 1(legs) check
			if (((EntityPlayer) entityIn).inventory.armorItemInSlot(1).getItem() instanceof ItemArrow) {
				ItemStack newStack = ((EntityPlayer) entityIn).inventory.armorItemInSlot(1);
				// Set it to normal Quiver if it has no tag, or arrow tag, or arrow <= 0
				if (newStack.hasTagCompound()) {
					NBTTagCompound nbt = newStack.getTagCompound();
					if (nbt.hasKey("Arrows")) {
						if (nbt.getInteger("Arrows") <= 0) {
							((EntityPlayer) entityIn).inventory.armorInventory.set(1, new ItemStack(ItemInit.QUIVER));
							return;
						}
					} else {
						((EntityPlayer) entityIn).inventory.armorInventory.set(1, new ItemStack(ItemInit.QUIVER));
						return;
					}
				} else {
					((EntityPlayer) entityIn).inventory.armorInventory.set(1, new ItemStack(ItemInit.QUIVER));
					return;
				}
			}

			// Inventory check
			if (stack.hasTagCompound()) {
				NBTTagCompound nbt = stack.getTagCompound();
				if (nbt.hasKey("Arrows")) {
					if (nbt.getInteger("Arrows") <= 0) {
						entityIn.replaceItemInInventory(itemSlot, new ItemStack(ItemInit.QUIVER));
					}
				} else {
					entityIn.replaceItemInInventory(itemSlot, new ItemStack(ItemInit.QUIVER));
				}
			} else {
				entityIn.replaceItemInInventory(itemSlot, new ItemStack(ItemInit.QUIVER));
			}
		}
	}
}