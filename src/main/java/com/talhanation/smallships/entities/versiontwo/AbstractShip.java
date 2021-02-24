package com.talhanation.smallships.entities.versiontwo;

import com.talhanation.smallships.entities.versiontwo.AbstractBoat;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class AbstractShip extends AbstractBoat {
    public AbstractShip(EntityType type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    public Vector3d func_230268_c_(LivingEntity passenger) {
        return getPlayerOffsets()[0].add(new Vector3d(getPosX(), getPosY() + 0.1D, getPosZ()));
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {

    }

    @Override
    public boolean onLivingFall(float distance, float damageMultiplier) {
        return false;
    }

    @Override
    protected void registerData() {

    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

    public void damageShip(double damage, boolean horizontal) {

    }

    public abstract float getPlayerScaleFactor();

    @Override
    public int getPassengerSize() {
        return getPlayerOffsets().length;
    }

}
