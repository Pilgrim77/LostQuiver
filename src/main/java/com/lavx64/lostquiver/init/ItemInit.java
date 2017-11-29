package com.lavx64.lostquiver.init;

import java.util.ArrayList;
import java.util.List;

import com.lavx64.lostquiver.objects.items.ItemQuiver;
import com.lavx64.lostquiver.objects.items.ItemQuiverWithArrows;

import net.minecraft.item.Item;

public class ItemInit {
	public static final List<Item> ITEMS = new ArrayList<Item>();

	public static final Item QUIVER = new ItemQuiver("quiver");
	public static final Item QUIVER_WITH_ARROWS = new ItemQuiverWithArrows("quiver_with_arrows", 576);

}
