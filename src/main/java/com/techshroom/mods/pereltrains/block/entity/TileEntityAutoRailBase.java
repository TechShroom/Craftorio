package com.techshroom.mods.pereltrains.block.entity;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.techshroom.mods.pereltrains.segment.Rail;
import com.techshroom.mods.pereltrains.segment.Segment;
import com.techshroom.mods.pereltrains.signal.RailSignal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityAutoRailBase extends TileEntity implements Rail {

    private int segmentId = -1;
    private transient Segment segment;

    public TileEntityAutoRailBase() {
        checkSegment();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.segmentId = compound.getInteger("segmentId");
        checkSegment();
    }

    public void checkSegment() {
        if (this.segmentId == -1) {
            if (this.segment == null) {
                this.segment = Segment.create();
            }
            this.segmentId = this.segment.getId();
        }
        // Change if no segment or if the ID is different
        if (this.segment != null && this.segment.getId() == this.segmentId) {
            return;
        }
        this.segment = Segment.get(this.segmentId);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        checkState(this.segmentId != -1, "checkSegment never called!");
        compound.setInteger("segmentId", this.segmentId);
        return super.writeToNBT(compound);
    }

    @Override
    public Map<EnumFacing, Rail> getNeighborRails() {
        return generateNeighborRails();
    }

    private Map<EnumFacing, Rail> generateNeighborRails() {
        ImmutableMap.Builder<EnumFacing, Rail> map = ImmutableMap.builder();
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            TileEntity te = getWorld().getTileEntity(getPos().offset(facing));
            if (te instanceof Rail) {
                map.put(facing, (Rail) te);
            }
        }
        return map.build();
    }

    @Override
    public Optional<RailSignal> getSignal(EnumFacing dir) {
        return Optional
                .ofNullable(getWorld().getTileEntity(getPos().offset(dir)))
                .filter(RailSignal.class::isInstance)
                .map(RailSignal.class::cast);
    }

    @Override
    public Segment getSegment() {
        checkSegment();
        return this.segment;
    }

}
