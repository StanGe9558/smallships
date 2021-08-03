package com.talhanation.smallships.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.talhanation.smallships.client.render.RenderBanner;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.UUID;

public abstract class AbstractBannerUser extends AbstractSailBoat {
    private static final DataParameter<Boolean> HAS_BANNER = EntityDataManager.defineId(AbstractSailBoat.class, DataSerializers.BOOLEAN);
    public static final DataParameter<ItemStack> BANNER = EntityDataManager.defineId(AbstractBannerUser.class, DataSerializers.ITEM_STACK);

    private float bannerWaveAngle;
    private float prevBannerWaveAngle;

    public AbstractBannerUser(EntityType<? extends TNBoatEntity> type, World world) {
        super(type, world);

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        //this.entityData.define(HAS_BANNER, false);
        this.entityData.define(BANNER, Items.WHITE_BANNER.getDefaultInstance());

    }

    ////////////////////////////////////TICK////////////////////////////////////

    @Override
    public void tick() {
        super.tick();
        //for banner wave
        this.prevBannerWaveAngle = this.bannerWaveAngle;
        this.bannerWaveAngle = (float) Math.sin(getBannerWaveSpeed() * (float) tickCount) * getBannerWaveFactor();

    }

    ////////////////////////////////////REGISTER////////////////////////////////////

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.put("banner", getBanner().serializeNBT());
        //nbt.putBoolean("hasbanner", getHasBanner());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        final INBT banner = nbt.get("banner");
        if (banner instanceof CompoundNBT) {
            entityData.set(BANNER, ItemStack.of((CompoundNBT) banner));
            //entityData.set(HAS_BANNER, ?????????));
        }

    }

    ////////////////////////////////////GET////////////////////////////////////
/*
    public boolean getHasBanner(){
        return entityData.get(HAS_BANNER);
    }
*/
    public ItemStack getBanner() {
        return entityData.get(BANNER);
    }

    public float getBannerWaveFactor() {
        return level.isRaining() ? 4.5F : 3.0F;
    }

    public float getBannerWaveSpeed() {
        return level.isRaining() ? 0.55F : 0.25F;
    }

    public float getBannerWaveAngle(float partialTicks) {
        return MathHelper.lerp(partialTicks, this.prevBannerWaveAngle, this.bannerWaveAngle);
    }

    ////////////////////////////////////SET////////////////////////////////////

    public void setBanner(ItemStack banner) {
            playBannerSound(true);
            //entityData.set(HAS_BANNER, true);
            entityData.set(BANNER, banner.copy());
            banner.setCount(1);

    }
    ////////////////////////////////////SOUND////////////////////////////////////

    public void playBannerSound(boolean hasbanner) {
        if (hasbanner) {
            this.level.playSound(null, this.getX(), this.getY() + 4 , this.getZ(),SoundEvents.WOOL_HIT, this.getSoundSource(), 15.0F, 0.8F + 0.4F * this.random.nextFloat());
        }
    }

    ////////////////////////////////////ON FUNCTIONS////////////////////////////////////

    public boolean onInteractionWithBanner(ItemStack banner, PlayerEntity playerEntity) {
        if (getBanner() != null) //replace with getHasBanner when ready
            dropBanner();
        setBanner(banner);
        if (!this.level.isClientSide()) {
            if (!playerEntity.isCreative()) banner.shrink(1);
            return true;
        }
        return false;

    }

    ////////////////////////////////////OTHER FUNCTIONS////////////////////////////////////

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer ,int packedLight, float partialTicks) {
        if (getBanner() != null) //replace with getHasBanner when ready
        RenderBanner.renderBanner(this, partialTicks, matrixStack, buffer, getBanner(), packedLight, BannerTileEntityRenderer.makeFlag());
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void dropBanner() {
        if (getBanner() != null) { //replace with getHasBanner when ready
            getBanner().setCount(1);
            this.spawnAtLocation(getBanner());
        }
    }
}
