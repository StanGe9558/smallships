package com.talhanation.smallships.upgrades;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.talhanation.smallships.entities.UpgradeAbleEntity;
import com.talhanation.smallships.util.MathUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;

public class BannerUpgrade extends Upgrade {
    public ItemStack banner;
    public float rotation;
    public float prevRotation;

    public BannerUpgrade(UpgradeAbleEntity planeEntity) {
        super((UpgradeType)Upgrades.BANNER.get(), planeEntity);
        this.banner = Items.WHITE_BANNER.getDefaultInstance();
        this.prevRotation = planeEntity.prevRotationYaw;
        this.rotation = planeEntity.prevRotationYaw;
    }

    public void tick() {
        this.prevRotation = this.rotation;
        this.rotation = MathUtil.lerpAngle(0.05F, this.rotation, this.planeEntity.prevRotationYaw);
    }

    public CompoundNBT serializeNBT() {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.put("banner", (INBT)this.banner.serializeNBT());
        return compoundNBT;
    }

    public void deserializeNBT(CompoundNBT nbt) {
        INBT banner = nbt.get("banner");
        if (banner instanceof CompoundNBT)
            this.banner = ItemStack.read((CompoundNBT)banner);
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, float partialTicks) {
        BannerModel.renderBanner(this, partialTicks, matrixStack, buffer, this.banner, packedLight, BannerTileEntityRenderer.getModelRender());
    }

    public void onApply(ItemStack itemStack, PlayerEntity playerEntity) {
        if (itemStack.getItem() instanceof net.minecraft.item.BannerItem) {
            this.banner = itemStack.copy();
            this.banner.setCount(1);
            updateClient();
        }
    }

    public void writePacket(PacketBuffer buffer) {
        buffer.writeItemStack(this.banner);
    }

    public void readPacket(PacketBuffer buffer) {
        this.banner = buffer.readItemStack();
    }

    public void dropItems() {
        this.planeEntity.entityDropItem(this.banner);
    }
}