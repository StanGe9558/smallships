package com.talhanation.smallships.upgrades;

import com.talhanation.smallships.entities.UpgradeAbleEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CommonEventHandler {
    @SubscribeEvent
    public static void interact(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        Entity entity = player.getLowestRidingEntity();
        if (entity instanceof UpgradeAbleEntity) {
            ItemStack itemStack = player.getHeldItem(event.getHand());
            if (!itemStack.isEmpty()) {
                UpgradeAbleEntity planeEntity = (UpgradeAbleEntity)entity;
                for (Upgrade upgrade : planeEntity.upgrades.values())
                    upgrade.onItemRightClick(event);
            }
        }
    }
}
