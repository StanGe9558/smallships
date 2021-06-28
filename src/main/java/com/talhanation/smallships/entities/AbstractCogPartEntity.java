package com.talhanation.smallships.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;

public class AbstractCogPartEntity extends net.minecraftforge.entity.PartEntity<AbstractCogEntity> {
    public final AbstractCogEntity parent;
    public final String name;
    private final EntitySize size;

    public AbstractCogPartEntity(AbstractCogEntity abstractCog, String name, float x, float y) {
        super(abstractCog);
        this.size = EntitySize.scalable(x, y);
        this.refreshDimensions();
        this.parent = abstractCog;
        this.name = name;
    }

    protected void defineSynchedData() {
    }

    protected void readAdditionalSaveData(CompoundNBT cmp) {
    }

    protected void addAdditionalSaveData(CompoundNBT cmp) {
    }

    public boolean isPickable() {
        return true;
    }

    public boolean hurt(DamageSource dmg, float amt) {
        return this.isInvulnerableTo(dmg) ? false : this.parent.hurt(dmg, amt);
    }

    public boolean is(Entity entity) {
        return this == entity || this.parent == entity;
    }

    public IPacket<?> getAddEntityPacket() {
        throw new UnsupportedOperationException();
    }

    public EntitySize getDimensions(Pose pose) {
        return this.size;
    }
}

