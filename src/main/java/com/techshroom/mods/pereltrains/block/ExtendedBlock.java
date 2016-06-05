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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.techshroom.mods.pereltrains.util.GeneralUtility;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    private FacingStyle facingStyle = FacingStyle.NONE;
    private BlockStateContainer blockState = super.getBlockState();

    protected ExtendedBlock(Material mat, String unlocalizedName) {
        super(mat);
        setUnlocalizedName(unlocalizedName);
        // Use the same name. Unique.
        setRegistryName(unlocalizedName);
        ModelLoader.setCustomStateMapper(this,
                new BetterStateMapper(unlocalizedName));
    }

    protected ExtendedBlock(String unlocalizedName) {
        this(Material.IRON, unlocalizedName);
    }

    protected static enum FacingStyle {
        NONE, ALL, HORIZONTAL;
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (this.facingStyle == FacingStyle.NONE) {
            return super.getStateFromMeta(meta);
        } else {
            return getBlockState().getBaseState().withProperty(
                    (this.facingStyle == FacingStyle.ALL
                            ? GeneralUtility.PROP_FACING
                            : GeneralUtility.PROP_FACING_HORIZ),
                    EnumFacing.getFront(meta));
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (this.facingStyle == FacingStyle.NONE) {
            return super.getMetaFromState(state);
        } else {
            return state
                    .getValue((this.facingStyle == FacingStyle.ALL
                            ? GeneralUtility.PROP_FACING
                            : GeneralUtility.PROP_FACING_HORIZ))
                    .getIndex();
        }
    }

    protected ExtendedBlock setFacingStyle(FacingStyle style) {
        checkNotNull(style);
        if (style == FacingStyle.ALL) {
            setDefaultState(GeneralUtility.getSideBaseState(this));
        }
        if (style == FacingStyle.HORIZONTAL) {
            setDefaultState(GeneralUtility.getSideBaseNoYAxisState(this));
        }
        this.facingStyle = style;
        this.blockState = createBlockState();
        return this;
    }

    public FacingStyle getFacingStyle() {
        return this.facingStyle;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        // null -> early block constr. call
        if (this.facingStyle == null || this.facingStyle == FacingStyle.NONE) {
            return GeneralUtility.getNoFacingBlockState(this);
        } else if (this.facingStyle == FacingStyle.ALL) {
            return GeneralUtility.getSideBlockState(this);
        } else if (this.facingStyle == FacingStyle.HORIZONTAL) {
            return GeneralUtility.getSideBlockNoYAxisState(this);
        } else {
            throw new UnsupportedOperationException(
                    String.valueOf(this.facingStyle));
        }
    }

    @Override
    public BlockStateContainer getBlockState() {
        return this.blockState;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos,
            EnumFacing side, float hitX, float hitY, float hitZ, int meta,
            EntityLivingBase placer) {
        if (this.facingStyle != FacingStyle.NONE) {
            EnumFacing facing = null;
            IProperty<EnumFacing> prop = null;
            if (this.facingStyle == FacingStyle.ALL) {
                facing = GeneralUtility.getFacing(pos, placer);
                prop = GeneralUtility.PROP_FACING;
            } else if (this.facingStyle == FacingStyle.HORIZONTAL) {
                facing = GeneralUtility.getHorizontalFacing(placer);
                prop = GeneralUtility.PROP_FACING_HORIZ;
            } else {
                throw new UnsupportedOperationException(
                        String.valueOf(this.facingStyle));
            }
            return createBlockState().getBaseState().withProperty(prop, facing);
        }
        return createBlockState().getBaseState();
    }

}
