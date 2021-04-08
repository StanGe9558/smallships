package com.talhanation.smallships.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;

public abstract class EntityVehicleBase extends Entity {

    private int steps;
    private double clientX;
    private double clientY;
    private double clientZ;
    private double clientYaw;
    private double clientPitch;
    protected float deltaRotation;
    protected AxisAlignedBB boundingBox;


    public EntityVehicleBase(EntityType type, World worldIn) {
        super(type, worldIn);
        this.preventEntitySpawning = true;
        this.stepHeight = 0.6F;
        recalculateBoundingBox();
    }

    public void tick() {
        if (!this.world.isRemote) {
            this.prevPosX = getPosX();
            this.prevPosY = getPosY();
            this.prevPosZ = getPosZ();
        }
        func_233577_ch_();
        super.tick();
        tickLerp();
        recalculateBoundingBox();
    }

    public void recalculateBoundingBox() {
        double width = getWidth();
        double height = getHeight();
        this.boundingBox = new AxisAlignedBB(getPosX() - width / 2.0D, getPosY(), getPosZ() - width / 2.0D, getPosX() + width / 2.0D, getPosY() + height, getPosZ() + width / 2.0D);
    }
    public double getVehicleWidth() {
        return 1.3D;
    }

    public double getVehicleHeight() {
        return 1.6D;
    }

    public PlayerEntity getDriver() {
        List<Entity> passengers = getPassengers();
        if (passengers.size() <= 0)
            return null;
        if (passengers.get(0) instanceof PlayerEntity)
            return (PlayerEntity)passengers.get(0);
        return null;
    }

    public abstract int getPassengerSize();

    protected boolean canFitPassenger(Entity passenger) {
        return (getPassengers().size() < getPassengerSize());
    }

    protected void applyYawToEntity(Entity entityToUpdate) {
        entityToUpdate.setRenderYawOffset(this.rotationYaw);
        float f = MathHelper.wrapDegrees(entityToUpdate.rotationYaw - this.rotationYaw);
        float f1 = MathHelper.clamp(f, -130.0F, 130.0F);
        entityToUpdate.prevRotationYaw += f1 - f;
        entityToUpdate.rotationYaw += f1 - f;
        entityToUpdate.setRotationYawHead(entityToUpdate.rotationYaw);
    }

    @OnlyIn(Dist.CLIENT)
    public void applyOrientationToEntity(Entity entityToUpdate) {
        applyYawToEntity(entityToUpdate);
    }

    public abstract Vector3d[] getPlayerOffsets();

    public void updatePassenger(Entity passenger) {
        if (!isPassenger(passenger))
            return;
        double front = 0.0D;
        double side = 0.0D;
        double height = 0.0D;
        List<Entity> passengers = getPassengers();
        if (passengers.size() > 0) {
            int i = passengers.indexOf(passenger);
            Vector3d offset = getPlayerOffsets()[i];
            front = offset.x;
            side = offset.z;
            height = offset.y;
        }
        Vector3d vec3d = (new Vector3d(front, height, side)).rotateYaw(-this.rotationYaw * 0.017453292F - 1.5707964F);
        passenger.setPosition(getPosX() + vec3d.x, getPosY() + vec3d.y, getPosZ() + vec3d.z);
        passenger.rotationYaw += this.deltaRotation;
        passenger.setRotationYawHead(passenger.getRotationYawHead() + this.deltaRotation);
        applyYawToEntity(passenger);
    }

    public Entity getControllingPassenger() {
        return (Entity)getDriver();
    }


    public boolean func_241845_aY() {
        return true;
    }

    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public boolean canRenderOnFire() {
        return false;
    }

    public boolean canBePushed() {
        return true;
    }

    public boolean canBeCollidedWith() {
        return isAlive();
    }

    private void tickLerp() {
        if (canPassengerSteer()) {
            this.steps = 0;
            setPacketCoordinates(getPosX(), getPosY(), getPosZ());
        }
        if (this.steps > 0) {
            double d0 = getPosX() + (this.clientX - getPosX()) / this.steps;
            double d1 = getPosY() + (this.clientY - getPosY()) / this.steps;
            double d2 = getPosZ() + (this.clientZ - getPosZ()) / this.steps;
            double d3 = MathHelper.wrapDegrees(this.clientYaw - this.rotationYaw);
            this.rotationYaw = (float)(this.rotationYaw + d3 / this.steps);
            this.rotationPitch = (float)(this.rotationPitch + (this.clientPitch - this.rotationPitch) / this.steps);
            this.steps--;
            setPosition(d0, d1, d2);
            setRotation(this.rotationYaw, this.rotationPitch);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.clientX = x;
        this.clientY = y;
        this.clientZ = z;
        this.clientYaw = yaw;
        this.clientPitch = pitch;
        this.steps = 10;
    }

    public static double calculateMotionX(float speed, float rotationYaw) {
        return (MathHelper.sin(-rotationYaw * 0.017453292F) * speed);
    }

    public static double calculateMotionZ(float speed, float rotationYaw) {
        return (MathHelper.cos(rotationYaw * 0.017453292F) * speed);
    }

    public ActionResultType processInitialInteract(PlayerEntity player, Hand hand) {
        if (!player.isSneaking()) {
            if (player.getRidingEntity() != this &&
                    !this.world.isRemote)
                player.startRiding(this);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    public abstract boolean doesEnterThirdPerson();

    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
