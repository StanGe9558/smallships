package com.talhanation.smallships.upgrades;

import java.util.Optional;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class UpgradeNetworking {
    private static final String PROTOCOL_VERSION = "4";

    public static SimpleChannel INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation("smallships", "main"), () -> "4", "4"::equals, "4"::equals);
        int id = -1;

        INSTANCE.registerMessage(++id, UpdateUpgradePacket.class, UpdateUpgradePacket::toBytes, UpdateUpgradePacket::new, UpdateUpgradePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        INSTANCE.registerMessage(++id, SUpgradeRemovedPacket.class, SUpgradeRemovedPacket::toBytes, SUpgradeRemovedPacket::new, SUpgradeRemovedPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}

