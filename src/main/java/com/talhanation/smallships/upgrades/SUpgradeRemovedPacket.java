package com.talhanation.smallships.upgrades;

import com.talhanation.smallships.entities.UpgradeAbleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SUpgradeRemovedPacket {
    private final ResourceLocation upgradeID;

    private final int planeEntityID;

    public SUpgradeRemovedPacket(ResourceLocation upgradeID, int planeEntityID) {
        this.upgradeID = upgradeID;
        this.planeEntityID = planeEntityID;
    }

    public SUpgradeRemovedPacket(PacketBuffer buffer) {
        this.upgradeID = buffer.readResourceLocation();
        this.planeEntityID = buffer.readVarInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeResourceLocation(this.upgradeID);
        buffer.writeVarInt(this.planeEntityID);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ClientWorld clientWorld = (Minecraft.getInstance()).world;
            ((UpgradeAbleEntity)clientWorld.getEntityByID(this.planeEntityID)).removeUpgrade(this.upgradeID);
        });
        ctx.setPacketHandled(true);
    }
}