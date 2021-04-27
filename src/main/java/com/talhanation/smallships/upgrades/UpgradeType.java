package com.talhanation.smallships.upgrades;

import com.talhanation.smallships.entities.UpgradeAbleEntity;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

public class UpgradeType extends ForgeRegistryEntry<UpgradeType> {
    public final Function<UpgradeAbleEntity, Upgrade> instanceSupplier;

    public final boolean isEngine;

    public UpgradeType(Function<UpgradeAbleEntity, Upgrade> instanceSupplier, boolean isEngine) {
        this.instanceSupplier = instanceSupplier;
        this.isEngine = isEngine;
    }

    public UpgradeType(Function<UpgradeAbleEntity, Upgrade> instanceSupplier) {
        this(instanceSupplier, false);
    }
}
