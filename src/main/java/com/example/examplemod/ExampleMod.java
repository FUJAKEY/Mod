package com.example.examplemod;

import com.example.examplemod.capabilities.Bladder;
import com.example.examplemod.capabilities.BladderProvider;
import com.example.examplemod.capabilities.BladderStorage;
import com.example.examplemod.capabilities.IBladder;
import com.example.examplemod.network.PacketHandler;
import com.example.examplemod.network.SyncBladderDataPacket; // Added import
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent; // Added import
import net.minecraftforge.fml.network.PacketDistributor; // Added import
import net.minecraft.entity.player.ServerPlayerEntity; // Added import
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks; // Keep this if used, remove if not.
import net.minecraftforge.event.RegistryEvent; // Keep this if used, remove if not.
import net.minecraftforge.fml.InterModComms; // Keep this if used, remove if not.

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("examplemod")
public class ExampleMod
{
    public static final String MOD_ID = "examplemod";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    private static final float BLADDER_FILL_RATE = 0.002f; // Changed value from 0.01f

    public static KeyBinding peeingKey;

    @CapabilityInject(IBladder.class)
    public static Capability<IBladder> BLADDER_CAP = null;

    public ExampleMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        CapabilityManager.INSTANCE.register(IBladder.class, new BladderStorage(), Bladder::new);
        PacketHandler.register(); // Added this line
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName()); // Example line, can be removed if Blocks isn't used
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);

        peeingKey = new KeyBinding("key.examplemod.peeing", GLFW.GLFW_KEY_R, "key.categories.examplemod");
        ClientRegistry.registerKeyBinding(peeingKey);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            BladderProvider provider = new BladderProvider();
            event.addCapability(new ResourceLocation(MOD_ID, "bladder"), provider);
            // The ICapabilitySerializable framework handles invalidation.
            // For LazyOptional, invalidation might be needed if the provider itself manages the LazyOptional's lifecycle directly.
            // However, in this setup, BladderProvider creates its own LazyOptional, and the capability system
            // typically handles when to invalidate capabilities attached to entities (e.g., on death or detach).
            // If specific invalidation logic is needed (e.g. player death), it would be handled by listening to player events.
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (!event.player.level.isClientSide) { // Server side only
                event.player.getCapability(ExampleMod.BLADDER_CAP).ifPresent(bladder -> {
                    bladder.addBladderLevel(BLADDER_FILL_RATE);
                    float newLevel = bladder.getBladderLevel();
                    if (event.player.tickCount % 20 == 0) { // Send update once per second
                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.player), new SyncBladderDataPacket(newLevel));
                    }
                    if (event.player.tickCount % 200 == 0) { // Log every 200 ticks (10 seconds)
                        LOGGER.info("Player " + event.player.getName().getString() + " bladder: " + newLevel);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            player.getCapability(ExampleMod.BLADDER_CAP).ifPresent(bladder -> {
                float currentLevel = bladder.getBladderLevel();
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SyncBladderDataPacket(currentLevel));
                ExampleMod.LOGGER.debug("Sent initial bladder level {} to player {}", currentLevel, player.getName().getString());
            });
        }
    }
}
