package com.techshroom.mods.pereltrains.block.entity;

import java.awt.Color;
import java.util.Optional;
import java.util.Random;

import com.techshroom.mods.pereltrains.segment.Segment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TESRRailSegmentDisplay extends FastTESR<TileEntityAutoRailBase> {

    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private TextureAtlasSprite whiteness;
    private Random random = new Random();

    private Color notRandomColorShift(int id) {
        this.random.setSeed(id);
        int r = this.random.nextInt(256);
        int g = this.random.nextInt(256);
        int b = this.random.nextInt(256);
        int a = 255;
        return new Color(r, g, b, a);
    }

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
        int id = Optional.ofNullable(te.getSegment()).map(Segment::getId)
                .orElse(Integer.MIN_VALUE);
        this.random.setSeed(te.getPos().toLong());
        id = this.random.nextInt(10000);
        Color segColor = notRandomColorShift(id);
        Color lighter = segColor.brighter().brighter();

        drawFlatTop(te, vb, segColor, 0.3, 0.2);
        drawFlatTop(te, vb, lighter, 0.4, 0.3);

        for (BlockPos conn : te.getConnections()) {
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                if (te.getPos().offset(facing).equals(conn)) {
                    drawConnector(vb, te.getPos(), facing, segColor);
                }
            }
        }
    }

    private void drawConnector(VertexBuffer vb, BlockPos pos, EnumFacing travel,
            Color a) {
        final double width = 0.15;
        final double notWidth = 0.5 - width;
        TextureAtlasSprite w = this.whiteness;

        double x1;
        double x2;
        double z1;
        double z2;
        double y1 = pos.getY() + 0.25;
        if (travel.getAxis() == Axis.X) {
            x1 = pos.getX();
            x2 = pos.getX() + 0.5;
            if (travel.getAxisDirection() == AxisDirection.POSITIVE) {
                x1 += 0.5;
                x2 += 0.5;
            }
            z1 = pos.getZ() + notWidth;
            z2 = pos.getZ() + 1 - notWidth;

            vertex(vb, x1, y1, z1, a, w.getMinU(), w.getMinV());
            vertex(vb, x2, y1, z1, a, w.getMaxU(), w.getMinV());
            vertex(vb, x2, y1, z2, a, w.getMaxU(), w.getMaxV());
            vertex(vb, x1, y1, z2, a, w.getMinU(), w.getMaxV());
        } else if (travel.getAxis() == Axis.Z) {
            z1 = pos.getZ();
            z2 = pos.getZ() + 0.5;
            if (travel.getAxisDirection() == AxisDirection.POSITIVE) {
                z1 += 0.5;
                z2 += 0.5;
            }
            x1 = pos.getX() + notWidth;
            x2 = pos.getX() + 1 - notWidth;

            vertex(vb, x1, y1, z1, a, w.getMinU(), w.getMinV());
            vertex(vb, x2, y1, z1, a, w.getMaxU(), w.getMinV());
            vertex(vb, x2, y1, z2, a, w.getMaxU(), w.getMaxV());
            vertex(vb, x1, y1, z2, a, w.getMinU(), w.getMaxV());
        } else {
            // ????
            throw new IllegalStateException("Y axis not expected");
        }
    }

    private void drawFlatTop(TileEntityAutoRailBase te, VertexBuffer vb,
            Color color, double shrink, double yoff) {
        double xv1 = te.getPos().getX() + shrink;
        double xv2 = te.getPos().getX() + 1 - shrink;
        double zv1 = te.getPos().getZ() + shrink;
        double zv2 = te.getPos().getZ() + 1 - shrink;
        double yv1 = te.getPos().getY() + yoff;
        TextureAtlasSprite w = this.whiteness;
        vertex(vb, xv1, yv1, zv1, color, w.getMinU(), w.getMinV());
        vertex(vb, xv2, yv1, zv1, color, w.getMaxU(), w.getMinV());
        vertex(vb, xv2, yv1, zv2, color, w.getMaxU(), w.getMaxV());
        vertex(vb, xv1, yv1, zv2, color, w.getMinU(), w.getMaxV());
    }

    private void vertex(VertexBuffer vb, double x, double y, double z,
            Color color, double u, double v) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();
        vb.pos(x, y, z).color(r, g, b, a).tex(u, v).lightmap(240, 240)
                .endVertex();
    }

}
