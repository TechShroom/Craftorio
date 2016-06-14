/*
 * This file is part of PerelTrains, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshoom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.techshroom.mods.pereltrains.block;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.util.GeneralUtility;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public abstract class ExtendedBlock extends Block {

    private static final List<ExtendedBlock> registeredBlocks =
            new ArrayList<>();

    public static List<ExtendedBlock> getRegisteredBlocks() {
        return ImmutableList.copyOf(registeredBlocks);
    }

    {
        registeredBlocks.add(this);
    }

    protected ExtendedBlock(Material mat, String unlocalizedName) {
        super(mat);
        setUnlocalizedName(unlocalizedName);
        ModelLoader.setCustomStateMapper(this, new StateMap.Builder().build());
    }

    protected ExtendedBlock(String unlocalizedName) {
        this(Material.IRON, unlocalizedName);
    }

    public void clientInit() {
        String inventoryVariant = getInventoryVariant();
        PerelTrains.getLogger().info(
                "inventoryVariant for " + this + " is " + inventoryVariant);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this),
                0,
                new ModelResourceLocation(
                        GeneralUtility.addressMod(
                                getRegistryName().getResourcePath()),
                        inventoryVariant));
    }

    protected String getInventoryVariant() {
        return "inventory";
    }

}
