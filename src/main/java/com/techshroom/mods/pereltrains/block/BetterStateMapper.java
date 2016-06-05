package com.techshroom.mods.pereltrains.block;

import com.techshroom.mods.pereltrains.util.GeneralUtility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;

public class BetterStateMapper extends StateMapperBase {

    private final String mrlId;

    public BetterStateMapper(String mrlId) {
        this.mrlId = mrlId;
    }

    @Override
    protected ModelResourceLocation
            getModelResourceLocation(IBlockState state) {
        return new ModelResourceLocation(GeneralUtility.addressMod(this.mrlId),
                this.getPropertyString(state.getProperties()));
    }

}
