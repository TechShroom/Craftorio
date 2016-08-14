package com.techshroom.mods.craftorio.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum Rotation implements IStringSerializable {
    _0(0), _90(90), _180(180), _270(270);

    public static Rotation byDegree(int degree) {
        switch (degree % 360) {
            case 0:
                return _0;
            case 90:
                return _90;
            case 180:
                return _180;
            case 270:
                return _270;
            default:
                throw new IllegalArgumentException(degree + " is not a multiple of 90");
        }
    }

    public static Rotation fromNMURotation(net.minecraft.util.Rotation rot) {
        switch (rot) {
            case NONE:
                return _0;
            case CLOCKWISE_90:
                return _90;
            case CLOCKWISE_180:
                return _180;
            case COUNTERCLOCKWISE_90:
                return _270;
            default:
                throw new IllegalArgumentException(rot + " is not a multiple of 90");
        }
    }

    private final int degree;

    Rotation(int degree) {
        this.degree = degree;
    }

    public Rotation add(Rotation rotation) {
        int degree = this.degree + rotation.degree;
        return byDegree(degree);
    }

    public EnumFacing rotate(EnumFacing facing) {
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            return facing;
        } else {
            switch (this) {
                case _90:
                    return facing.rotateY();
                case _180:
                    return facing.getOpposite();
                case _270:
                    return facing.rotateYCCW();
                default:
                    return facing;
            }
        }
    }

    @Override
    public String getName() {
        return name().substring(1);
    }

}