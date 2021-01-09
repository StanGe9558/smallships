package com.talhanation.smallships.init;

import com.talhanation.smallships.Main;
import com.talhanation.smallships.entities.CogEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;



public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, Main.MOD_ID);

    public static final RegistryObject<EntityType<CogEntity>> COG_ENTITY = ENTITY_TYPES.register("cog",
            () -> EntityType.Builder.<CogEntity>create(CogEntity::new, EntityClassification.MISC)
                    .size(3.5F, 1.25F)
                    .trackingRange(10)
                    .setUpdateInterval(3)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(Main.MOD_ID, "cog").toString()));

}