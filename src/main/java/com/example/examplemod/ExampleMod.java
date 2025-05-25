package com.example.examplemod;

import com.example.examplemod.capabilities.Bladder;
import com.example.examplemod.capabilities.BladderProvider;
import com.example.examplemod.capabilities.BladderStorage;
import com.example.examplemod.capabilities.IBladder;
import com.example.examplemod.network.PacketHandler;
import com.example.examplemod.network.SyncBladderDataPacket; // Added import
import com.example.examplemod.commands.CommandSetBladder;
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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.entity.Pose;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.entity.item.FallingBlockEntity;
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
    private static final float PEEING_RATE_PER_TICK = 0.5f; // 10 units per second (0.5 units * 20 ticks/sec)

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
        CommandSetBladder.register(event.getServer().getCommands().getDispatcher()); // Добавленная строка
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
                    if (!bladder.isPeeing() && bladder.getBladderLevel() < 100.0f) {
                        float levelBeforeNaturalFill = bladder.getBladderLevel();
                        bladder.addBladderLevel(BLADDER_FILL_RATE);
                        if (levelBeforeNaturalFill < 100.0f && bladder.getBladderLevel() >= 100.0f) {
                            serverPlayer.sendMessage(new TranslationTextComponent("message.examplemod.bladder.full"), serverPlayer.getUUID());
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
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isClientSide && event.phase == TickEvent.Phase.END) {
            if (!(event.world instanceof ServerWorld)) { // Дополнительная проверка типа мира
                return;
            }
            ServerWorld serverWorld = (ServerWorld) event.world;

            // Итерация по всем загруженным сущностям не очень оптимальна, но для начала подойдет.
            // В идеале, лучше было бы отслеживать наши FallingBlockEntity в специальном списке.
            for (Entity entity : serverWorld.getEntities().getAll()) { 
                if (entity instanceof FallingBlockEntity && entity.getPersistentData().contains("CustomPeeBlock") && entity.getPersistentData().getBoolean("CustomPeeBlock")) {
                    FallingBlockEntity fallingBlock = (FallingBlockEntity) entity;

                    // Логика удаления:
                    // 1. Если блок на земле и существует хотя бы короткое время (чтобы не удалить сразу при спавне, если он заспавнился в земле)
                    //    `fallingBlock.time` - это внутренний счетчик FallingBlockEntity, если он > 0 и onGround, он скоро превратится в блок.
                    //    `tickCount` - общее время жизни сущности в тиках.
                    boolean shouldRemove = false;
                    if (fallingBlock.isOnGround() && fallingBlock.tickCount > 1) { // Коснулся земли
                         // Дополнительно можно проверить fallingBlock.time, если нужно точнее контролировать превращение
                         shouldRemove = true;
                    }

                    // 2. Если блок "завис" в воздухе (почти не двигается) после некоторого времени
                    if (!shouldRemove && fallingBlock.getDeltaMovement().lengthSqr() < 0.001 && fallingBlock.tickCount > 20) { // 1 секунда в "зависшем" состоянии
                        shouldRemove = true;
                    }

                    // 3. Если блок существует слишком долго (например, пролетел сквозь мир или ошибка в логике)
                    if (!shouldRemove && fallingBlock.tickCount > 100) { // 5 секунд максимальное время жизни
                        shouldRemove = true;
                        // ExampleMod.LOGGER.debug("Removing CustomPeeBlock due to timeout: " + fallingBlock.getUUID());
                    }
                    
                    if (shouldRemove) {
                        fallingBlock.remove(); // Удаляем сущность
                        // ExampleMod.LOGGER.debug("Removed CustomPeeBlock: " + fallingBlock.getUUID());
                    }
                }
            }
        }
    }
}
