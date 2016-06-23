package com.techshroom.mods.pereltrains.block.entity;

import com.google.common.eventbus.Subscribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TESRRailSegmentDisplay extends FastTESR<TileEntityAutoRailBase> {

    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private TextureAtlasSprite whiteness;

    @SubscribeEvent
    public void onReload(TextureStitchEvent.Post postStitch) {
        TextureMap textureMapBlocks =
                Minecraft.getMinecraft().getTextureMapBlocks();
        if (postStitch.getMap() == textureMapBlocks) {
            this.whiteness = textureMapBlocks.getAtlasSprite("minecraft:white");
        }
    }

    @Override
    public void renderTileEntityFast(TileEntityAutoRailBase te, double x,
            double y, double z, float partialTicks, int destroyStage,
            VertexBuffer vb) {
        if (this.whiteness == null) {
            throw new IllegalStateException("how can i be white");
        }
        vb.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(),
                z - te.getPos().getZ());
        vb.pos(te.getPos().getX(), te.getPos().getY() + 0.2, te.getPos().getZ())
                .color(255, 255, 255, 255)
                .tex(this.whiteness.getMinU(), this.whiteness.getMinV())
                .lightmap(0, 0).endVertex();
        vb.pos(te.getPos().getX() + 1, te.getPos().getY() + 0.2,
                te.getPos().getZ()).color(255, 255, 255, 255)
                .tex(this.whiteness.getMaxU(), this.whiteness.getMinV())
                .lightmap(0, 0).endVertex();
        vb.pos(te.getPos().getX() + 1, te.getPos().getY() + 0.2,
                te.getPos().getZ() + 1).color(255, 255, 255, 255)
                .tex(this.whiteness.getMaxU(), this.whiteness.getMaxV())
                .lightmap(0, 0).endVertex();
        vb.pos(te.getPos().getX(), te.getPos().getY() + 0.2,
                te.getPos().getZ() + 1).color(255, 255, 255, 255)
                .tex(this.whiteness.getMinU(), this.whiteness.getMaxV())
                .lightmap(0, 0).endVertex();
    }

}
