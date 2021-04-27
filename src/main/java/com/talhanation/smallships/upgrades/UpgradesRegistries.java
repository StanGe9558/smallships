package com.talhanation.smallships.upgrades;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = "smallships", bus = Mod.EventBusSubscriber.Bus.MOD)
public class UpgradesRegistries {
    public static IForgeRegistry<UpgradeType> UPGRADE_TYPES;

    @SubscribeEvent
    public static void registerRegistries(RegistryEvent.NewRegistry event) {
        UPGRADE_TYPES = (new RegistryBuilder()).setName(new ResourceLocation("smallships", "upgrade_types")).setType(UpgradeType.class).create();
    }
}
