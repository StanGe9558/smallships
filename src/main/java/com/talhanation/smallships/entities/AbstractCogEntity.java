package com.talhanation.smallships.entities;

import com.talhanation.smallships.config.SmallShipsConfig;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractCogEntity extends AbstractSailBoat{
    public float momentum;
    public float outOfControlTicks;
    public float deltaRotation;
    public boolean removed;
    private double waterLevel;
    public boolean leftInputDown;
    public boolean rightInputDown;
    public boolean forwardInputDown;
    public boolean backInputDown;
    private float boatGlide;
    private Status status;
    private Status previousStatus;
    public int passengerwaittime;
    public float passengerfaktor;
    public int posPointer = -1;
    public final double[][] positions = new double[64][3];

    public final AbstractCogPartEntity mast;
    public final AbstractCogPartEntity sail;
    public final AbstractCogPartEntity body_1;
    public final AbstractCogPartEntity body_2;
    public final AbstractCogPartEntity body_3;
    public final AbstractCogPartEntity[] subEntities;

    public AbstractCogEntity(EntityType<? extends AbstractCogEntity> entityType, World worldIn) {
        super(entityType, worldIn);
        this.mast = new AbstractCogPartEntity(this, "mast", 0.5F, 5.0F);
        this.sail = new AbstractCogPartEntity(this, "sail", 2.0F, 2.0F);
        this.body_1 = new AbstractCogPartEntity(this, "body_1", 2.0F, 1.5F);
        this.body_2 = new AbstractCogPartEntity(this, "body_2", 2.0F, 1.5F);
        this.body_3 = new AbstractCogPartEntity(this, "body_3", 2.0F, 1.5F);
        this.subEntities = new AbstractCogPartEntity[]{this.mast, this.sail, this.body_1, this.body_2, this.body_3};//??
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    private float rotWrap(double x) {
        return (float)MathHelper.wrapDegrees(x);
    }

    @Override
    public net.minecraftforge.entity.PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    public AbstractCogPartEntity[] getSubEntities() {
        return this.subEntities;
    }

    private void tickPart(AbstractCogPartEntity part, double x, double y, double z) {
        part.setPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }
    ///////////////////////////////////////////////////
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    public double[] getLatencyPos(int p_70974_1_, float p_70974_2_) {
        p_70974_2_ = 1.0F - p_70974_2_;
        int i = this.posPointer - p_70974_1_ & 63;
        int j = this.posPointer - p_70974_1_ - 1 & 63;
        double[] adouble = new double[3];
        double d0 = this.positions[i][0];
        double d1 = MathHelper.wrapDegrees(this.positions[j][0] - d0);
        adouble[0] = d0 + d1 * (double)p_70974_2_;
        d0 = this.positions[i][1];
        d1 = this.positions[j][1] - d0;
        adouble[1] = d0 + d1 * (double)p_70974_2_;
        adouble[2] = MathHelper.lerp((double)p_70974_2_, this.positions[i][2], this.positions[j][2]);
        return adouble;
    }

    @Override
    public void tick() {

        Vector3d[] avector3d = new Vector3d[this.subEntities.length];

        for(int j = 0; j < this.subEntities.length; ++j) {
            avector3d[j] = new Vector3d(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
        }
        float f15 = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * ((float)Math.PI / 180F);
        float f16 = MathHelper.cos(f15);
        float f2 = MathHelper.sin(f15);
        float f17 = this.deltaRotation * ((float)Math.PI / 180F);
        float f3 = MathHelper.sin(f17);
        float f18 = MathHelper.cos(f17);
        this.tickPart(this.sail, (double)(f3 * 0.0F), 3.0D, (double)(-f18 * 0.0F));
        this.tickPart(this.mast, (double)(f3 * 0.0F), 0.0D, (double)(-f18 * 0.0F));
        if (!this.level.isClientSide) {
            for (int b = 0; b < 5; ++b) {
                AxisAlignedBB axisalignedbb = null;
                if (b == 0) {
                    axisalignedbb = this.body_1.getBoundingBox();
                }

                if (b == 1) {
                    axisalignedbb = this.body_2.getBoundingBox();
                }

                if (b == 2) {
                    axisalignedbb = this.body_3.getBoundingBox();
                }
                if (b == 3) {
                    axisalignedbb = this.mast.getBoundingBox();
                }

                if (b == 4) {
                    axisalignedbb = this.sail.getBoundingBox();
                }
                this.knockBack(this.level.getEntities(this, axisalignedbb.inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntityPredicates.NO_CREATIVE_OR_SPECTATOR));

            }
        }
        for(int l = 0; l < this.subEntities.length; ++l) {
            this.subEntities[l].xo = avector3d[l].x;
            this.subEntities[l].yo = avector3d[l].y;
            this.subEntities[l].zo = avector3d[l].z;
            this.subEntities[l].xOld = avector3d[l].x;
            this.subEntities[l].yOld = avector3d[l].y;
            this.subEntities[l].zOld = avector3d[l].z;
        }

        for(int k = 0; k < 3; ++k) {
            AbstractCogPartEntity abstractCogPartEntity = null;
            if (k == 0) {
                abstractCogPartEntity = this.body_1;
            }

            if (k == 1) {
                abstractCogPartEntity = this.body_2;
            }

            if (k == 2) {
                abstractCogPartEntity = this.body_3;
            }

            double[] adouble = this.getLatencyPos(0, 0.0F);
            double[] adouble1 = this.getLatencyPos(0 + k * 2, 0.0F);
            float f7 = this.yRot * ((float)Math.PI / 180F) + this.rotWrap(adouble1[0] - adouble[0]) * ((float)Math.PI / 180F);
            float f20 = MathHelper.sin(f7);
            float f21 = MathHelper.cos(f7);
            float f23 = (float)(k - 1) * 2.0F;//pos
            this.tickPart(abstractCogPartEntity, (double)(-(f3 * 1 + f20 * f23) * f16), adouble1[1] - adouble[1] - (double)((f23 + 0.0F) * f2) + 0.0D, (double)((f18 * 0.0F + f21 * f23) * f16));
        }

        passengerwaittime--;
        this.previousStatus = this.status;
        this.status = this.getBoatStatus();
        if (this.status != TNBoatEntity.Status.UNDER_WATER && this.status != TNBoatEntity.Status.UNDER_FLOWING_WATER) {
            this.outOfControlTicks = 0.0F;
        } else {
            ++this.outOfControlTicks;
        }

        if (!this.level.isClientSide && this.outOfControlTicks >= 60.0F) {
            this.ejectPassengers();
        }

        if (this.getTimeSinceHit() > 0) {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }

        super.tick();
        this.tickLerp();
        if (this.isControlledByLocalInstance()) {
            this.updateMotion();
            if (this.level.isClientSide) {
                this.controlBoat();
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            this.setDeltaMovement(Vector3d.ZERO);
        }

        if (getSailState() && this.getBoatStatus().equals(Status.IN_WATER) && this.getControllingPassenger() instanceof PlayerEntity && SmallShipsConfig.PlaySwimmSound.get() ) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_SWIM, this.getSoundSource(), 0.05F, 0.8F + 0.4F * this.random.nextFloat());

        }

        this.checkInsideBlocks();
        List<Entity> list = this.level.getEntities(this, this.body_1.getBoundingBox().inflate((double) 0.2F, (double) -0.01F, (double) 0.2F), EntityPredicates.pushableBy(this));
        if (!list.isEmpty()) {
            boolean flag = !this.level.isClientSide && !(this.getControllingPassenger() instanceof PlayerEntity);

            for (int j = 0; j < list.size(); ++j) {
                Entity entity = list.get(j);
                if (!entity.hasPassenger(this)) {
                    if (flag && this.getPassengers().size() < 5 && !entity.isPassenger() && entity.getBbWidth() < this.getBbWidth() && entity instanceof LivingEntity && !(entity instanceof WaterMobEntity) && !(entity instanceof PlayerEntity)) {
                        if (passengerwaittime < 0) {
                            entity.startRiding(this);
                        }
                    } else {
                        this.push(entity);
                    }
                }
            }
        }
    }

    @Override
    public void tickLerp() {
    }

    @Override
    public void Watersplash(){
        super.Watersplash();
        Vector3d vector3d = this.getViewVector(0.0F);
        float f0 = MathHelper.cos(this.yRot * ((float)Math.PI / 180F)) * 0.8F;
        float f1 = MathHelper.sin(this.yRot * ((float)Math.PI / 180F)) * 0.8F;
        float f2 =  2.5F - this.random.nextFloat() * 0.7F;
        for (int i = 0; i < 2; ++i) {
            this.level.addParticle(ParticleTypes.DOLPHIN, this.getX() - vector3d.x * (double) f2 + (double) f0, this.getY() - vector3d.y + 0.5D, this.getZ() - vector3d.z * (double) f2 + (double) f1, 0.0D, 0.0D, 0.0D);
            this.level.addParticle(ParticleTypes.DOLPHIN, this.getX() - vector3d.x * (double) f2 - (double) f0, this.getY() - vector3d.y + 0.5D, this.getZ() - vector3d.z * (double) f2 - (double) f1, 0.0D, 0.0D, 0.0D);
            this.level.addParticle(ParticleTypes.DOLPHIN, this.getX() - vector3d.x * (double) f2 + (double) f0, this.getY() - vector3d.y + 0.5D, this.getZ() - vector3d.z * (double) f2 + (double) f1 * 1.1, 0.0D, 0.0D, 0.0D);
            this.level.addParticle(ParticleTypes.DOLPHIN, this.getX() - vector3d.x * (double) f2 - (double) f0, this.getY() - vector3d.y + 0.5D, this.getZ() - vector3d.z * (double) f2 - (double) f1 * 1.1, 0.0D, 0.0D, 0.0D);

            this.level.addParticle(ParticleTypes.SPLASH, this.getX() - vector3d.x * (double) f2 + (double) f0, this.getY() - vector3d.y + 0.8D, this.getZ() - vector3d.z * (double) f2 + (double) f1, 0.0D, 0.0D, 0.0D);
            this.level.addParticle(ParticleTypes.SPLASH, this.getX() - vector3d.x * (double) f2 - (double) f0, this.getY() - vector3d.y + 0.8D, this.getZ() - vector3d.z * (double) f2 - (double) f1, 0.0D, 0.0D, 0.0D);
            this.level.addParticle(ParticleTypes.SPLASH, this.getX() - vector3d.x * (double) f2 + (double) f0, this.getY() - vector3d.y + 0.8D, this.getZ() - vector3d.z * (double) f2 + (double) f1 * 1.1, 0.0D, 0.0D, 0.0D);
            this.level.addParticle(ParticleTypes.SPLASH, this.getX() - vector3d.x * (double) f2 - (double) f0, this.getY() - vector3d.y + 0.8D, this.getZ() - vector3d.z * (double) f2 - (double) f1 * 1.1, 0.0D, 0.0D, 0.0D);

        }
    }

    public Status getBoatStatus() {
        Status boatentity$status = getUnderwaterStatus();
        if (boatentity$status != null) {
            this.waterLevel = (this.body_1.getBoundingBox()).maxY;
            return boatentity$status;
        }
        if (checkInWater())
            return Status.IN_WATER;
        float f = getBoatGlide();
        if (f > 0.0F) {
            this.boatGlide = 0F;
            return Status.ON_LAND;
        }
        return Status.IN_AIR;
    }

    public void updateMotion() {
        double d0 = -0.04D; // down/up moment
        double d1 = isNoGravity() ? 0.0D : d0;
        double d2 = 0.0D;  //
        double CogTurnFactor = SmallShipsConfig.CogTurnFactor.get();
        this.momentum = 1.0F;

        if (this.getPassengers().size() == 2) this.passengerfaktor = 0.05F;
        if (this.getPassengers().size() == 4) this.passengerfaktor = 0.1F;
        if (this.getPassengers().size() == 6) this.passengerfaktor = 0.2F;
        if (this.getPassengers().size() == 8) this.passengerfaktor = 0.3F;
        if (this.getPassengers().size() > 8) this.passengerfaktor = 0.4F;

        if (this.previousStatus == Status.IN_AIR && this.status != Status.IN_AIR && this.status != Status.ON_LAND) {
            this.waterLevel = (this.body_1.getBoundingBox()).minY + getBbHeight();
            setPos(getX(), (getWaterLevelAbove() - getBbHeight()) + 0.101D, getZ());
            setDeltaMovement(getDeltaMovement().multiply(10.0D, 0.0D, 10.0D));
            this.status = Status.IN_WATER;
        } else {
            if (this.status == Status.IN_WATER) {
                d2 = (this.waterLevel - (this.body_1.getBoundingBox()).minY + 0.1D) / getBbHeight();
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
                if (getControllingPassenger() instanceof PlayerEntity)
                    this.boatGlide /= 1.0F;
            }
            Vector3d vec3d = getDeltaMovement();
            setDeltaMovement(vec3d.x * (this.momentum - this.passengerfaktor), vec3d.y + d1, vec3d.z * (this.momentum - this.passengerfaktor));
            this.deltaRotation *= (this.momentum - this.passengerfaktor) * CogTurnFactor;
            if (d2 > 0.0D) {
                Vector3d vec3d1 = getDeltaMovement();
                setDeltaMovement(vec3d1.x, (vec3d1.y + d2 * 0.06D) * 0.75D, vec3d1.z);
            }
        }
    }

    protected void controlBoat() {
        double CogSpeedFactor = SmallShipsConfig.CogSpeedFactor.get();
        if (this.isVehicle()) {
            float f = 0.0F;
            if (this.leftInputDown) {
                --this.deltaRotation;
            }
            if (this.rightInputDown) {
                ++this.deltaRotation;
            }
            if (this.rightInputDown != this.leftInputDown && !this.forwardInputDown && !this.backInputDown) {
                f += 0.005F;
            }
            this.yRot += this.deltaRotation;

            if (getSailState()) {
                f += (0.04F * CogSpeedFactor);
            }
            if (this.backInputDown) {
                f -= (0.005F * CogSpeedFactor);
            }
            if (this.forwardInputDown) {
                f += (0.005F * CogSpeedFactor);
            }
            this.setDeltaMovement(this.getDeltaMovement().add((double) (MathHelper.sin(-this.yRot * ((float) Math.PI / 180F)) * f), 0.0D, (double) (MathHelper.cos(this.yRot * ((float) Math.PI / 180F)) * f)));
            setSteerState(this.rightInputDown && !this.leftInputDown, this.leftInputDown && !this.rightInputDown);
            setIsForward(this.forwardInputDown);
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity) this);
    }

    @Nullable
    private Status getUnderwaterStatus() {
        AxisAlignedBB axisalignedbb = this.body_1.getBoundingBox();
        double d0 = axisalignedbb.maxY + 0.001D;
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.maxY);
        int l = MathHelper.ceil(d0);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.set(k1, l1, i2);
                    FluidState fluidstate = this.level.getFluidState(blockpos$mutable);
                    if (fluidstate.is(FluidTags.WATER) && d0 < (double) ((float) blockpos$mutable.getY() + fluidstate.getHeight(this.level, blockpos$mutable))) {
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

    private boolean checkInWater() {
        AxisAlignedBB axisalignedbb =this.body_1.getBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.minY);
        int l = MathHelper.ceil(axisalignedbb.minY + 0.001D);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        this.waterLevel = Double.MIN_VALUE;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.set(k1, l1, i2);
                    FluidState fluidstate = this.level.getFluidState(blockpos$mutable);
                    if (fluidstate.is(FluidTags.WATER)) {
                        float f = (float) l1 + fluidstate.getHeight(this.level, blockpos$mutable);
                        this.waterLevel = Math.max((double) f, this.waterLevel);
                        flag |= axisalignedbb.minY < (double) f;
                    }
                }
            }
        }

        return flag;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entityIn) {
        super.push(entityIn);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        double CogHealth = SmallShipsConfig.CogHealth.get();
        if (isInvulnerableTo(source))
            return false;
        if (!this.level.isClientSide && isAlive()) {
            if (source == DamageSource.CACTUS)
                return false;
            if (source instanceof IndirectEntityDamageSource && source.getEntity() != null && hasPassenger(source.getEntity()))
                return false;
            setForwardDirection(-getForwardDirection());
            setTimeSinceHit(3);
            setDamageTaken(getDamageTaken() + amount * 10.0F);
            boolean flag = (source.getEntity() instanceof PlayerEntity && ((PlayerEntity) source.getEntity()).abilities.instabuild);
            if (flag || getDamageTaken() > CogHealth) {
                onDestroyed(source, flag);
                remove();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
    }

    @Override
    public Vector3d getDismountLocationForPassenger(final LivingEntity rider) {
        return super.getDismountLocationForPassenger(rider);
    }

    public PlayerEntity getDriver() {
        return super.getDriver();

    }

    @OnlyIn(Dist.CLIENT)
    public void updateInputs(boolean leftInputDown, boolean rightInputDown, boolean forwardInputDown, boolean backInputDown) {
        this.leftInputDown = leftInputDown;
        this.rightInputDown = rightInputDown;
        this.forwardInputDown = forwardInputDown;
        this.backInputDown = backInputDown;
    }

    @Override
    protected void checkInsideBlocks() {
        for (int b = 0; b < 5; ++b) {
            AxisAlignedBB axisalignedbb = null;
            if (b == 0) {
                axisalignedbb = this.body_1.getBoundingBox();
            }

            if (b == 1) {
                axisalignedbb = this.body_2.getBoundingBox();
            }

            if (b == 2) {
                axisalignedbb = this.body_3.getBoundingBox();
            }
            if (b == 3) {
                axisalignedbb = this.mast.getBoundingBox();
            }

            if (b == 4) {
                axisalignedbb = this.sail.getBoundingBox();
            }
            BlockPos blockpos = new BlockPos(axisalignedbb.minX + 0.001D, axisalignedbb.minY + 0.001D, axisalignedbb.minZ + 0.001D);
            BlockPos blockpos1 = new BlockPos(axisalignedbb.maxX - 0.001D, axisalignedbb.maxY - 0.001D, axisalignedbb.maxZ - 0.001D);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
            if (this.level.hasChunksAt(blockpos, blockpos1)) {
                for (int i = blockpos.getX(); i <= blockpos1.getX(); ++i) {
                    for (int j = blockpos.getY(); j <= blockpos1.getY(); ++j) {
                        for (int k = blockpos.getZ(); k <= blockpos1.getZ(); ++k) {
                            blockpos$mutable.set(i, j, k);
                            BlockState blockstate = this.level.getBlockState(blockpos$mutable);

                            try {
                                blockstate.entityInside(this.level, blockpos$mutable, this);
                                this.onInsideBlock(blockstate);
                            } catch (Throwable throwable) {
                                CrashReport crashreport = CrashReport.forThrowable(throwable, "Colliding entity with block");
                                CrashReportCategory crashreportcategory = crashreport.addCategory("Block being collided with");
                                CrashReportCategory.populateBlockDetails(crashreportcategory, blockpos$mutable, blockstate);
                                throw new ReportedException(crashreport);
                            }
                        }
                    }
                }
            }


        }
    }

}