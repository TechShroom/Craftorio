package com.techshroom.mods.craftorio.util;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public final class InventoryUtil {

    private InventoryUtil() {
    }

    @Nullable
    public static ItemStack pullOneStack(IItemHandler pullInv) {
        return pull(pullInv, 64);
    }

    @Nullable
    public static ItemStack pull(IItemHandler pullInv, int max) {
        ItemStack stack;
        for (int i = 0; i < pullInv.getSlots(); i++) {
            if ((stack = pullInv.extractItem(i, max, false)) != null) {
                return stack;
            }
        }
        return null;
    }

    @Nullable
    public static ItemStack putStack(IItemHandler pushInv, @Nullable ItemStack stack) {
        if (stack == null) {
            return null;
        }
        for (int i = 0; i < pushInv.getSlots(); i++) {
            if ((stack = pushInv.insertItem(i, stack, false)) == null || stack.stackSize == 0) {
                return null;
            }
        }
        return stack;
    }

}
