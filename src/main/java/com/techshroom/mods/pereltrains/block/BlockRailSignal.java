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

import javax.annotation.Nullable;

import com.techshroom.mods.pereltrains.Constants;
import com.techshroom.mods.pereltrains.block.entity.TileEntityRailSignal;
import com.techshroom.mods.pereltrains.util.BlockLocator;
import com.techshroom.mods.pereltrains.util.GeneralUtility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRailSignal extends ExtendedBlock {

    public static final PropertyEnum<LightValue> LIGHT_PROPERTY = PropertyEnum
            .create(Constants.LIGHT_PROPERTY_NAME, LightValue.class);
    private static final PropertyEnum<EnumFacing> FACING_PRORERTY =
            BlockHorizontal.FACING;
    public static final PropertyEnum<EnumFacing> ATTACHED_RAIL_PROPERTY =
            PropertyDirection.create("attached", EnumFacing.Plane.HORIZONTAL);
    private static final double BLOCK_WIDTH = 16;
    private static final AxisAlignedBB AABB =
            new AxisAlignedBB(4 / BLOCK_WIDTH, 0 / BLOCK_WIDTH, 4 / BLOCK_WIDTH,
                    12 / BLOCK_WIDTH, 14 / BLOCK_WIDTH, 12 / BLOCK_WIDTH);

    protected BlockRailSignal() {
        super(Material.IRON, "rail_signal");
        IBlockState defaultState = getDefaultState();
        setDefaultState(defaultState.withProperty(FACING_PRORERTY,
                calculateFacingFromState(defaultState)));
        System.err.println("THE DEFAULT STATE " + getDefaultState());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this)
                .add(LIGHT_PROPERTY, FACING_PRORERTY, ATTACHED_RAIL_PROPERTY)
                .build();
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        int light = meta & 0b11;
        int attached = (meta >> 2) & 0b11;
        return getDefaultState()
                .withProperty(LIGHT_PROPERTY, LightValue.values()[light])
                .withProperty(ATTACHED_RAIL_PROPERTY,
                        EnumFacing.HORIZONTALS[attached]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LIGHT_PROPERTY).ordinal() + (state
                .getValue(ATTACHED_RAIL_PROPERTY).getHorizontalIndex() << 2);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn,
            BlockPos pos) {
        return state.withProperty(FACING_PRORERTY,
                calculateFacingFromState(state));
    }

    private EnumFacing calculateFacingFromState(IBlockState state) {
        EnumFacing railFace = state.getValue(ATTACHED_RAIL_PROPERTY);
        return GeneralUtility.getSignalFacing(railFace);
    }

    private EnumFacing findRail(IBlockAccess worldIn, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            IBlockState state = worldIn.getBlockState(pos.offset(facing));
            if (BlockAutoRailBase.isRailBlock(state)) {
                return facing;
            }
        }
        throw new IllegalStateException("No rail near " + pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState,
            World worldIn, BlockPos pos) {
        return AABB;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source,
            BlockPos pos) {
        return AABB;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (!validatePosition(worldIn, pos, null)) {
            return false;
        }
        return super.canPlaceBlockAt(worldIn, pos);
    }

    private boolean validatePosition(IBlockAccess worldIn, BlockPos pos,
            @Nullable IBlockState placedState) {
        if (!BlockLocator.forBlock(PerelBlocks.NORMAL_RAIL).checkAllStates(true)
                .touchingHorizontal(worldIn, pos)) {
            return false;
        }
        if (placedState != null) {
            // we can validate our rail here
            if (!BlockAutoRailBase.isRailBlock(worldIn.getBlockState(pos
                    .offset(placedState.getValue(ATTACHED_RAIL_PROPERTY))))) {
                // no rail
                return false;
            }
        }
        IBlockState state = worldIn.getBlockState(pos.down());
        // We're like...a torch, right?
        if (!state.getBlock().canPlaceTorchOnTop(state, worldIn, pos)) {
            return false;
        }
        return true;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos,
            EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
            EntityLivingBase placer) {
        IBlockState base = super.onBlockPlaced(worldIn, pos, facing, hitX, hitY,
                hitZ, meta, placer);

        // add the attached rail now
        EnumFacing railFacingSignal = findRail(worldIn, pos);
        base = base.withProperty(ATTACHED_RAIL_PROPERTY, railFacingSignal);
        return base;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
            Block blockIn) {
        if (!validatePosition(world, pos, state)) {
            // explode!
            world.destroyBlock(pos, true);
        }
        TileEntityRailSignal te =
                ((TileEntityRailSignal) world.getTileEntity(pos));
        if (te != null) {
            te.recalculateLighting();
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world,
            BlockPos pos) {
        return state.getValue(LIGHT_PROPERTY) == LightValue.NONE ? 0 : 10;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    protected String getInventoryVariant() {
        return GeneralUtility.getVariantString(getDefaultState());
    }

}
