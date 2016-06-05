package com.techshroom.mods.pereltrains;

import com.techshroom.mods.pereltrains.block.ExtendedBlock;
import com.techshroom.mods.pereltrains.block.PerelBlocks;
import com.techshroom.mods.pereltrains.util.GeneralUtility;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class PerelProxy {

    public static final class Client extends PerelProxy {

        @Override
        public void onPreInit() {
            super.onPreInit();
            for (Block block : ExtendedBlock.getRegisteredBlocks()) {
                ModelLoader.setCustomModelResourceLocation(
                        Item.getItemFromBlock(block), 0,
                        new ModelResourceLocation(
                                GeneralUtility
                                        .addressMod(block.getUnlocalizedName()),
                                "inventory"));
            }
        }

    }

    public void onPreInit() {
        PerelBlocks.load();
    }

}
