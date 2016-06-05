package com.techshroom.mods.pereltrains.block;

import com.techshroom.mods.pereltrains.block.entity.TileEntityRailSignal;
import com.techshroom.mods.pereltrains.util.BlockLocator;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRailSignal extends ExtendedBlock
        implements ITileEntityProvider {

    private static final BlockLocator RAIL_LOCATOR =
            BlockLocator.forBlock(Blocks.RAIL).checkAllStates(true);

    protected BlockRailSignal() {
        super(Material.IRON, "rail_signal");
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityRailSignal();
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (!RAIL_LOCATOR.touchingHorizontal(worldIn, pos)) {
            return false;
        }
        return super.canPlaceBlockAt(worldIn, pos);
    }

}
