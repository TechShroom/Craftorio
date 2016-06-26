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
package com.techshroom.mods.pereltrains.block.entity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.block.BlockRailSignal;
import com.techshroom.mods.pereltrains.block.PerelBlocks;
import com.techshroom.mods.pereltrains.segment.Segment;
import com.techshroom.mods.pereltrains.util.GeneralUtility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TileEntityAutoRailBase extends TileEntity {

    private static final class OnChunkLoad {

        @SubscribeEvent
        public void onChunkLoad(ChunkEvent.Load load) {
            updateAll();
        }

    }

    public static void addHooks() {
        MinecraftForge.EVENT_BUS.register(new OnChunkLoad());
    }

    private static final Set<TileEntityAutoRailBase> updateMe = new HashSet<>();

    private static void updateAll() {
        updateMe.forEach(TileEntityAutoRailBase::updateLinks);
        updateMe.clear();
    }

    private final Set<BlockPos> connections = new HashSet<>();
    private Segment segment;

    @Override
    public void onLoad() {
        updateLinks();
    }

    public void updateLinks() {
        if (getWorld().isRemote) {
            return;
        }
        this.connections.clear();
        if (this.segment != null) {
            this.segment.removeRail(this);
        }
        World world = getWorld();
        if (!world.isBlockLoaded(getPos())) {
            updateMe.add(this);
            return;
        }
        List<BlockPos> connectedRails =
                PerelBlocks.NORMAL_RAIL.new ConnectionHelper(world, getPos(),
                        world.getBlockState(getPos())).getConnectedRails();
        for (BlockPos pos : connectedRails) {
            if (!world.isBlockLoaded(pos)) {
                // we'll get it later!
                continue;
            }
            TileEntityAutoRailBase rail =
                    (TileEntityAutoRailBase) world.getChunkFromBlockCoords(pos)
                            .getTileEntity(pos, EnumCreateEntityType.CHECK);
            if (rail == null) {
                // skip it, soft connection
                continue;
            }
            if (!rail.isConnectedTo(getPos())) {
                // We're not actually connected to them.
                continue;
            }
            this.connections.add(pos);
            if (this.segment == null) {
                this.segment = rail.getSegment();
            }
            if (rail.getSegment() != this.segment) {
                rail.segment = this.segment;
                propagateNonRecursive(rail);
            } else if (rail.getSegment() == null) {
                // schedule re-write for later?
                // idk
                PerelTrains.getLogger()
                        .warn("Rail with null segment as neighbor: " + rail);
            }
            // Now add ourselves to their set, because why not
            rail.connections.add(getPos());
            rail.markDirty();
        }
        if (this.segment == null) {
            this.segment = Segment.create();
        }
        this.segment.addRail(this);
        PerelTrains.getLogger().info("finalConnections " + this.connections);
        this.markDirty();
    }

    private void propagateNonRecursive(TileEntityAutoRailBase start) {
        Queue<BlockPos> updates = new LinkedList<>(start.connections);
        World world = getWorld();
        while (!updates.isEmpty()) {
            BlockPos pos = updates.poll();
            if (!world.isBlockLoaded(pos)) {
                // we'll get it later!
                continue;
            }
            TileEntityAutoRailBase rail =
                    (TileEntityAutoRailBase) world.getChunkFromBlockCoords(pos)
                            .getTileEntity(pos, EnumCreateEntityType.CHECK);
            if (rail == null) {
                // skip it, soft connection
                continue;
            }
            if (rail.segment != this.segment) {
                rail.segment = this.segment;
                rail.markDirty();
                updates.addAll(rail.connections);
            }
        }
    }

    public void onSignalAttached(TileEntityRailSignal signal) {
        World world = getWorld();
        IBlockState realState = world.getBlockState(signal.getPos())
                .getActualState(world, getPos());
        EnumFacing facing = realState.getValue(BlockRailSignal.FACING_PRORERTY);
        
    }

    public boolean isConnectedTo(BlockPos pos) {
        return PerelBlocks.NORMAL_RAIL.new ConnectionHelper(getWorld(),
                getPos(), getWorld().getBlockState(getPos()))
                        .getConnectedRails().contains(pos);
    }

    @Override
    public void onChunkUnload() {
        if (this.segment != null) {
            this.segment.removeRail(this);
        }
        super.onChunkUnload();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (this.segment != null) {
            compound.setInteger("segmentId", this.segment.getId());
        }
        NBTTagList list = new NBTTagList();
        this.connections.forEach(conn -> {
            list.appendTag(GeneralUtility.blockPosData(conn));
        });
        compound.setTag("connections", list);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("segmentId")) {
            this.segment = Segment.get(compound.getInteger("segmentId"));
            this.segment.addRail(this);
        }
        if (compound.hasKey("connections")) {
            this.connections.clear();
            NBTTagList data =
                    compound.getTagList("connections", NBT.TAG_INT_ARRAY);
            for (int i = 0; i < data.tagCount(); i++) {
                this.connections.add(GeneralUtility.blockPosData(data.get(i)));
            }
        }
    }

    @Override
    public void markDirty() {
        if (!getWorld().isRemote) {
            getWorld()
                    .getPlayers(EntityPlayerMP.class,
                            epmp -> epmp.getDistanceSq(getPos()) < 64 * 64)
                    .forEach(epmp -> epmp.connection
                            .sendPacket(getUpdatePacket()));
        }
        super.markDirty();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        if (net.getNetHandler() instanceof INetHandlerPlayClient) {
            readFromNBT(pkt.getNbtCompound());
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(),
                getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    public Segment getSegment() {
        return this.segment;
    }

    public Set<BlockPos> getConnections() {
        return this.connections;
    }

    public Set<TileEntityAutoRailBase> getConnectionsAsTE() {
        return this.connections.stream().map(getWorld()::getTileEntity)
                .map(TileEntityAutoRailBase.class::cast)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[segment=" + this.segment + ",pos="
                + getPos() + "]";
    }

}
