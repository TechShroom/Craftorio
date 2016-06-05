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

import com.techshroom.mods.pereltrains.Constants;
import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.block.entity.TileEntityRailSignal;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class PerelBlocks {

    public static final CreativeTabs TAB = new CreativeTabs("perel") {

        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(RAIL_SIGNAL);
        }

    };

    public static final BlockRailSignal RAIL_SIGNAL = new BlockRailSignal();

    public static void load() {
        // Doesn't actually do anything.
        register(new BlockRailSignal(), "rail_signal");
        GameRegistry.registerTileEntity(TileEntityRailSignal.class,
                "rail_signal");
    }

    private static void register(Block block, String name) {
        block.setRegistryName(Constants.MOD_ID, name);
        GameRegistry.register(block);
        GameRegistry.register(
                new ItemBlock(block).setRegistryName(Constants.MOD_ID, name));
        PerelTrains.getLogger().info("I have registered "
                + block.getRegistryName() + "/" + block.getUnlocalizedName());
    }

    private PerelBlocks() {
    }

}
