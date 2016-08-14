/*
 * This file is part of Craftorio, licensed under the MIT License (MIT).
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
package com.techshroom.mods.craftorio.block;

import java.awt.Color;

import javax.annotation.Nullable;

import com.techshroom.mods.craftorio.Constants;
import com.techshroom.mods.craftorio.block.inserters.BlockRegularInserter;
import com.techshroom.mods.craftorio.block.inserters.TileEntityInserter;
import com.techshroom.mods.craftorio.block.rails.BlockAutoRailNormal;
import com.techshroom.mods.craftorio.block.rails.BlockRailSignal;
import com.techshroom.mods.craftorio.block.rails.LightValue;
import com.techshroom.mods.craftorio.block.rails.TileEntityAutoRailNormal;
import com.techshroom.mods.craftorio.block.rails.TileEntityRailSignal;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class CraftorioBlocks {

    public static final CreativeTabs TAB = new CreativeTabs("craftorio") {

        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(RAIL_SIGNAL);
        }

    };

    public static final BlockRailSignal RAIL_SIGNAL = new BlockRailSignal();
    public static final BlockAutoRailNormal NORMAL_RAIL = new BlockAutoRailNormal();
    public static final BlockRegularInserter REGULAR_INSERTER = new BlockRegularInserter();

    public static void registerBlocks() {
        register(RAIL_SIGNAL, "rail_signal", TileEntityRailSignal.class);
        register(NORMAL_RAIL, "normal_rail", TileEntityAutoRailNormal.class);
        register(REGULAR_INSERTER, "inserter.regular");
        
        // Register inserter tile as it's own thing
        GameRegistry.registerTileEntity(TileEntityInserter.class, "inserter");
    }

    public static void loadColorHandlers() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(CraftorioBlocks::signalColorHandler,
                RAIL_SIGNAL);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(CraftorioBlocks::signalColorHandler,
                RAIL_SIGNAL);
    }

    private static int onColor(Color c) {
        return c.brighter().brighter().brighter().getRGB();
    }

    private static int offColor(Color c) {
        return c.darker().darker().darker().getRGB();
    }

    private static final int RED_ON = onColor(Color.RED);
    private static final int RED_OFF = offColor(Color.RED);
    private static final int YELLOW_ON = onColor(Color.YELLOW);
    private static final int YELLOW_OFF = offColor(Color.YELLOW);
    private static final int GREEN_ON = onColor(Color.GREEN);
    private static final int GREEN_OFF = offColor(Color.GREEN);

    private static int
            signalColorHandler(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        return getTintIndex(state, tintIndex);
    }

    private static int signalColorHandler(ItemStack stack, int tintIndex) {
        // Probably an ItemBlock
        if (stack.getItem() instanceof ItemBlock) {
            return signalColorHandler((ItemBlock) stack.getItem(), stack.getMetadata(), tintIndex);
        } else {
            throw new UnsupportedOperationException("unimplemented handling of " + stack);
        }
    }

    private static int signalColorHandler(ItemBlock item, int metadata, int tintIndex) {
        @SuppressWarnings("deprecation")
        IBlockState state = item.block.getStateFromMeta(metadata);
        return getTintIndex(state, tintIndex);
    }

    private static int getTintIndex(IBlockState state, int tintIndex) {
        LightValue light = state.getValue(findLightValueProp(state));
        switch (tintIndex) {
            case 0:
                // GREEN
                return light == LightValue.GREEN ? GREEN_ON : GREEN_OFF;
            case 1:
                // YELLOW
                return light == LightValue.YELLOW ? YELLOW_ON : YELLOW_OFF;
            case 2:
                // RED
                return light == LightValue.RED ? RED_ON : RED_OFF;
            default:
                throw new IllegalStateException("bad tintIndex " + tintIndex);
        }
    }

    public static IProperty<LightValue> findLightValueProp(IBlockState state) {
        for (IProperty<?> prop : state.getPropertyNames()) {
            if (prop.getName().equals(Constants.LIGHT_PROPERTY_NAME)) {
                @SuppressWarnings("unchecked")
                IProperty<LightValue> propCast = (IProperty<LightValue>) prop;
                return propCast;
            }
        }
        throw new IllegalStateException("State " + state + " has no light property!");
    }

    private static void register(Block block, String name) {
        block.setCreativeTab(TAB);
        GameRegistry.register(block.setRegistryName(Constants.MOD_ID, name));
        GameRegistry.register(new ItemBlock(block).setRegistryName(Constants.MOD_ID, name));
    }

    private static void register(Block block, String name, Class<? extends TileEntity> tileClass) {
        register(block, name);
        GameRegistry.registerTileEntity(tileClass, name);
    }

    private CraftorioBlocks() {
    }

}
