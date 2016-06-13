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
package com.techshroom.mods.pereltrains;

import com.techshroom.mods.pereltrains.block.ExtendedBlock;
import com.techshroom.mods.pereltrains.block.PerelBlocks;
import com.techshroom.mods.pereltrains.util.GeneralUtility;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class PerelProxy {

    public static final class Client extends PerelProxy {

        @Override
        public void onPreInit() {
            super.onPreInit();
            for (Block block : ExtendedBlock.getRegisteredBlocks()) {
                ModelLoader.setCustomModelResourceLocation(
                        Item.getItemFromBlock(block), 0,
                        new ModelResourceLocation(
                                GeneralUtility
                                        .addressMod(block.getUnlocalizedName()),
                                "inventory"));
            }
        }

        @Override
        public void onInit() {
            super.onInit();
            PerelBlocks.loadColorHandlers();
        }

    }

    public void onPreInit() {
        PerelBlocks.registerBlocks();
    }

    public void onInit() {
    }

}
