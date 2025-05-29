package com.example.examplemod;

import com.example.examplemod.capabilities.Bladder;
import com.example.examplemod.capabilities.BladderProvider;
import com.example.examplemod.capabilities.BladderStorage;
import com.example.examplemod.capabilities.IBladder;
import com.example.examplemod.network.PacketHandler;
import com.example.examplemod.network.SyncBladderDataPacket; // Added import
import com.example.examplemod.commands.CommandSetBladder;
import com.example.examplemod.items.UrineItem;
import com.example.examplemod.effects.BladderRushEffect;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent; // Added import
import net.minecraftforge.fml.network.PacketDistributor; // Added import
import net.minecraft.entity.player.ServerPlayerEntity; // Added import
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.entity.Pose;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.entity.item.FallingBlockEntity; // This was for the old block logic, but might be okay to keep if other parts use it, or remove if certain it's no longer needed. For now, I'll leave it as the task only specified adding ItemEntity.
import net.minecraft.entity.item.ItemEntity; // Added
import java.util.Iterator; // Added
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks; // Keep this if used, remove if not.
import net.minecraftforge.event.RegistryEvent; // Keep this if used, remove if not. - For RegistryEvents class, might be removed/modified
import net.minecraftforge.fml.InterModComms; // Keep this if used, remove if not.

import java.util.stream.Collectors;
import java.util.UUID; // Set, Collections, HashSet removed
import java.util.Map; // Added
import java.util.concurrent.ConcurrentHashMap; // Added

// The value here should match an entry in the META-INF/mods.toml file
@Mod("pipimod")
public class ExampleMod
{
    public static final String MOD_ID = "pipimod";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static ExampleMod instance; // Добавлено для синглтона
    public final Map<UUID, Long> customPeeItemsWithCreationTick = new ConcurrentHashMap<>(); // Заменено поле

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, MOD_ID); // POTIONS is the registry name for Effects

    public static final RegistryObject<Item> URINE_ITEM = ITEMS.register("urine_item", UrineItem::new);
    public static final RegistryObject<Effect> BLADDER_RUSH_EFFECT = EFFECTS.register("bladder_rush_effect", () -> new BladderRushEffect(EffectType.BENEFICIAL, 0xFFFF00)); // Example color: yellow


    private static final float BLADDER_FILL_RATE = 0.002f; // Changed value from 0.01f
    private static final float PEEING_RATE_PER_TICK = 0.5f; // 10 units per second (0.5 units * 20 ticks/sec)

    public static KeyBinding peeingKey;

    @CapabilityInject(IBladder.class)
    public static Capability<IBladder> BLADDER_CAP = null;

    public ExampleMod() {
        instance = this; // Добавлено для синглтона
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());

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

        peeingKey = new KeyBinding("key.pipimod.peeing", GLFW.GLFW_KEY_R, "key.categories.pipimod");
        ClientRegistry.registerKeyBinding(peeingKey);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        // InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
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
        CommandSetBladder.register(event.getServer().getCommands().getDispatcher()); // Добавленная строка
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    // The DeferredRegister approach replaces the need for this for items and effects.
    // This class can be removed if it was only for block/item/effect registration handled by DeferredRegister.
    // Kept for now as it might be used for other registry types or the onBlocksRegistry might be relevant for other blocks.
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
        // Item and Effect registration is now handled by DeferredRegister
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
            if (!event.player.level.isClientSide) {
                if (!(event.player instanceof ServerPlayerEntity)) {
                    return;
                }
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.player;

                serverPlayer.getCapability(ExampleMod.BLADDER_CAP).ifPresent(bladder -> {
                    float initialLevelThisTick = bladder.getBladderLevel(); // Level at the very start of this player's tick processing

                    // 1. Process peeing action (reduces level) and player pose
                    if (bladder.isPeeing()) {
                        if (bladder.getBladderLevel() > 0) {
                            bladder.consumeBladderLevel(PEEING_RATE_PER_TICK);
                            if (serverPlayer.getPose() != Pose.CROUCHING) {
                                serverPlayer.setPose(Pose.CROUCHING);
                            }
                            if (bladder.getBladderLevel() == 0) { // Если моча кончилась во время процесса
                                bladder.setPeeing(false);
                            }
                        } else { // Если уровень уже был 0, но флаг isPeeing еще стоял
                            bladder.setPeeing(false);
                        }
                    }
                    
                    // Handle returning to STANDING pose if stopped peeing and not holding sneak
                    if (!bladder.isPeeing() && serverPlayer.getPose() == Pose.CROUCHING) {
                        if (!serverPlayer.isShiftKeyDown()) { // Only stand up if player is not manually sneaking
                            serverPlayer.setPose(Pose.STANDING);
                        }
                    }

                    // 2. Process bladder filling (natural, increases level)
                    // Also, handle Bladder Rush effect increasing fill rate
                    float currentFillRate = BLADDER_FILL_RATE;
                    if (serverPlayer.hasEffect(BLADDER_RUSH_EFFECT.get())) {
                        currentFillRate *= 2; // Example: Double fill rate with Bladder Rush
                    }

                    if (!bladder.isPeeing() && bladder.getBladderLevel() < 100.0f) {
                        float levelBeforeNaturalFill = bladder.getBladderLevel();
                        bladder.addBladderLevel(currentFillRate);
                        if (levelBeforeNaturalFill < 100.0f && bladder.getBladderLevel() >= 100.0f) {
                            serverPlayer.sendMessage(new TranslationTextComponent("message.pipimod.bladder.full"), serverPlayer.getUUID());
                        }
                    }


                    // --- Логика негативных эффектов ---
                    float currentBladderLevel = bladder.getBladderLevel();
                    int effectDuration = 105; // Длительность эффекта ~5 секунд (20 тиков = 1 секунда)

                    // Сначала проверяем самые строгие условия, чтобы избежать многократного наложения эффектов
                    // или чтобы более сильные эффекты перезаписывали более слабые, если они одного типа.
                    
                    // Уровень > 95%
                    if (currentBladderLevel > 95.0f) {
                        serverPlayer.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, effectDuration, 1, false, true)); // Замедление II
                        serverPlayer.addEffect(new EffectInstance(Effects.CONFUSION, effectDuration, 0, false, false));       // Тошнота
                    } 
                    // Уровень > 85% и <= 95%
                    else if (currentBladderLevel > 85.0f) {
                        serverPlayer.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, effectDuration, 0, false, true)); // Замедление I
                        // Даем Тошноту, но короче, если хотим чтобы она была менее навязчивой на этом уровне
                        serverPlayer.addEffect(new EffectInstance(Effects.CONFUSION, effectDuration / 2, 0, false, false)); 
                    } 
                    // Уровень > 70% и <= 85%
                    else if (currentBladderLevel > 70.0f) {
                        serverPlayer.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, effectDuration, 0, false, false)); // Замедление I (без частиц)
                    }
                    // Если уровень <= 70%, эффекты, наложенные этим модом, должны сами истечь,
                    // так как мы их не обновляем. Если игрок выпил молоко или умер, они также снимутся.
                    // --- Конец логики негативных эффектов ---
                    
                    // 3. Sync if level changed from the start of the tick, or periodically
                    if (bladder.getBladderLevel() != initialLevelThisTick || event.player.tickCount % 20 == 0) {
                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SyncBladderDataPacket(bladder.getBladderLevel()));
                    }

                    // 4. Logging
                    if (event.player.tickCount % 200 == 0) {
                        LOGGER.info("Player " + serverPlayer.getName().getString() + 
                                    " bladder: " + String.format("%.2f", bladder.getBladderLevel()) + 
                                    ", isPeeing: " + bladder.isPeeing());
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

    @SubscribeEvent
    public void onWorldTickTrackPeeItems(TickEvent.WorldTickEvent event) {
        if (!event.world.isClientSide && event.phase == TickEvent.Phase.END) {
            if (!(event.world instanceof ServerWorld)) {
                return;
            }
            ServerWorld serverWorld = (ServerWorld) event.world;
            long currentTime = serverWorld.getGameTime();

            // Логгируем размер мапы в начале каждого тика (можно сделать реже, если будет слишком много спама)
            // if (currentTime % 20 == 0) { // Например, раз в секунду
            //     ExampleMod.LOGGER.debug("TrackPeeItems - Map size: {}, Current time: {}", customPeeItemsWithCreationTick.size(), currentTime);
            // }

            Iterator<Map.Entry<UUID, Long>> iterator = customPeeItemsWithCreationTick.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();
                UUID itemUuid = entry.getKey();
                long creationTick = entry.getValue();
                
                Entity entity = serverWorld.getEntity(itemUuid);

                // Логгируем информацию о каждом отслеживаемом предмете
                // ExampleMod.LOGGER.debug("TrackPeeItems - Checking UUID: {}, CreationTick: {}, Age: {} ticks", itemUuid, creationTick, (currentTime - creationTick));

                if (entity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity) entity;
                    
                    if (currentTime - creationTick > 60) { // 3 секунды
                        ExampleMod.LOGGER.info("TrackPeeItems - Attempting to remove ItemEntity (timeout): UUID: {}, Age: {} ticks", itemUuid, (currentTime - creationTick));
                        itemEntity.remove(); 
                        iterator.remove();
                        ExampleMod.LOGGER.info("TrackPeeItems - ItemEntity removed from world and map: UUID: {}", itemUuid);
                    } else if (itemEntity.removed || !itemEntity.isAlive()) { 
                        ExampleMod.LOGGER.info("TrackPeeItems - ItemEntity was already removed/dead: UUID: {}. Removing from map.", itemUuid);
                        iterator.remove(); 
                    }
                } else {
                    // Если сущность не найдена или не ItemEntity
                    if (entity == null) {
                        ExampleMod.LOGGER.warn("TrackPeeItems - Entity not found for UUID: {}. CreationTick: {}. Age: {} ticks. Removing from map.", itemUuid, creationTick, (currentTime - creationTick));
                    } else {
                        ExampleMod.LOGGER.warn("TrackPeeItems - Entity for UUID: {} is not ItemEntity (Type: {}). CreationTick: {}. Age: {} ticks. Removing from map.", itemUuid, entity.getClass().getSimpleName(), creationTick, (currentTime - creationTick));
                    }
                    iterator.remove(); 
                }
            }
        }
    }
}
