package com.talhanation.smallships.upgrades;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

public class Upgrades {
    private static final DeferredRegister<UpgradeType> UPGRADE_TYPES = DeferredRegister.create(UpgradeType.class, "simpleplanes");

    private static final Map<Item, UpgradeType> ITEM_UPGRADE_MAP = new HashMap<>();

    private static final Map<Item, UpgradeType> LARGE_ITEM_UPGRADE_MAP = new HashMap<>();

    public static void init() {
        UPGRADE_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static void registerUpgradeItem(Item item, UpgradeType upgradeType) {
        ITEM_UPGRADE_MAP.put(item, upgradeType);
    }

    public static void registerLargeUpgradeItem(Item item, UpgradeType upgradeType) {
        LARGE_ITEM_UPGRADE_MAP.put(item, upgradeType);
    }

    public static Optional<UpgradeType> getUpgradeFromItem(Item item) {
        if (ITEM_UPGRADE_MAP.containsKey(item))
            return Optional.of(ITEM_UPGRADE_MAP.get(item));
        return Optional.empty();
    }

    public static Optional<UpgradeType> getLargeUpgradeFromItem(Item item) {
        if (LARGE_ITEM_UPGRADE_MAP.containsKey(item))
            return Optional.of(LARGE_ITEM_UPGRADE_MAP.get(item));
        return Optional.empty();
    }

    // public static final RegistryObject<UpgradeType> SHOOTER = UPGRADE_TYPES.register("shooter", () -> new UpgradeType(xyz.przemyk.simpleplanes.upgrades.shooter.ShooterUpgrade::new));

    public static final RegistryObject<UpgradeType> BANNER = UPGRADE_TYPES.register("banner",
            () -> new UpgradeType(BannerUpgrade::new));
}