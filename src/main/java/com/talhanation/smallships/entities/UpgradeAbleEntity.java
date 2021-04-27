package com.talhanation.smallships.entities;

import com.talhanation.smallships.upgrades.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class UpgradeAbleEntity extends TNBoatEntity{
    public HashMap<ResourceLocation, Upgrade> upgrades = new HashMap<>();
    private final int networkUpdateInterval;

    public UpgradeAbleEntity(EntityType<? extends TNBoatEntity> type, World world) {
        super(type, world);
        this.networkUpdateInterval = type.getUpdateFrequency();
    }

    public void tick(){
        super.tick();

        if (this.world.isRemote && !canPassengerSteer()) {
            tickUpgrades();
            return;
        }
    }

    public void tickUpgrades(){
        List<ResourceLocation> upgradesToRemove = new ArrayList<>();
        List<ResourceLocation> upgradesToUpdate = new ArrayList<>();
        this.upgrades.forEach((rl, upgrade) -> {
            upgrade.tick();
            if (upgrade.removed) {
                upgradesToRemove.add(rl);
            } else if (upgrade.updateClient && !this.world.isRemote) {
                upgradesToUpdate.add(rl);
            }
        });
        for (ResourceLocation name : upgradesToRemove)
            this.upgrades.remove(name);
        if (this.ticksExisted % this.networkUpdateInterval == 0)
            for (ResourceLocation name : upgradesToUpdate)
                UpgradeNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new UpdateUpgradePacket(name, getEntityId(), (ServerWorld)this.world));
    }


    public boolean tryToAddUpgrade(PlayerEntity playerEntity, ItemStack itemStack) {
        Optional<UpgradeType> upgradeTypeOptional = Upgrades.getUpgradeFromItem(itemStack.getItem());
        return ((Boolean)upgradeTypeOptional.<Boolean>map(upgradeType -> {
            if (canAddUpgrade(upgradeType)) {
                Upgrade upgrade = upgradeType.instanceSupplier.apply(this);
                addUpgrade(playerEntity, itemStack, upgrade);
                return Boolean.valueOf(true);
            }
            return Boolean.valueOf(false);
        }).orElse(Boolean.valueOf(false))).booleanValue();
    }


    protected void addUpgrade(PlayerEntity playerEntity, ItemStack itemStack, Upgrade upgrade) {
        upgrade.onApply(itemStack, playerEntity);
        if (!playerEntity.isCreative())
            itemStack.shrink(1);
        UpgradeType upgradeType = upgrade.getType();
        this.upgrades.put(upgradeType.getRegistryName(), upgrade);
        if (!this.world.isRemote)
            UpgradeNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new UpdateUpgradePacket(upgrade.getType().getRegistryName(), getEntityId(), (ServerWorld)this.world, true));
    }

    public void writeUpdateUpgradePacket(ResourceLocation upgradeID, PacketBuffer buffer) {
        buffer.writeVarInt(getEntityId());
        buffer.writeResourceLocation(upgradeID);
        ((Upgrade)this.upgrades.get(upgradeID)).writePacket(buffer);
    }

    public void readUpdateUpgradePacket(ResourceLocation upgradeID, PacketBuffer buffer, boolean newUpgrade) {
        if (newUpgrade) {
            UpgradeType upgradeType = (UpgradeType)UpgradesRegistries.UPGRADE_TYPES.getValue(upgradeID);
            Upgrade upgrade = upgradeType.instanceSupplier.apply(this);
            this.upgrades.put(upgradeID, upgrade);
        }
        ((Upgrade)this.upgrades.get(upgradeID)).readPacket(buffer);
    }

    public void removeUpgrade(ResourceLocation upgradeID) {
        Upgrade upgrade = this.upgrades.remove(upgradeID);
        if (upgrade != null) {
            upgrade.dropItems();
            upgrade.remove();
            if (!this.world.isRemote)
                UpgradeNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new SUpgradeRemovedPacket(upgradeID, getEntityId()));
        }
    }

    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains("upgrades")) {
            CompoundNBT upgradesNBT = compound.getCompound("upgrades");
            deserializeUpgrades(upgradesNBT);
        }
    }

    private void deserializeUpgrades(CompoundNBT upgradesNBT) {
        for (String key : upgradesNBT.keySet()) {
            ResourceLocation resourceLocation = new ResourceLocation(key);
            UpgradeType upgradeType = (UpgradeType)UpgradesRegistries.UPGRADE_TYPES.getValue(resourceLocation);
            if (upgradeType != null) {
                Upgrade upgrade = upgradeType.instanceSupplier.apply(this);
                upgrade.deserializeNBT(upgradesNBT.getCompound(key));
                this.upgrades.put(resourceLocation, upgrade);
            }
        }
    }

    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
         compound.put("upgrades", (INBT)getUpgradesNBT());
    }

    private CompoundNBT getUpgradesNBT() {
        CompoundNBT upgradesNBT = new CompoundNBT();
        for (Upgrade upgrade : this.upgrades.values())
            upgradesNBT.put(upgrade.getType().getRegistryName().toString(), (INBT)upgrade.serializeNBT());
        return upgradesNBT;
    }

    public boolean canAddUpgrade(UpgradeType upgradeType) {
        return !this.upgrades.containsKey(upgradeType.getRegistryName());
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (this.upgrades != null)
            for (Upgrade upgrade : this.upgrades.values()) {
                LazyOptional<T> lazyOptional = upgrade.getCapability(cap, side);
                if (lazyOptional.isPresent())
                    return lazyOptional;
            }
        return super.getCapability(cap, side);
    }

    public void writeSpawnData(PacketBuffer buffer) {
        Collection<Upgrade> upgrades = this.upgrades.values();
        buffer.writeVarInt(upgrades.size());
        for (Upgrade upgrade : upgrades) {
            ResourceLocation upgradeID = upgrade.getType().getRegistryName();
            buffer.writeResourceLocation(upgradeID);
            upgrade.writePacket(buffer);
        }
    }

    public void readSpawnData(PacketBuffer additionalData) {
        int upgradesSize = additionalData.readVarInt();
        for (int i = 0; i < upgradesSize; i++) {
            ResourceLocation upgradeID = additionalData.readResourceLocation();
            UpgradeType upgradeType = (UpgradeType)UpgradesRegistries.UPGRADE_TYPES.getValue(upgradeID);
            Upgrade upgrade = upgradeType.instanceSupplier.apply(this);
            this.upgrades.put(upgradeID, upgrade);
            upgrade.readPacket(additionalData);
        }
    }

}
