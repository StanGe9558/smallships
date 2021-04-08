package com.talhanation.smallships.entities;

import com.talhanation.smallships.Main;
import com.talhanation.smallships.network.MessageControlShip;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public abstract class EntityShipBase extends EntityVehicleBase {
    private static final DataParameter<Boolean> FORWARD = EntityDataManager.createKey(EntityShipBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BACKWARD = EntityDataManager.createKey(EntityShipBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEFT = EntityDataManager.createKey(EntityShipBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RIGHT = EntityDataManager.createKey(EntityShipBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> DAMAGE_TAKEN = EntityDataManager.createKey(TNBoatEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> BOAT_TYPE = EntityDataManager.createKey(TNBoatEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> LEFT_PADDLE = EntityDataManager.createKey(TNBoatEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RIGHT_PADDLE = EntityDataManager.createKey(TNBoatEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ROCKING_TICKS = EntityDataManager.createKey(TNBoatEntity.class, DataSerializers.VARINT);

    private final float[] paddlePositions = new float[2];
    private float momentum;
    private float outOfControlTicks;
    private float deltaRotation;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYaw;
    private double lerpPitch;
    private double waterLevel;
    private float boatGlide;
    private Status status;
    private Status previousStatus;
    private double lastYd;
    private boolean rocking;
    private boolean downwards;
    private float rockingIntensity;
    private float rockingAngle;
    private float prevRockingAngle;

    public abstract float getSpeedModifier();
    public abstract float getRotationModifier();
    public abstract float getPassengerFactor();
    public abstract double getPlayerYOffset();

    public EntityShipBase(EntityType type, World worldIn) {
        super(type, worldIn);

    }

    protected void registerData() {
        this.dataManager.register(FORWARD, Boolean.valueOf(false));
        this.dataManager.register(BACKWARD, Boolean.valueOf(false));
        this.dataManager.register(LEFT, Boolean.valueOf(false));
        this.dataManager.register(RIGHT, Boolean.valueOf(false));
    }

    public void readAdditional(CompoundNBT compound) {

    }

    protected void writeAdditional(CompoundNBT compound) {

    }

    public void tick() {
        super.tick();
        updateMotion();
        controlShip();
        checkPush();
        move(MoverType.SELF, getMotion());
        breakLily();

    }

    public void centerShip() {
        Direction facing = getHorizontalFacing();
        switch (facing) {
            case SOUTH:
                this.rotationYaw = 0.0F;
                break;
            case NORTH:
                this.rotationYaw = 180.0F;
                break;
            case EAST:
                this.rotationYaw = -90.0F;
                break;
            case WEST:
                this.rotationYaw = 90.0F;
                break;
        }
    }

    public void checkPush() {
        List<PlayerEntity> list = this.world.getEntitiesWithinAABB(PlayerEntity.class, getBoundingBox().expand(0.2D, 0.0D, 0.2D).expand(-0.2D, 0.0D, -0.2D));
        for (PlayerEntity player : list) {
            if (!player.isPassenger(this) && player.isSneaking()) {
                double motX = calculateMotionX(0.05F, player.rotationYaw);
                double motZ = calculateMotionZ(0.05F, player.rotationYaw);
                move(MoverType.PLAYER, new Vector3d(motX, 0.0D, motZ));
                return;
            }
        }
    }

    public boolean canPlayerDriveCar(PlayerEntity player) {
        if (player.equals(getDriver()))
            return true;
        return false;
    }


    public void updateControls(boolean forward, boolean backward, boolean left, boolean right, PlayerEntity player) {
        boolean needsUpdate = false;
        if (isForward() != forward) {
            setForward(forward);
            needsUpdate = true;
        }
        if (isBackward() != backward) {
            setBackward(backward);
            needsUpdate = true;
        }
        if (isLeft() != left) {
            setLeft(left);
            needsUpdate = true;
        }
        if (isRight() != right) {
            setRight(right);
            needsUpdate = true;
        }
        if (this.world.isRemote && needsUpdate)
            Main.SIMPLE_CHANNEL.sendToServer(new MessageControlShip(forward, backward, left, right, player));
    }

    public void setForward(boolean forward) {
        this.dataManager.set(FORWARD, Boolean.valueOf(forward));
    }

    public boolean isForward() {
        if (getDriver() == null || !canPlayerDriveCar(getDriver()))
            return false;
        return ((Boolean)this.dataManager.get(FORWARD)).booleanValue();
    }

    public void setBackward(boolean backward) {
        this.dataManager.set(BACKWARD, Boolean.valueOf(backward));
    }

    public boolean isBackward() {
        if (getDriver() == null || !canPlayerDriveCar(getDriver()))
            return false;
        return ((Boolean)this.dataManager.get(BACKWARD)).booleanValue();
    }

    public void setLeft(boolean left) {
        this.dataManager.set(LEFT, Boolean.valueOf(left));
    }

    public boolean isLeft() {
        return ((Boolean)this.dataManager.get(LEFT)).booleanValue();
    }

    public void setRight(boolean right) {
        this.dataManager.set(RIGHT, Boolean.valueOf(right));
    }

    public boolean isRight() {
        return ((Boolean)this.dataManager.get(RIGHT)).booleanValue();
    }

    public void updateMotion() {
        double d0 = -0.03D; // down/up moment
        double d1 = hasNoGravity() ? 0.0D : d0;
        double d2 = 0.0D;  //
        double RotationModifier = getRotationModifier();
        double passengerFactor =  getPassengerFactor();
        this.momentum = 1.0F;
        if (this.previousStatus == Status.IN_AIR && this.status != Status.IN_AIR && this.status != Status.ON_LAND) {
            this.waterLevel = (getBoundingBox()).minY + getHeight();
            setPosition(getPosX(), (getWaterLevelAbove() - getHeight()) + 0.101D, getPosZ());
            setMotion(getMotion().mul(10.0D, 0.0D, 10.0D));
            this.status = Status.IN_WATER;
        } else {
            if (this.status == Status.IN_WATER) {
                d2 = (this.waterLevel - (getBoundingBox()).minY + 0.1D) / getHeight();
                this.momentum = 0.9F;
            } else if (this.status == Status.UNDER_FLOWING_WATER) {
                d1 = -7.0E-4D;
                this.momentum = 0.9F;
            } else if (this.status == Status.UNDER_WATER) {
                d2 = 0.009999999776482582D;
                this.momentum = 0.45F;
            } else if (this.status == Status.IN_AIR) {
                this.momentum = 0.9F;
            } else if (this.status == Status.ON_LAND) {
                this.momentum = this.boatGlide * 0.001F;
                if (getControllingPassenger() instanceof net.minecraft.entity.player.PlayerEntity)
                    this.boatGlide /= 1.0F;
            }
            Vector3d vec3d = getMotion();
            setMotion(vec3d.x * (this.momentum - passengerFactor), vec3d.y + d1, vec3d.z * (this.momentum - passengerFactor));
            this.deltaRotation *= (this.momentum - passengerFactor) * RotationModifier;

            if (d2 > 0.0D) {
                Vector3d vec3d1 = getMotion();
                setMotion(vec3d1.x, (vec3d1.y + d2 * 0.06D) * 0.75D, vec3d1.z);
            }
        }
    }

    private void controlShip() {
        float modifier = getSpeedModifier();
        if (!isBeingRidden()) {
            setForward(false);
            setBackward(false);
            setLeft(false);
            setRight(false);
        }
        float speed = 0.0F;
        if (this.isLeft()) {
            --this.deltaRotation;
        }
        if (this.isRight()) {
            ++this.deltaRotation;
        }
        if (this.isRight() != this.isLeft() && !this.isForward() && !this.isBackward()) {
            speed += 0.005F;
        }
        this.rotationYaw += this.deltaRotation;

        if (getSailState()) {
            speed += (0.04 * modifier);
        }
        if (this.isBackward()) {
            speed -= (0.005F * modifier);
        }
        if (this.isForward()) {
            speed += (0.005F * modifier);
        }
        this.setMotion(this.getMotion().add((double) (MathHelper.sin(-this.rotationYaw * ((float) Math.PI / 180F)) * speed), 0.0D, (double) (MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F)) * speed)));
    }

    private void breakLily() {
        AxisAlignedBB boundingBox = getBoundingBox();
        double offset = 0.75D;
        BlockPos start = new BlockPos(boundingBox.minX - offset, boundingBox.minY - offset, boundingBox.minZ - offset);
        BlockPos end = new BlockPos(boundingBox.maxX + offset, boundingBox.maxY + offset, boundingBox.maxZ + offset);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        boolean hasBroken = false;
        if (world.isAreaLoaded(start, end)) {
            for (int i = start.getX(); i <= end.getX(); ++i) {
                for (int j = start.getY(); j <= end.getY(); ++j) {
                    for (int k = start.getZ(); k <= end.getZ(); ++k) {
                        pos.setPos(i, j, k);
                        BlockState blockstate = world.getBlockState(pos);
                        if (blockstate.getBlock() instanceof LilyPadBlock) {
                            world.destroyBlock(pos, true);
                            hasBroken = true;
                        }
                    }
                }
            }
        }
        if (hasBroken) {
            world.playSound(null, getPosX(), getPosY(), getPosZ(), SoundEvents.BLOCK_CROP_BREAK, SoundCategory.BLOCKS, 1F, 0.9F + 0.2F * rand.nextFloat());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public float getRowingTime(int side, float limbSwing) {
        return this.getPaddleState(side) ? (float)MathHelper.clampedLerp((double)this.paddlePositions[side] - (double)((float)Math.PI / 8F), (double)this.paddlePositions[side], (double)limbSwing) : 0.0F;
    }


    private Status getShipStatus() {
        Status shipstatus = this.getUnderwaterStatus();
        if (shipstatus != null) {
            this.waterLevel = this.getBoundingBox().maxY;
            return shipstatus;
        } else if (this.checkInWater()) {
            return Status.IN_WATER;
        } else {
            float f = this.getBoatGlide();
            if (f > 0.0F) {
                this.boatGlide = f;
                return Status.ON_LAND;
            } else {
                return Status.IN_AIR;
            }
        }
    }

    public float getWaterLevelAbove() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.maxY);
        int l = MathHelper.ceil(axisalignedbb.maxY - this.lastYd);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        label39:
        for(int k1 = k; k1 < l; ++k1) {
            float f = 0.0F;

            for(int l1 = i; l1 < j; ++l1) {
                for(int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.setPos(l1, k1, i2);
                    FluidState fluidstate = this.world.getFluidState(blockpos$mutable);
                    if (fluidstate.isTagged(FluidTags.WATER)) {
                        f = Math.max(f, fluidstate.getActualHeight(this.world, blockpos$mutable));
                    }

                    if (f >= 1.0F) {
                        continue label39;
                    }
                }
            }

            if (f < 1.0F) {
                return (float)blockpos$mutable.getY() + f;
            }
        }

        return (float)(l + 1);
    }

    /**
     * Decides how much the boat should be gliding on the land (based on any slippery blocks)
     */
    public float getBoatGlide() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY - 0.001D, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        int i = MathHelper.floor(axisalignedbb1.minX) - 1;
        int j = MathHelper.ceil(axisalignedbb1.maxX) + 1;
        int k = MathHelper.floor(axisalignedbb1.minY) - 1;
        int l = MathHelper.ceil(axisalignedbb1.maxY) + 1;
        int i1 = MathHelper.floor(axisalignedbb1.minZ) - 1;
        int j1 = MathHelper.ceil(axisalignedbb1.maxZ) + 1;
        VoxelShape voxelshape = VoxelShapes.create(axisalignedbb1);
        float f = 0.0F;
        int k1 = 0;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for(int l1 = i; l1 < j; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
                int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
                if (j2 != 2) {
                    for(int k2 = k; k2 < l; ++k2) {
                        if (j2 <= 0 || k2 != k && k2 != l - 1) {
                            blockpos$mutable.setPos(l1, k2, i2);
                            BlockState blockstate = this.world.getBlockState(blockpos$mutable);
                            if (!(blockstate.getBlock() instanceof LilyPadBlock) && VoxelShapes.compare(blockstate.getCollisionShape(this.world, blockpos$mutable).withOffset((double)l1, (double)k2, (double)i2), voxelshape, IBooleanFunction.AND)) {
                                f += blockstate.getSlipperiness(this.world, blockpos$mutable, this);
                                ++k1;
                            }
                        }
                    }
                }
            }
        }

        return f / (float)k1;
    }

    private boolean checkInWater() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.minY);
        int l = MathHelper.ceil(axisalignedbb.minY + 0.001D);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        this.waterLevel = Double.MIN_VALUE;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
                for(int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.setPos(k1, l1, i2);
                    FluidState fluidstate = this.world.getFluidState(blockpos$mutable);
                    if (fluidstate.isTagged(FluidTags.WATER)) {
                        float f = (float)l1 + fluidstate.getActualHeight(this.world, blockpos$mutable);
                        this.waterLevel = Math.max((double)f, this.waterLevel);
                        flag |= axisalignedbb.minY < (double)f;
                    }
                }
            }
        }

        return flag;
    }

    /**
     * Decides whether the boat is currently underwater.
     */
    @Nullable
    private Status getUnderwaterStatus() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        double d0 = axisalignedbb.maxY + 0.001D;
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.maxY);
        int l = MathHelper.ceil(d0);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
                for(int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.setPos(k1, l1, i2);
                    FluidState fluidstate = this.world.getFluidState(blockpos$mutable);
                    if (fluidstate.isTagged(FluidTags.WATER) && d0 < (double)((float)blockpos$mutable.getY() + fluidstate.getActualHeight(this.world, blockpos$mutable))) {
                        if (!fluidstate.isSource()) {
                            return Status.UNDER_FLOWING_WATER;
                        }

                        flag = true;
                    }
                }
            }
        }

        return flag ? Status.UNDER_WATER : null;
    }
    public enum Status {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR
    }

    public enum Type {
        OAK(Blocks.OAK_PLANKS, "oak"),
        SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
        BIRCH(Blocks.BIRCH_PLANKS, "birch"),
        JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
        ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
        DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak");

        private final String name;
        private final Block block;

        Type(Block block, String name) {
            this.name = name;
            this.block = block;
        }

        public String getName() {
            return this.name;
        }

        public Block asPlank() {
            return this.block;
        }

        public String toString() {
            return this.name;
        }

        /**
         * Get a boat type by it's enum ordinal
         */
        public static Type byId(int id) {
            Type[] aboatentity$type = values();
            if (id < 0 || id >= aboatentity$type.length) {
                id = 0;
            }

            return aboatentity$type[id];
        }

        public static Type getTypeFromString(String nameIn) {
            Type[] shiptype = values();

            for(int i = 0; i < shiptype.length; ++i) {
                if (shiptype[i].getName().equals(nameIn)) {
                    return shiptype[i];
                }
            }

            return shiptype[0];
        }
    }

    public Vector3d func_230268_c_(final LivingEntity rider) {
        for (final float angle : rider.getPrimaryHand() == HandSide.RIGHT ? new float[] { 90.0F, -90.0F } : new float[] { -90.0F, 90.0F }) {
            final Vector3d pos = this.dismount(func_233559_a_(this.getWidth(), rider.getWidth(), this.rotationYaw + angle), rider);
            if (pos != null) return pos;
        }
        return this.getPositionVec();
    }

    private Vector3d dismount(final Vector3d dir, LivingEntity rider) {
        final double x = this.getPosX() + dir.x;
        final double y = this.getBoundingBox().minY;
        final double z = this.getPosZ() + dir.z;
        final double limit = this.getBoundingBox().maxY + 0.75D;
        final BlockPos.Mutable blockPos = new BlockPos.Mutable();
        for (final Pose pose : rider.getAvailablePoses()) {
            blockPos.setPos(x, y, z);
            while (blockPos.getY() < limit) {
                final double ground = this.world.func_242403_h(blockPos);
                if (blockPos.getY() + ground > limit) break;
                if (TransportationHelper.func_234630_a_(ground)) {
                    final Vector3d pos = new Vector3d(x, blockPos.getY() + ground, z);
                    if (TransportationHelper.func_234631_a_(this.world, rider, rider.getPoseAABB(pose).offset(pos))) {
                        rider.setPose(pose);
                        return pos;
                    }
                }
                blockPos.move(Direction.UP);
            }
        }
        return null;
    }

}
