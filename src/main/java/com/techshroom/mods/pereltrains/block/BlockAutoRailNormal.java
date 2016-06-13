package com.techshroom.mods.pereltrains.block;

import com.techshroom.mods.pereltrains.block.entity.TileEntityAutoRailNormal;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.world.World;

public class BlockAutoRailNormal extends BlockAutoRailBase {

    private static final IProperty<RailDirection> SHAPE =
            PropertyEnum.create("shape", RailDirection.class);

    protected BlockAutoRailNormal() {
        super("normal_rail");
    }

    @Override
    public TileEntityAutoRailNormal createNewTileEntity(World worldIn,
            int meta) {
        return new TileEntityAutoRailNormal();
    }

    @Override
    public IProperty<RailDirection> getShapeProperty() {
        return SHAPE;
    }

}
