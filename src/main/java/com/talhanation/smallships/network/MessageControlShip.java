package com.talhanation.smallships.network;

import com.talhanation.smallships.entities.EntityShipBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class MessageControlShip implements Message<MessageControlShip> {
    private boolean forward;

    private boolean backward;

    private boolean left;

    private boolean right;

    private UUID uuid;

    public MessageControlShip() {}

    public MessageControlShip(boolean forward, boolean backward, boolean left, boolean right, PlayerEntity player) {
        this.forward = forward;
        this.backward = backward;
        this.left = left;
        this.right = right;
        this.uuid = player.getUniqueID();
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        Entity e = context.getSender().getRidingEntity();
        if (!(e instanceof EntityShipBase))
            return;
        EntityShipBase car = (EntityShipBase)e;
        car.updateControls(this.forward, this.backward, this.left, this.right, (PlayerEntity)context.getSender());
    }

    public MessageControlShip fromBytes(PacketBuffer buf) {
        this.forward = buf.readBoolean();
        this.backward = buf.readBoolean();
        this.left = buf.readBoolean();
        this.right = buf.readBoolean();
        this.uuid = buf.readUniqueId();
        return this;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(this.forward);
        buf.writeBoolean(this.backward);
        buf.writeBoolean(this.left);
        buf.writeBoolean(this.right);
        buf.writeUniqueId(this.uuid);
    }
}
