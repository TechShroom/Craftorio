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
package com.techshroom.mods.craftorio.block.entity;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.techshroom.mods.craftorio.block.BlockRailSignal;
import com.techshroom.mods.craftorio.block.LightValue;
import com.techshroom.mods.craftorio.signal.BlockingState;
import com.techshroom.mods.craftorio.util.GeneralUtility;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TileEntityRailSignal extends TileEntity {

    private static final class OnChunkLoad {

        @SubscribeEvent
        public void onChunkLoad(ChunkEvent.Load load) {
            updateAll();
        }

    }

    public static void addHooks() {
        MinecraftForge.EVENT_BUS.register(new OnChunkLoad());
    }

    private static final Set<TileEntityRailSignal> updateMe =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static void updateAll() {
        updateMe.forEach(TileEntityRailSignal::recalculateLighting);
        updateMe.clear();
    }

    @Override
    public void onLoad() {
        recalculateLighting();
    }

    public Optional<TileEntityAutoRailBase> getAttachedRail() {
        World w = getWorld();
        EnumFacing attachedDir = w.getBlockState(getPos())
                .getValue(BlockRailSignal.ATTACHED_RAIL_PROPERTY);
        return Optional
                .ofNullable(w.getChunkFromBlockCoords(getPos()).getTileEntity(
                        getPos().offset(attachedDir),
                        EnumCreateEntityType.CHECK))
                .map(GeneralUtility.castOrNull(TileEntityAutoRailBase.class));
    }

    public void recalculateLighting() {
        World w = getWorld();
        if (!w.isBlockLoaded(getPos())) {
            updateMe.add(this);
            return;
        }

        getAttachedRail().ifPresent(r -> r.onSignalAttached(this));
    }

    private void setState(LightValue light) {
        World w = getWorld();
        w.setBlockState(getPos(), w.getBlockState(getPos())
                .withProperty(BlockRailSignal.LIGHT_PROPERTY, light));
    }

    public void onStateChange(BlockingState previousState,
            BlockingState newState) {
        switch (newState) {
            case OPEN:
                setState(LightValue.GREEN);
                break;
            case EXPECTING:
                setState(LightValue.YELLOW);
                break;
            case CLOSED:
                setState(LightValue.RED);
                break;
            default:
                setState(LightValue.NONE);
                break;
        }
    }

}
