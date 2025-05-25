package com.example.examplemod;

import com.example.examplemod.network.EmptyBladderPacket;
import com.example.examplemod.network.PacketHandler;
import com.example.examplemod.network.StartPeeingPacket;
import com.example.examplemod.network.StopPeeingPacket;
import com.example.examplemod.network.SpawnPeeBlockPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.particles.ParticleTypes;
import com.example.examplemod.client.ClientBladderData;

public class KeyInputHandler {

    private static boolean wasPeeingKeyDown = false;
    private int peeBlockSpawnCooldown = 0;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        // This method can be kept if there's a desire for an immediate (on press) effect
        // separate from the tick-based start/stop.
        // For this task, we're focusing on the tick-based handling, so this
        // specific EmptyBladderPacket sending might be redundant or conflicting
        // if not carefully managed with the new system.
        // For now, let's comment it out to avoid conflict with the new logic.
        /*
        if (ExampleMod.peeingKey == null) {
            return;
        }

        if (event.getKey() == ExampleMod.peeingKey.getKey().getValue() && event.getAction() == GLFW.GLFW_PRESS) {
            // PacketHandler.INSTANCE.sendToServer(new EmptyBladderPacket()); // Old logic
            // ExampleMod.LOGGER.debug("Peeing key pressed, sending EmptyBladderPacket.");
        }
        */
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { // Process at the end of the tick
            if (ExampleMod.peeingKey == null || Minecraft.getInstance().player == null) { // Ensure key and player exist
                return;
            }

            boolean isPeeingKeyDown = ExampleMod.peeingKey.isDown();
            if (isPeeingKeyDown && !wasPeeingKeyDown) {
                // Key was just pressed
                // Только отправляем пакет, если уровень мочевого пузыря > 0
                if (ClientBladderData.currentBladderLevel > 0) {
                    PacketHandler.INSTANCE.sendToServer(new StartPeeingPacket());
                }
            } else if (!isPeeingKeyDown && wasPeeingKeyDown) {
                // Key was just released
                PacketHandler.INSTANCE.sendToServer(new StopPeeingPacket());
            }
            wasPeeingKeyDown = isPeeingKeyDown;

            // Particle/Block generation logic
            boolean isPeeingKeyDownNow = ExampleMod.peeingKey.isDown(); // Re-check current state for this logic block

            if (isPeeingKeyDownNow && ClientBladderData.currentBladderLevel > 0 && Minecraft.getInstance().player != null) {
                if (this.peeBlockSpawnCooldown <= 0) {
                    PacketHandler.INSTANCE.sendToServer(new SpawnPeeBlockPacket());
                    this.peeBlockSpawnCooldown = 4; // Отправлять пакет каждые 4 тика (5 раз в секунду)
                }
                if (this.peeBlockSpawnCooldown > 0) {
                    this.peeBlockSpawnCooldown--;
                }
            } else {
                this.peeBlockSpawnCooldown = 0; // Сброс, если не мочится
            }
            // Старый код генерации частиц DRIPPING_WATER нужно удалить или закомментировать.
        }
    }
}
