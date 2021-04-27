package com.talhanation.smallships.upgrades;

import com.talhanation.smallships.entities.UpgradeAbleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateUpgradePacket {
    private final boolean newUpgrade;

    private final ResourceLocation upgradeID;

    private final int planeEntityID;

    private ServerWorld serverWorld;

    private PacketBuffer packetBuffer;

    public UpdateUpgradePacket(ResourceLocation upgradeID, int planeEntityID, ServerWorld serverWorld) {
        this(upgradeID, planeEntityID, serverWorld, false);
    }

    public UpdateUpgradePacket(ResourceLocation upgradeID, int planeEntityID, ServerWorld serverWorld, boolean newUpgrade) {
        this.upgradeID = upgradeID;
        this.planeEntityID = planeEntityID;
        this.serverWorld = serverWorld;
        this.newUpgrade = newUpgrade;
    }

    public UpdateUpgradePacket(PacketBuffer buffer) {
        this.newUpgrade = buffer.readBoolean();
        this.planeEntityID = buffer.readVarInt();
        this.upgradeID = buffer.readResourceLocation();
        this.packetBuffer = buffer;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeBoolean(this.newUpgrade);
        UpgradeAbleEntity planeEntity = (UpgradeAbleEntity)this.serverWorld.getEntityByID(this.planeEntityID);
        planeEntity.writeUpdateUpgradePacket(this.upgradeID, buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ClientWorld clientWorld = (Minecraft.getInstance()).world;
            ((UpgradeAbleEntity)clientWorld.getEntityByID(this.planeEntityID)).readUpdateUpgradePacket(this.upgradeID, this.packetBuffer, this.newUpgrade);
        });
        ctx.setPacketHandled(true);
    }
}