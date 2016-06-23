package com.techshroom.mods.pereltrains.block.entity;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraftforge.client.model.animation.FastTESR;

public class TESRRailSegmentDisplay extends FastTESR<TileEntityAutoRailBase> {

    @Override
    public void renderTileEntityFast(TileEntityAutoRailBase te, double x,
            double y, double z, float partialTicks, int destroyStage,
            VertexBuffer vb) {
        vb.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(),
                z - te.getPos().getZ());
        vb.color(255, 0, 0, 255).pos(te.getPos().getX(),
                te.getPos().getY() + 0.2, te.getPos().getZ()).endVertex();
        vb.color(255, 0, 0, 255).pos(te.getPos().getX() + 1,
                te.getPos().getY() + 0.2, te.getPos().getZ()).endVertex();
        vb.color(255, 0, 0, 255).pos(te.getPos().getX() + 1,
                te.getPos().getY() + 0.2, te.getPos().getZ() + 1).endVertex();
        vb.color(255, 0, 0, 255).pos(te.getPos().getX(),
                te.getPos().getY() + 0.2, te.getPos().getZ() + 1).endVertex();
    }

}
