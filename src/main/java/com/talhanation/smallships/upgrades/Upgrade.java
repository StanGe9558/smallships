package com.talhanation.smallships.upgrades;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.talhanation.smallships.entities.UpgradeAbleEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public abstract class Upgrade extends CapabilityProvider<Upgrade> implements INBTSerializable<CompoundNBT> {
    private final UpgradeType type;

    protected final UpgradeAbleEntity planeEntity;

    public boolean updateClient = false;

    public boolean removed = false;

    public UpgradeAbleEntity getPlaneEntity() {
        return this.planeEntity;
    }

    public Upgrade(UpgradeType type, UpgradeAbleEntity planeEntity) {
        super(Upgrade.class);
        this.type = type;
        this.planeEntity = planeEntity;
    }

    protected void updateClient() {
        this.updateClient = true;
    }

    public void remove() {
        this.removed = true;
        invalidateCaps();
    }

    public final UpgradeType getType() {
        return this.type;
    }

    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {}

    public void tick() {}

    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    public void deserializeNBT(CompoundNBT nbt) {}

    public void onApply(ItemStack itemStack, PlayerEntity playerEntity) {}

    public abstract void render(MatrixStack paramMatrixStack, IRenderTypeBuffer paramIRenderTypeBuffer, int paramInt, float paramFloat);

    public abstract void writePacket(PacketBuffer paramPacketBuffer);

    public abstract void readPacket(PacketBuffer paramPacketBuffer);

    public abstract void dropItems();
}
