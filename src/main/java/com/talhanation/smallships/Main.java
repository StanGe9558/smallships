package com.talhanation.smallships;

import com.talhanation.smallships.client.events.ClientRegistry;
import com.talhanation.smallships.client.events.KeyEvents;
import com.talhanation.smallships.config.SmallShipsConfig;
import com.talhanation.smallships.init.ModEntityTypes;
import com.talhanation.smallships.init.SoundInit;
import com.talhanation.smallships.items.ModItems;
import com.talhanation.smallships.network.*;
import com.talhanation.smallships.upgrades.SimplePlanesDataSerializers;
import com.talhanation.smallships.upgrades.UpgradeNetworking;
import com.talhanation.smallships.upgrades.UpgradeType;
import com.talhanation.smallships.upgrades.Upgrades;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod("smallships")
public class Main {
    public static final String MOD_ID = "smallships";
    public static SimpleChannel SIMPLE_CHANNEL;
    public static KeyBinding SAIL_KEY;
    public static KeyBinding INV_KEY;

    public Main() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SmallShipsConfig.CONFIG);
        SmallShipsConfig.loadConfig(SmallShipsConfig.CONFIG, FMLPaths.CONFIGDIR.get().resolve("smallships-common.toml"));

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);

        Upgrades.init();
        SimplePlanesDataSerializers.init();
        UpgradeNetworking.init();
        SoundInit.SOUNDS.register(modEventBus);
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup));
    }

    private void setup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("smallships", "default"), () -> "1.0.0", s -> true, s -> true);

        SIMPLE_CHANNEL.registerMessage(0, MessageSailState.class, (msg, buf) -> msg.toBytes(buf),
                buf -> (new MessageSailState()).fromBytes(buf),
                (msg, fun) -> msg.executeServerSide(fun.get()));

        SIMPLE_CHANNEL.registerMessage(1, MessageSailStateGalley.class, (msg, buf) -> msg.toBytes(buf),
                buf -> (new MessageSailStateGalley()).fromBytes(buf),
                (msg, fun) -> msg.executeServerSide(fun.get()));

        SIMPLE_CHANNEL.registerMessage(2, MessagePaddleState.class, (msg, buf) -> msg.toBytes(buf),
                buf -> (new MessagePaddleState()).fromBytes(buf),
                (msg, fun) -> msg.executeServerSide(fun.get()));

        SIMPLE_CHANNEL.registerMessage(3, MessageSailStateWarGalley.class, (msg, buf) -> msg.toBytes(buf),
                buf -> (new MessageSailStateWarGalley()).fromBytes(buf),
                (msg, fun) -> msg.executeServerSide(fun.get()));

        SIMPLE_CHANNEL.registerMessage(4, MessageSailStateDrakkar.class, (msg, buf) -> msg.toBytes(buf),
                buf -> (new MessageSailStateDrakkar()).fromBytes(buf),
                (msg, fun) -> msg.executeServerSide(fun.get()));

        SIMPLE_CHANNEL.registerMessage(5, MessageSailStateBrigg.class, (msg, buf) -> msg.toBytes(buf),
                buf -> (new MessageSailStateBrigg()).fromBytes(buf),
                (msg, fun) -> msg.executeServerSide(fun.get()));

        SIMPLE_CHANNEL.registerMessage(6, MessageSteerState.class, (msg, buf) -> msg.toBytes(buf),
                buf -> (new MessageSteerState()).fromBytes(buf),
                (msg, fun) -> msg.executeServerSide(fun.get()));

        SIMPLE_CHANNEL.registerMessage(7, MessageOpenInv.class, (msg, buf) -> msg.toBytes(buf),
                buf -> (new MessageOpenInv()).fromBytes(buf),
                (msg, fun) -> msg.executeServerSide(fun.get()));

        Upgrades.registerUpgradeItem(Items.WHITE_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.ORANGE_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.MAGENTA_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.LIGHT_BLUE_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.YELLOW_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.LIME_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.PINK_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.GRAY_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.LIGHT_GRAY_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.CYAN_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.PURPLE_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.BLUE_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.BROWN_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.GREEN_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.RED_BANNER, (UpgradeType)Upgrades.BANNER.get());
        Upgrades.registerUpgradeItem(Items.BLACK_BANNER, (UpgradeType)Upgrades.BANNER.get());
    }



    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new KeyEvents());
        SAIL_KEY = ClientRegistry.registerKeyBinding("key.ship_sail", "category.smallships", 82);
        INV_KEY = ClientRegistry.registerKeyBinding("key.ship_inventory", "category.smallships", 73);
    }

}
