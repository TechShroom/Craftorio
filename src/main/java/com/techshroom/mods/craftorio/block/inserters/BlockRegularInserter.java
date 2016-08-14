package com.techshroom.mods.craftorio.block.inserters;

import java.awt.Color;

import net.minecraft.world.World;

public class BlockRegularInserter extends BlockInserter {
    
    private static final int COLOR = Color.GRAY.getRGB();

    public BlockRegularInserter() {
        super("inserter.regular");
    }

    @Override
    public TileEntityInserter createNewTileEntity(World worldIn, int meta) {
        TileEntityInserter te = new TileEntityInserter();
        te.setTransferMax(1);
        te.setColor(COLOR);
        return te;
    }

}
