package com.techshroom.mods.pereltrains.block;

import com.techshroom.mods.pereltrains.Constants;
import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.block.entity.TileEntityRailSignal;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class PerelBlocks {

    public static final CreativeTabs TAB = new CreativeTabs("perel") {

        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(RAIL_SIGNAL);
        }

    };

    public static final BlockRailSignal RAIL_SIGNAL = new BlockRailSignal();

    public static void load() {
        // Doesn't actually do anything.
        register(new BlockRailSignal(), "rail_signal");
        GameRegistry.registerTileEntity(TileEntityRailSignal.class,
                "rail_signal");
    }

    private static void register(Block block, String name) {
        block.setRegistryName(Constants.MOD_ID, name);
        GameRegistry.register(block);
        GameRegistry.register(
                new ItemBlock(block).setRegistryName(Constants.MOD_ID, name));
        PerelTrains.getLogger().info("I have registered "
                + block.getRegistryName() + "/" + block.getUnlocalizedName());
    }

    private PerelBlocks() {
    }

}
