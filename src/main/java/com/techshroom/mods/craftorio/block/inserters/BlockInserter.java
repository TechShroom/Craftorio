package com.techshroom.mods.craftorio.block.inserters;

import com.techshroom.mods.craftorio.block.ExtendedBlock;
import com.techshroom.mods.craftorio.util.GeneralUtility;
import com.techshroom.mods.craftorio.util.Rotation;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockInserter extends ExtendedBlock implements ITileEntityProvider {

    public static final PropertyEnum<Rotation> ROTATION = PropertyEnum.create("rotation", Rotation.class);
    public static final PropertyDirection ATTACHED_DIRECTION = PropertyDirection.create("attached");

    public static EnumFacing getInserterFacing(IBlockState state) {
        return getInserterFacing(state.getValue(ATTACHED_DIRECTION), state.getValue(ROTATION));
    }

    public static EnumFacing getInserterFacing(EnumFacing attachedBlock, Rotation rotation) {
        return rotation.rotate(getDefaultFacing(attachedBlock));
    }

    private static EnumFacing getDefaultFacing(EnumFacing attachedBlock) {
        // Use opposite axis to axis that travels though the face
        switch (attachedBlock) {
            case EAST:
            case WEST:
                // x axis
                return EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, Axis.Z);
            // case NORTH:
            // case SOUTH:
            // case DOWN:
            // case UP:
            default:
                // y/z axis -- use X for both
                return EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, Axis.X);
        }
    }

    protected BlockInserter(String unlocalizedName) {
        super(Material.IRON, unlocalizedName);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        // 4 values -> 2 bits
        meta &= state.getValue(ROTATION).ordinal();
        // 6 values -> 3 bits
        meta &= state.getValue(ATTACHED_DIRECTION).ordinal() << 2;
        return meta;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ROTATION, Rotation.values()[meta & 4]).withProperty(ATTACHED_DIRECTION,
                EnumFacing.values()[(meta >> 2) & 6]);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(ROTATION, ATTACHED_DIRECTION).build();
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return withRotation(state, mirrorIn.toRotation(getInserterFacing(state)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState withRotation(IBlockState state, net.minecraft.util.Rotation rot) {
        return state.withProperty(ROTATION, state.getValue(ROTATION).add(Rotation.fromNMURotation(rot)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        TileEntityInserter inserter = GeneralUtility.castOrNull(worldIn.getTileEntity(pos), TileEntityInserter.class);
        if (inserter != null) {
            inserter.recalculateNeighbors();
        }
    }

    @Override
    public abstract TileEntityInserter createNewTileEntity(World worldIn, int meta);

}
