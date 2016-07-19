/*
 * This file is part of Craftorio, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshoom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.techshroom.mods.craftorio.block.entity;

import java.awt.Color;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import com.google.common.base.Throwables;
import com.techshroom.mods.craftorio.segment.Segment;
import com.techshroom.mods.craftorio.util.GeneralUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TESRRailSegmentDisplay extends FastTESR<TileEntityAutoRailBase> {

    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private final MessageDigest md5;
    {
        try {
            this.md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            throw Throwables.propagate(e);
        }
    }

    private TextureAtlasSprite whiteness;

    // Algorithm from Textual IRC Client.
    // License in LICENSE-textual.txt
    private int[] notRandomColorShift(int id) {
        int stringHash32 = Math.abs(new BigInteger(1,
                this.md5.digest(GeneralUtility.intToBytes(id))).intValue());
        int shash = stringHash32 >> 1;
        int lhash = stringHash32 >> 2;

        int h = (stringHash32 % 360);

        int s;
        int l;

        s = (shash % 50 + 35); // 35 - 85
        l = (lhash % 38 + 20); // 20 - 58

        // Lower lightness for Yello, Green, Cyan
        if (h > 45 && h <= 195) {
            l = (lhash % 21 + 20); // 20 - 41

            if (l > 31) {
                s = (shash % 40 + 55); // 55 - 95
            } else {
                s = (shash % 35 + 65); // 65 - 95
            }
        }

        // Give the reds a bit more saturation
        if (h <= 25 || h >= 335) {
            s = (shash % 33 + 45); // 45 - 78
        }

        // Increase lightness for brighter colors...
        l += 15;
        l = Math.min(100, l);
        // SATUREATE
        s += 200;
        s = Math.min(360, s);

        return new int[] { h, s, l };
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
        int[] hsl = notRandomColorShift(id);
        Color segColor = GeneralUtility.hslToColor(hsl[0], hsl[1], hsl[2]);
        Color lighter =
                GeneralUtility.hslToColor(hsl[0] + 10, hsl[1], hsl[2] - 5);
        Color connector =
                GeneralUtility.hslToColor(hsl[0] - 10, hsl[1], hsl[2] + 5);

        drawFlatTop(te, vb, segColor, 4 / 16.0, 4 / 16.0);
        drawFlatTop(te, vb, lighter, 6 / 16.0, 6 / 16.0);

        for (BlockPos conn : te.getConnections()) {
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                if (te.getPos().offset(facing).equals(conn)) {
                    drawConnector(vb, te.getPos(), facing, connector);
                }
            }
        }
    }

    private void drawConnector(VertexBuffer vb, BlockPos pos, EnumFacing travel,
            Color a) {
        final double width = 1 / 16.0;
        final double notWidth = 8 / 16.0 - width;
        TextureAtlasSprite w = this.whiteness;

        double x1;
        double x2;
        double z1;
        double z2;
        double y1 = pos.getY() + 5 / 16.0;
        if (travel.getAxis() == Axis.X) {
            x1 = pos.getX();
            x2 = pos.getX() + 8 / 16.0;
            if (travel.getAxisDirection() == AxisDirection.POSITIVE) {
                x1 += 8 / 16.0;
                x2 += 8 / 16.0;
            }
            z1 = pos.getZ() + notWidth;
            z2 = pos.getZ() + 1 - notWidth;

            box(vb, x1, y1, z1, x2, y1 - 1 / 16.0, z2, a, w.getMinU(),
                    w.getMinV(), w.getMaxU(), w.getMaxV());
        } else if (travel.getAxis() == Axis.Z) {
            z1 = pos.getZ();
            z2 = pos.getZ() + 8 / 16.0;
            if (travel.getAxisDirection() == AxisDirection.POSITIVE) {
                z1 += 8 / 16.0;
                z2 += 8 / 16.0;
            }
            x1 = pos.getX() + notWidth;
            x2 = pos.getX() + 1 - notWidth;

            box(vb, x1, y1, z1, x2, y1 - 1 / 16.0, z2, a, w.getMinU(),
                    w.getMinV(), w.getMaxU(), w.getMaxV());
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
        box(vb, xv1, yv1, zv1, xv2, yv1 - 1.5 / 16.0, zv2, color, w.getMinU(),
                w.getMinV(), w.getMaxU(), w.getMaxV());
    }

    private void box(VertexBuffer vb, double x1, double y1, double z1,
            double x2, double y2, double z2, Color color, double u1, double v1,
            double u2, double v2) {

        // Top
        vertex(vb, x1, y1, z1, color, u1, v1);
        vertex(vb, x2, y1, z1, color, u1, v2);
        vertex(vb, x2, y1, z2, color, u2, v2);
        vertex(vb, x1, y1, z2, color, u2, v1);

        // Bottom
        vertex(vb, x1, y2, z1, color, u1, v1);
        vertex(vb, x2, y2, z1, color, u1, v2);
        vertex(vb, x2, y2, z2, color, u2, v2);
        vertex(vb, x1, y2, z2, color, u2, v1);

        // Side A
        vertex(vb, x1, y1, z1, color, u1, v1);
        vertex(vb, x1, y2, z1, color, u1, v2);
        vertex(vb, x1, y2, z2, color, u2, v2);
        vertex(vb, x1, y1, z2, color, u2, v1);

        // Side B
        vertex(vb, x2, y1, z1, color, u1, v1);
        vertex(vb, x2, y2, z1, color, u1, v2);
        vertex(vb, x2, y2, z2, color, u2, v2);
        vertex(vb, x2, y1, z2, color, u2, v1);

        // Side C
        vertex(vb, x1, y1, z1, color, u1, v1);
        vertex(vb, x1, y2, z1, color, u1, v2);
        vertex(vb, x2, y2, z1, color, u2, v2);
        vertex(vb, x2, y1, z1, color, u2, v1);

        // Side D
        vertex(vb, x1, y1, z2, color, u1, v1);
        vertex(vb, x1, y2, z2, color, u1, v2);
        vertex(vb, x2, y2, z2, color, u2, v2);
        vertex(vb, x2, y1, z2, color, u2, v1);
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
