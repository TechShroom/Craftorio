package com.techshroom.mods.pereltrains;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldAbstractionImpl implements WorldAbstraction {

    public static WorldAbstraction get(World world) {
        // TODO caching?
        return new WorldAbstractionImpl(world);
    }

    private final World world;

    private WorldAbstractionImpl(World world) {
        this.world = world;
    }

    @Override
    public Object get(BlockPos pos) {
        return this.world.getTileEntity(pos);
    }

}
