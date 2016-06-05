/*
 * This file is part of PerelTrains, licensed under the MIT License (MIT).
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
package com.techshroom.mods.pereltrains.util;

import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.techshroom.mods.pereltrains.Constants;
import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.block.ExtendedBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockSourceImpl;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class GeneralUtility {

    @SideOnly(Side.CLIENT)
    public static final class Client {

        public static boolean buttonIsPressed(int id, GuiButton check) {
            return check.enabled && check.id == id;
        }

        private Client() {
            throw new AssertionError("Nope.");
        }
    }

    public static final class IBS {

        private static class DistSort extends BlockSourceImpl
                implements Comparable<DistSort> {

            private int dSq = 0;

            public DistSort(World par1World, BlockPos pos, IBlockSource from) {
                super(par1World, pos);
                this.dSq = distSq(this, from);
            }

            @Override
            public int compareTo(DistSort o) {
                if (IBS.equal(o, this)) {
                    return 0;
                }
                if (this.dSq > o.dSq) {
                    return 1;
                }
                return -1;
            }

        }

        public static Block block(IBlockSource ibs) {
            if (ibs == null) {
                return null;
            }

            return ibs.getWorld().getBlockState(ibs.getBlockPos()).getBlock();
        }

        public static String
                collectionAsString(Collection<? extends IBlockSource> c) {
            List<String> out = new ArrayList<String>(c.size());
            Iterator<? extends IBlockSource> i = c.iterator();
            while (i.hasNext()) {
                out.add(string(i.next()));
            }
            return out.toString();
        }

        @SuppressWarnings("unchecked")
        private static <T extends IBlockSource> T constr(Class<T> ibs, World w,
                BlockPos pos) {
            try {
                Class<? extends IBlockSource> ibsClass = ibs;
                if (override != null) {
                    ibsClass = override;
                }
                Constructor<? extends IBlockSource> ibsConstr =
                        constrCache.get(ibsClass);
                if (ibsConstr == null) {
                    try {
                        ibsConstr = ibsClass.getConstructor(World.class,
                                BlockPos.class);
                        ibsConstr.setAccessible(true);
                    } catch (SecurityException e) {
                        System.err.println("[techshroom-util] "
                                + "SecurityException caught, falling back: "
                                + e.getMessage());
                    } catch (NoSuchMethodException e) {
                        System.err.println("[techshroom-util] " + "Class "
                                + ibsClass + " does not expose a constructor"
                                + " with the parameters [World, BlockPos]!"
                                + " Falling back to BlockSourceImpl.");
                    }
                    if (ibsConstr == null) {
                        ibsConstr = constrCache.get(BlockSourceImpl.class);
                    }
                }
                constrCache.put(ibsClass, ibsConstr);
                try {
                    return (T) ibsConstr.newInstance(w, pos);
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (InstantiationException e) {
                    throw e;
                } catch (IllegalAccessException e) {
                    throw e;
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            } catch (RuntimeException re) {
                throw re;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        public static boolean contains(List<? extends IBlockSource> list,
                IBlockSource check) {
            return IBS.indexOf(list, check) != -1;
        }

        public static int distSq(IBlockSource o1, IBlockSource o2) {
            // Guaranteed int (int * int + int * int + int * int)
            return (int) o1.getBlockPos().distanceSq(o2.getBlockPos());
        }

        public static boolean equal(IBlockSource ibs1, IBlockSource ibs2) {
            return ibs1 != null && ibs2 != null
                    && (ibs1 == ibs2 || (ibs1.getWorld() == ibs2.getWorld()
                            && ibs1.getBlockPos().equals(ibs2.getBlockPos())));
        }

        public static int indexOf(List<? extends IBlockSource> list,
                IBlockSource check) {
            if (list != null) {
                int index = 0;
                for (IBlockSource c : list) {
                    if (equal(check, c)) {
                        return index;
                    }
                    index++;
                }
            }
            return -1;
        }

        public static <T extends IBlockSource> IBlockSource[] neighbors(T ibs) {
            IBlockSource[] n = new IBlockSource[EnumFacing.values().length];
            Class<? extends IBlockSource> c = ibs.getClass();
            for (int i = 0; i < n.length; i++) {
                EnumFacing facing = EnumFacing.getFront(i);
                n[i] = constr(c, ibs.getWorld(),
                        ibs.getBlockPos().offset(facing));
            }
            return n;
        }

        public static void setOverride(Class<? extends IBlockSource> c) {
            override = c;
        }

        @SuppressWarnings("unchecked")
        public static <T extends IBlockSource> List<T>
                sortByDistFrom(IBlockSource loc, List<T> list) {
            List<DistSort> sort = new ArrayList<DistSort>();
            for (int i = 0; i < list.size(); i++) {
                T t = list.get(i);
                DistSort nt = new DistSort(t.getWorld(), t.getBlockPos(), loc);
                sort.add(nt);
            }
            Collections.sort(sort);
            for (int i = 0; i < sort.size(); i++) {
                T t = list.get(i);
                DistSort d = sort.get(i);
                list.set(i, (T) constr(t.getClass(), d.getWorld(),
                        d.getBlockPos()));
            }
            return list;
        }

        public static String string(IBlockSource ibs) {
            if (ibs == null) {
                return String.valueOf(null);
            }
            return ibs.getClass().getSimpleName() + "["
                    + String.format("world=%s,pos=%s",
                            ibs.getWorld().getWorldInfo().getWorldName(),
                            ibs.getBlockPos())
                    + "]";
        }

        public static void unsetOverride() {
            override = null;
        }

        private static Map<Class<? extends IBlockSource>, Constructor<? extends IBlockSource>> constrCache =
                new HashMap<Class<? extends IBlockSource>, Constructor<? extends IBlockSource>>();

        static {
            Constructor<BlockSourceImpl> constr =
                    cast(BlockSourceImpl.class.getDeclaredConstructors()[0]);
            constrCache.put(BlockSourceImpl.class, constr);
            constrCache.put(IBlockSource.class, constr);
        }

        private static Class<? extends IBlockSource> override = null;

        private IBS() {
            throw new AssertionError("Nope.");
        }
    }

    public static final class SetBlockFlag {

        public static final int UPDATE = 1, SEND = 2, DONT_RE_RENDER = 4;
        public static final int UPDATE_AND_SEND = UPDATE | SEND;
        public static final int SEND_AND_DONT_RE_RENDER = SEND | DONT_RE_RENDER;
        public static final int UPDATE_AND_DONT_RE_RENDER =
                UPDATE | DONT_RE_RENDER;
        public static final int UPDATE_SEND_AND_DONT_RE_RENDER =
                UPDATE | SEND | DONT_RE_RENDER;

        private SetBlockFlag() {
            throw new AssertionError("Nope.");
        }
    }

    public static final class SideConstants {

        public static final int BOTTOM = EnumFacing.DOWN.getIndex();
        public static final int TOP = EnumFacing.UP.getIndex();
        public static final int NORTH = EnumFacing.NORTH.getIndex();
        public static final int SOUTH = EnumFacing.SOUTH.getIndex();
        public static final int WEST = EnumFacing.WEST.getIndex();
        public static final int EAST = EnumFacing.EAST.getIndex();

        private SideConstants() {
            throw new AssertionError("Nope.");
        }
    }

    public static final class Time {

        /**
         * @deprecated {@link TimeUnit}
         */
        @Deprecated
        public static int minutesAsSeconds(int minutes) {
            return minutes * 60;
        }

        public static int minutesAsTicks(int minutes) {
            return secondsAsTicks(minutesAsSeconds(minutes));
        }

        public static int secondsAsTicks(int seconds) {
            return seconds * 20;
        }

        private Time() {
            throw new AssertionError("Nope.");
        }
    }

    /**
     * Generates an array of length {@code len}, with each element equal to its
     * index.
     */
    public static int[] indexEqualsIndexArray(int len) {
        int[] out = new int[len];
        for (int i = 0; i < out.length; i++) {
            out[i] = i;
        }
        return out;
    }

    public static String address(String id, String object) {
        return id + ":" + object;
    }

    public static String addressMod(String object) {
        return address(Constants.MOD_ID, object);
    }

    private static final int[] _c;
    private static final int[] cc;

    static {
        cc = new int[6];
        _c = new int[6];
        _c[SideConstants.TOP] = cc[SideConstants.TOP] = SideConstants.TOP;
        _c[SideConstants.BOTTOM] =
                cc[SideConstants.BOTTOM] = SideConstants.BOTTOM;
        _c[SideConstants.NORTH] = SideConstants.EAST;
        _c[SideConstants.EAST] = SideConstants.SOUTH;
        _c[SideConstants.SOUTH] = SideConstants.WEST;
        _c[SideConstants.WEST] = SideConstants.NORTH;

        cc[SideConstants.NORTH] = SideConstants.WEST;
        cc[SideConstants.WEST] = SideConstants.SOUTH;
        cc[SideConstants.SOUTH] = SideConstants.EAST;
        cc[SideConstants.EAST] = SideConstants.NORTH;
    }

    public static int clockwise(int side) {
        return _c[side];
    }

    public static int counterClockwise(int side) {
        return cc[side];
    }

    public static void drawBackground(GuiScreen gui, int xoff, int yoff, int u,
            int v, int w, int h) {
        gui.drawTexturedModalRect(xoff + u, yoff + v, u, v, w, h);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }

    private static final Map<ExtendedBlock, BlockStateContainer> NO_FACING_STATE =
            new HashMap<>();
    private static final Map<ExtendedBlock, BlockStateContainer> SIDE_STATE =
            new HashMap<>();
    private static final Map<ExtendedBlock, BlockStateContainer> SIDE_STATE_NO_Y_AXIS =
            new HashMap<>();
    public static final PropertyDirection PROP_FACING =
            PropertyDirection.create("facing");
    public static final PropertyDirection PROP_FACING_HORIZ =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private static BlockStateContainer
            getOrCreateNoFacingState(ExtendedBlock block) {
        return NO_FACING_STATE.computeIfAbsent(block, BlockStateContainer::new);
    }

    private static BlockStateContainer
            getOrCreateSideState(ExtendedBlock block) {
        return SIDE_STATE.computeIfAbsent(block,
                b -> new BlockStateContainer(b, PROP_FACING));
    }

    private static BlockStateContainer
            getOrCreateSideStateNoYAxis(ExtendedBlock block) {
        return SIDE_STATE_NO_Y_AXIS.computeIfAbsent(block,
                b -> new BlockStateContainer(b, PROP_FACING_HORIZ));
    }

    public static BlockStateContainer
            getNoFacingBlockState(ExtendedBlock block) {
        return getOrCreateNoFacingState(block);
    }

    public static IBlockState getNoFacingBaseState(ExtendedBlock block) {
        return getOrCreateNoFacingState(block).getBaseState();
    }

    public static BlockStateContainer getSideBlockState(ExtendedBlock block) {
        return getOrCreateSideState(block);
    }

    public static IBlockState getSideBaseState(ExtendedBlock block) {
        return getOrCreateSideState(block).getBaseState();
    }

    public static IBlockState createStateForSideByEntityRotation(
            ExtendedBlock block, BlockPos pos, EntityLivingBase entity) {
        return getOrCreateSideState(block).getBaseState()
                .withProperty(PROP_FACING, getFacing(pos, entity));
    }

    public static BlockStateContainer
            getSideBlockNoYAxisState(ExtendedBlock block) {
        return getOrCreateSideStateNoYAxis(block);
    }

    public static IBlockState getSideBaseNoYAxisState(ExtendedBlock block) {
        return getOrCreateSideStateNoYAxis(block).getBaseState();
    }

    public static IBlockState createStateForSideByEntityRotationNoYAxis(
            ExtendedBlock block, BlockPos pos, EntityLivingBase entity) {
        return getOrCreateSideStateNoYAxis(block).getBaseState()
                .withProperty(PROP_FACING_HORIZ, getHorizontalFacing(entity));
    }

    public static EnumFacing getFacing(BlockPos pos, EntityLivingBase entity) {
        return BlockPistonBase.getFacingFromEntity(pos, entity);
    }

    public static EnumFacing getHorizontalFacing(EntityLivingBase entity) {
        return entity.getHorizontalFacing().getOpposite();
    }

    @SuppressWarnings("unused")
    private static int getSideByEntityRotation(BlockPos pos,
            EntityLivingBase entity) {
        if (MathHelper.abs((float) entity.posX - (float) pos.getX()) < 2.0F
                && MathHelper
                        .abs((float) entity.posZ - (float) pos.getZ()) < 2.0F) {
            double d0 = entity.posY + 1.82D - entity.getYOffset();

            if (d0 - (double) pos.getY() > 2.0D) {
                return SideConstants.TOP;
            }

            if ((double) pos.getY() - d0 > 0.0D) {
                return SideConstants.BOTTOM;
            }
        }
        return getSideByEntityRotationNoYAxis(entity);
    }

    /**
     * Gets the side meta, but only for left/right/front/back, not up/down
     */
    private static int getSideByEntityRotationNoYAxis(EntityLivingBase entity) {
        int l = MathHelper.floor_double(
                (double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        switch (l) {
            // Not needed, default case
            // case 0: return NORTH;
            case 1:
                return SideConstants.EAST;
            case 2:
                return SideConstants.SOUTH;
            case 3:
                return SideConstants.WEST;
            default:
                return SideConstants.NORTH;
        }
    }

    @Deprecated
    public static Point[] pointsForItemsAroundArc(int objSide, int arcDegrees,
            int circRadius, int count) {
        double baseRad = Math.PI / (arcDegrees / 2);
        int halfOS = objSide / 2;
        double baseArcDegAdd = arcDegrees / count / 2;
        Point[] out = new Point[count];
        for (int i = 0; i < count; i++) {
            double angMult = baseArcDegAdd * i;
            int x = (int) (Math.sin(angMult * baseRad) * circRadius - halfOS);
            int y = (int) (Math.cos(angMult * baseRad) * circRadius - halfOS);
            out[i] = new Point(x, y);
        }
        return out;
    }

    public static void reverse(Object[] o) {
        Object[] copy = o.clone();
        for (int i = copy.length - 1; i >= 0; i--) {
            o[i] = copy[copy.length - i - 1];
        }
    }

    public static void throwing(Throwable t) {
        PerelTrains.getLogger().throwing(t);
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            throw new RuntimeException(t);
        }
    }

    public static boolean isClient(World w) {
        return w == null
                ? FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT
                : w.isRemote;
    }

    public static void placeTileEntityCopy(TileEntity original, Block block,
            World w, BlockPos pos) {
        TileEntity copy = null;
        copy = block.createTileEntity(w, w.getBlockState(pos));
        if (original.getClass() != copy.getClass()) {
            throw new IllegalArgumentException(
                    "Transfers only valid between same class "
                            + String.format("(%s != %s)", original.getClass(),
                                    copy.getClass()));
        }
        NBTTagCompound copyTag = new NBTTagCompound();
        original.writeToNBT(copyTag);
        copyTag.setInteger("x", pos.getX());
        copyTag.setInteger("y", pos.getY());
        copyTag.setInteger("z", pos.getZ());
        copy.readFromNBT(copyTag);
        w.setBlockState(pos, w.getBlockState(pos));
        w.setTileEntity(pos, copy);
    }

    private GeneralUtility() {
    }
}
