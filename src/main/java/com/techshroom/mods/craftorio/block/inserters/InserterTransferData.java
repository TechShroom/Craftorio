package com.techshroom.mods.craftorio.block.inserters;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

import net.minecraft.item.ItemStack;

@AutoValue
public abstract class InserterTransferData {

    public static InserterTransferData begin(@Nullable ItemStack stack) {
        return of(stack, 0);
    }

    public static InserterTransferData of(@Nullable ItemStack stack, int ticks) {
        return new AutoValue_InserterTransferData(stack, ticks);
    }

    InserterTransferData() {
    }

    @Nullable
    public abstract ItemStack getItemStack();

    public abstract int getTicks();

    public final InserterTransferData tick() {
        return of(getItemStack(), getTicks() + 1);
    }

}
