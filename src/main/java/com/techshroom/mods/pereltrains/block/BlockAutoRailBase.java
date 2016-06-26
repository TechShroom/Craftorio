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

import javax.annotation.Nullable;

import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.block.entity.TESRRailSegmentDisplay;
import com.techshroom.mods.pereltrains.block.entity.TileEntityAutoRailBase;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockAutoRailBase extends ExtendedBlock
        implements ITileEntityProvider {

    protected static final AxisAlignedBB FLAT_AABB =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
    protected static final AxisAlignedBB ASCENDING_AABB =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.15625D, 1.0D);

    public static boolean isRailBlock(World worldIn, BlockPos pos) {
        return isRailBlock(worldIn.getBlockState(pos));
    }

    public static boolean isRailBlock(IBlockState state) {
        Block block = state.getBlock();
        return block instanceof BlockAutoRailBase;
    }

    protected BlockAutoRailBase(String unlocalizedName) {
        super(Material.CIRCUITS, unlocalizedName);
    }

    @Override
    public void clientInit() {
        super.clientInit();
        ClientRegistry.bindTileEntitySpecialRenderer(
                TileEntityAutoRailBase.class, new TESRRailSegmentDisplay());
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getShapeProperty(),
                RailDirection.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(getShapeProperty()).getMetadata();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(getShapeProperty())
                .build();
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState,
            World worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks
     * for render
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source,
            BlockPos pos) {
        BlockAutoRailBase.RailDirection railDir = state.getBlock() == this
                ? (BlockAutoRailBase.RailDirection) state
                        .getValue(this.getShapeProperty())
                : null;
        return railDir != null && railDir.isAscending() ? ASCENDING_AABB
                : FLAT_AABB;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isSideSolid(worldIn,
                pos.down(), EnumFacing.UP);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            state = this.updateDir(worldIn, pos, state, true);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos,
            Block blockIn) {
        if (!worldIn.isRemote) {
            BlockAutoRailBase.RailDirection railDir =
                    state.getValue(this.getShapeProperty());
            boolean invalid = false;

            if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn,
                    pos.down(), EnumFacing.UP)) {
                invalid = true;
            }

            if (railDir == BlockAutoRailBase.RailDirection.ASCENDING_EAST
                    && !worldIn.getBlockState(pos.east()).isSideSolid(worldIn,
                            pos.east(), EnumFacing.UP)) {
                invalid = true;
            } else if (railDir == BlockAutoRailBase.RailDirection.ASCENDING_WEST
                    && !worldIn.getBlockState(pos.west()).isSideSolid(worldIn,
                            pos.west(), EnumFacing.UP)) {
                invalid = true;
            } else if (railDir == BlockAutoRailBase.RailDirection.ASCENDING_NORTH
                    && !worldIn.getBlockState(pos.north()).isSideSolid(worldIn,
                            pos.north(), EnumFacing.UP)) {
                invalid = true;
            } else if (railDir == BlockAutoRailBase.RailDirection.ASCENDING_SOUTH
                    && !worldIn.getBlockState(pos.south()).isSideSolid(worldIn,
                            pos.south(), EnumFacing.UP)) {
                invalid = true;
            }

            if (invalid && !worldIn.isAirBlock(pos)) {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            } else {
                this.updateState(state, worldIn, pos, blockIn);
            }
        }
    }

    protected void updateState(IBlockState blockState, World world,
            BlockPos pos, Block block) {
        PerelTrains.getLogger().info("updateState " + pos);
        ((TileEntityAutoRailBase) world.getTileEntity(pos)).updateLinks();
    }

    protected IBlockState updateDir(World worldIn, BlockPos pos,
            IBlockState state, boolean p_176564_4_) {
        return worldIn.isRemote ? state
                : (new BlockAutoRailBase.ConnectionHelper(worldIn, pos, state))
                        .place(p_176564_4_).getBlockState();
    }

    @Override
    public abstract TileEntityAutoRailBase createNewTileEntity(World worldIn,
            int meta);

    @SuppressWarnings("deprecation")
    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.NORMAL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntityAutoRailBase tile =
                (TileEntityAutoRailBase) worldIn.getTileEntity(pos);
        if (tile != null) {

        }
        if (state.getValue(this.getShapeProperty()).isAscending()) {
            worldIn.notifyNeighborsOfStateChange(pos.up(), this);
        }
        super.breakBlock(worldIn, pos, state);
    }

    public abstract IProperty<BlockAutoRailBase.RailDirection>
            getShapeProperty();

    public RailDirection getRailDirection(IBlockAccess world, BlockPos pos,
            IBlockState state, @Nullable EntityMinecart cart) {
        return state.getValue(getShapeProperty());
    }

    public static enum RailDirection implements IStringSerializable {
        NORTH_SOUTH(0, "north_south"), EAST_WEST(1, "east_west"),
        ASCENDING_EAST(2, "ascending_east"),
        ASCENDING_WEST(3, "ascending_west"),
        ASCENDING_NORTH(4, "ascending_north"),
        ASCENDING_SOUTH(5, "ascending_south"), SOUTH_EAST(6, "south_east"),
        SOUTH_WEST(7, "south_west"), NORTH_WEST(8, "north_west"),
        NORTH_EAST(9, "north_east");

        private static final BlockAutoRailBase.RailDirection[] META_LOOKUP =
                new BlockAutoRailBase.RailDirection[values().length];
        private final int meta;
        private final String name;

        private RailDirection(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public int getMetadata() {
            return this.meta;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public boolean isAscending() {
            return this == ASCENDING_NORTH || this == ASCENDING_EAST
                    || this == ASCENDING_SOUTH || this == ASCENDING_WEST;
        }

        public static BlockAutoRailBase.RailDirection byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        @Override
        public String getName() {
            return this.name;
        }

        static {
            for (BlockAutoRailBase.RailDirection railDir : values()) {
                META_LOOKUP[railDir.getMetadata()] = railDir;
            }
        }
    }

    public class ConnectionHelper {

        private final World world;
        private final BlockPos pos;
        private final BlockAutoRailBase block;
        private IBlockState state;
        private final List<BlockPos> connectedRails = new ArrayList<>();

        public ConnectionHelper(World worldIn, BlockPos pos,
                IBlockState state) {
            this.world = worldIn;
            this.pos = pos;
            this.state = state;
            this.block = (BlockAutoRailBase) state.getBlock();
            BlockAutoRailBase.RailDirection railDir =
                    this.block.getRailDirection(worldIn, pos, state, null);
            this.updateConnectedRails(railDir);
        }

        public List<BlockPos> getConnectedRails() {
            return this.connectedRails;
        }

        private void updateConnectedRails(
                BlockAutoRailBase.RailDirection railDirection) {
            this.connectedRails.clear();

            switch (railDirection) {
                case NORTH_SOUTH:
                    this.connectedRails.add(this.pos.north());
                    this.connectedRails.add(this.pos.south());
                    break;
                case EAST_WEST:
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.east());
                    break;
                case ASCENDING_EAST:
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.east().up());
                    break;
                case ASCENDING_WEST:
                    this.connectedRails.add(this.pos.west().up());
                    this.connectedRails.add(this.pos.east());
                    break;
                case ASCENDING_NORTH:
                    this.connectedRails.add(this.pos.north().up());
                    this.connectedRails.add(this.pos.south());
                    break;
                case ASCENDING_SOUTH:
                    this.connectedRails.add(this.pos.north());
                    this.connectedRails.add(this.pos.south().up());
                    break;
                case SOUTH_EAST:
                    this.connectedRails.add(this.pos.east());
                    this.connectedRails.add(this.pos.south());
                    break;
                case SOUTH_WEST:
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.south());
                    break;
                case NORTH_WEST:
                    this.connectedRails.add(this.pos.west());
                    this.connectedRails.add(this.pos.north());
                    break;
                case NORTH_EAST:
                    this.connectedRails.add(this.pos.east());
                    this.connectedRails.add(this.pos.north());
            }
        }

        private void removeSoftConnections() {
            for (int i = 0; i < this.connectedRails.size(); ++i) {
                BlockAutoRailBase.ConnectionHelper BlockAutoRailBase$rail =
                        this.findRailAt(this.connectedRails.get(i));

                if (BlockAutoRailBase$rail != null
                        && BlockAutoRailBase$rail.isConnectedToRail(this)) {
                    this.connectedRails.set(i, BlockAutoRailBase$rail.pos);
                } else {
                    this.connectedRails.remove(i--);
                }
            }
        }

        private boolean hasRailAt(BlockPos pos) {
            return BlockAutoRailBase.isRailBlock(this.world, pos)
                    || BlockAutoRailBase.isRailBlock(this.world, pos.up())
                    || BlockAutoRailBase.isRailBlock(this.world, pos.down());
        }

        @Nullable
        private BlockAutoRailBase.ConnectionHelper findRailAt(BlockPos pos) {
            IBlockState state = this.world.getBlockState(pos);

            if (BlockAutoRailBase.isRailBlock(state)) {
                return BlockAutoRailBase.this.new ConnectionHelper(this.world,
                        pos, state);
            } else {
                BlockPos up = pos.up();
                state = this.world.getBlockState(up);

                if (BlockAutoRailBase.isRailBlock(state)) {
                    return BlockAutoRailBase.this.new ConnectionHelper(
                            this.world, up, state);
                } else {
                    BlockPos down = pos.down();
                    state = this.world.getBlockState(down);
                    return BlockAutoRailBase.isRailBlock(state)
                            ? BlockAutoRailBase.this.new ConnectionHelper(
                                    this.world, down, state)
                            : null;
                }
            }
        }

        private boolean
                isConnectedToRail(BlockAutoRailBase.ConnectionHelper rail) {
            return this.isConnectedTo(rail.pos);
        }

        private boolean isConnectedTo(BlockPos posIn) {
            for (int i = 0; i < this.connectedRails.size(); ++i) {
                BlockPos blockpos = this.connectedRails.get(i);

                if (blockpos.getX() == posIn.getX()
                        && blockpos.getZ() == posIn.getZ()) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Counts the number of rails adjacent to this rail.
         */
        protected int countAdjacentRails() {
            int i = 0;

            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                if (this.hasRailAt(this.pos.offset(enumfacing))) {
                    ++i;
                }
            }

            return i;
        }

        private boolean canConnectTo(BlockAutoRailBase.ConnectionHelper rail) {
            return this.isConnectedToRail(rail)
                    || this.connectedRails.size() != 2;
        }

        private void connectTo(BlockAutoRailBase.ConnectionHelper rail) {
            this.connectedRails.add(rail.pos);
            BlockPos northPos = this.pos.north();
            BlockPos southPos = this.pos.south();
            BlockPos westPos = this.pos.west();
            BlockPos eastPos = this.pos.east();
            boolean isConnNorth = this.isConnectedTo(northPos);
            boolean isConnSouth = this.isConnectedTo(southPos);
            boolean isConnWest = this.isConnectedTo(westPos);
            boolean isConnEast = this.isConnectedTo(eastPos);
            BlockAutoRailBase.RailDirection railDir = null;

            if (isConnNorth || isConnSouth) {
                railDir = BlockAutoRailBase.RailDirection.NORTH_SOUTH;
            }

            if (isConnWest || isConnEast) {
                railDir = BlockAutoRailBase.RailDirection.EAST_WEST;
            }

            if (isConnSouth && isConnEast && !isConnNorth && !isConnWest) {
                railDir = BlockAutoRailBase.RailDirection.SOUTH_EAST;
            }

            if (isConnSouth && isConnWest && !isConnNorth && !isConnEast) {
                railDir = BlockAutoRailBase.RailDirection.SOUTH_WEST;
            }

            if (isConnNorth && isConnWest && !isConnSouth && !isConnEast) {
                railDir = BlockAutoRailBase.RailDirection.NORTH_WEST;
            }

            if (isConnNorth && isConnEast && !isConnSouth && !isConnWest) {
                railDir = BlockAutoRailBase.RailDirection.NORTH_EAST;
            }

            if (railDir == BlockAutoRailBase.RailDirection.NORTH_SOUTH) {
                if (BlockAutoRailBase.isRailBlock(this.world, northPos.up())) {
                    railDir = BlockAutoRailBase.RailDirection.ASCENDING_NORTH;
                }

                if (BlockAutoRailBase.isRailBlock(this.world, southPos.up())) {
                    railDir = BlockAutoRailBase.RailDirection.ASCENDING_SOUTH;
                }
            }

            if (railDir == BlockAutoRailBase.RailDirection.EAST_WEST) {
                if (BlockAutoRailBase.isRailBlock(this.world, eastPos.up())) {
                    railDir = BlockAutoRailBase.RailDirection.ASCENDING_EAST;
                }

                if (BlockAutoRailBase.isRailBlock(this.world, westPos.up())) {
                    railDir = BlockAutoRailBase.RailDirection.ASCENDING_WEST;
                }
            }

            if (railDir == null) {
                railDir = BlockAutoRailBase.RailDirection.NORTH_SOUTH;
            }

            this.state = this.state.withProperty(this.block.getShapeProperty(),
                    railDir);
            this.world.setBlockState(this.pos, this.state, 3);
        }

        private boolean hasNeighborRail(BlockPos pos) {
            BlockAutoRailBase.ConnectionHelper rail = this.findRailAt(pos);

            if (rail == null) {
                return false;
            } else {
                rail.removeSoftConnections();
                return rail.canConnectTo(this);
            }
        }

        public BlockAutoRailBase.ConnectionHelper place(boolean forceUpdate) {
            BlockPos northPos = this.pos.north();
            BlockPos southPos = this.pos.south();
            BlockPos westPos = this.pos.west();
            BlockPos eastPos = this.pos.east();
            boolean hasNorth = this.hasNeighborRail(northPos);
            boolean hasSouth = this.hasNeighborRail(southPos);
            boolean hasWest = this.hasNeighborRail(westPos);
            boolean hasEast = this.hasNeighborRail(eastPos);
            BlockAutoRailBase.RailDirection railDirection = null;

            if ((hasNorth || hasSouth) && !hasWest && !hasEast) {
                railDirection = BlockAutoRailBase.RailDirection.NORTH_SOUTH;
            }

            if ((hasWest || hasEast) && !hasNorth && !hasSouth) {
                railDirection = BlockAutoRailBase.RailDirection.EAST_WEST;
            }

            if (hasSouth && hasEast && !hasNorth && !hasWest) {
                railDirection = BlockAutoRailBase.RailDirection.SOUTH_EAST;
            }

            if (hasSouth && hasWest && !hasNorth && !hasEast) {
                railDirection = BlockAutoRailBase.RailDirection.SOUTH_WEST;
            }

            if (hasNorth && hasWest && !hasSouth && !hasEast) {
                railDirection = BlockAutoRailBase.RailDirection.NORTH_WEST;
            }

            if (hasNorth && hasEast && !hasSouth && !hasWest) {
                railDirection = BlockAutoRailBase.RailDirection.NORTH_EAST;
            }

            if (railDirection == null) {
                if (hasNorth || hasSouth) {
                    railDirection = BlockAutoRailBase.RailDirection.NORTH_SOUTH;
                }

                if (hasWest || hasEast) {
                    railDirection = BlockAutoRailBase.RailDirection.EAST_WEST;
                }

                if (hasNorth && hasWest) {
                    railDirection = BlockAutoRailBase.RailDirection.NORTH_WEST;
                }

                if (hasEast && hasNorth) {
                    railDirection = BlockAutoRailBase.RailDirection.NORTH_EAST;
                }

                if (hasWest && hasSouth) {
                    railDirection = BlockAutoRailBase.RailDirection.SOUTH_WEST;
                }

                if (hasSouth && hasEast) {
                    railDirection = BlockAutoRailBase.RailDirection.SOUTH_EAST;
                }
            }

            if (railDirection == BlockAutoRailBase.RailDirection.NORTH_SOUTH) {
                if (BlockAutoRailBase.isRailBlock(this.world, northPos.up())) {
                    railDirection =
                            BlockAutoRailBase.RailDirection.ASCENDING_NORTH;
                }

                if (BlockAutoRailBase.isRailBlock(this.world, southPos.up())) {
                    railDirection =
                            BlockAutoRailBase.RailDirection.ASCENDING_SOUTH;
                }
            }

            if (railDirection == BlockAutoRailBase.RailDirection.EAST_WEST) {
                if (BlockAutoRailBase.isRailBlock(this.world, eastPos.up())) {
                    railDirection =
                            BlockAutoRailBase.RailDirection.ASCENDING_EAST;
                }

                if (BlockAutoRailBase.isRailBlock(this.world, westPos.up())) {
                    railDirection =
                            BlockAutoRailBase.RailDirection.ASCENDING_WEST;
                }
            }

            if (railDirection == null) {
                railDirection = BlockAutoRailBase.RailDirection.NORTH_SOUTH;
            }

            this.updateConnectedRails(railDirection);
            this.state = this.state.withProperty(this.block.getShapeProperty(),
                    railDirection);

            if (forceUpdate
                    || this.world.getBlockState(this.pos) != this.state) {
                this.world.setBlockState(this.pos, this.state, 3);

                for (int i = 0; i < this.connectedRails.size(); ++i) {
                    BlockAutoRailBase.ConnectionHelper rail =
                            this.findRailAt(this.connectedRails.get(i));

                    if (rail != null) {
                        rail.removeSoftConnections();

                        if (rail.canConnectTo(this)) {
                            rail.connectTo(this);
                        }
                    }
                }
            }

            return this;
        }

        public IBlockState getBlockState() {
            return this.state;
        }
    }

}
