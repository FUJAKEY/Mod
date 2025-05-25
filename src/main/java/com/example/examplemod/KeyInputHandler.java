package com.example.examplemod;

import com.example.examplemod.network.EmptyBladderPacket;
import com.example.examplemod.network.PacketHandler;
import com.example.examplemod.network.StartPeeingPacket;
import com.example.examplemod.network.StopPeeingPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.client.multiplayer.ClientLevel; // Corrected import in original was ClientWorld
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import com.example.examplemod.client.ClientBladderData;

public class KeyInputHandler {

    private static boolean wasPeeingKeyDown = false;

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
                PacketHandler.INSTANCE.sendToServer(new StartPeeingPacket());
            } else if (!isPeeingKeyDown && wasPeeingKeyDown) {
                // Key was just released
                PacketHandler.INSTANCE.sendToServer(new StopPeeingPacket());
            }
            wasPeeingKeyDown = isPeeingKeyDown;

            // Particle generation logic
            boolean isPeeingKeyDownNow = ExampleMod.peeingKey.isDown();

            if (isPeeingKeyDownNow && ClientBladderData.currentBladderLevel > 0 && Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
                PlayerEntity player = Minecraft.getInstance().player;
                ClientLevel world = Minecraft.getInstance().level; // Corrected from ClientWorld

                double forwardOffset = 0.4; 
                double upwardOffset = player.getEyeHeight() * 0.3; 
                double particleSpeedMultiplier = 0.2;

                Vec3 lookVector = player.getViewVector(1.0F);
                
                double baseX = player.getX() + lookVector.x * forwardOffset;
                double baseY = player.getY() + upwardOffset;
                double baseZ = player.getZ() + lookVector.z * forwardOffset;

                for (int i = 0; i < 3; i++) { 
                    double spawnX = baseX + (world.random.nextDouble() - 0.5) * 0.3; 
                    double spawnY = baseY + (world.random.nextDouble() - 0.5) * 0.2; 
                    double spawnZ = baseZ + (world.random.nextDouble() - 0.5) * 0.3; 

                    double motionX = lookVector.x * particleSpeedMultiplier + (world.random.nextDouble() - 0.5) * 0.05;
                    double motionY = -0.10 + (world.random.nextDouble() - 0.1) * 0.05; 
                    double motionZ = lookVector.z * particleSpeedMultiplier + (world.random.nextDouble() - 0.5) * 0.05;
                
                    world.addParticle(ParticleTypes.DRIPPING_WATER,
                                      spawnX, spawnY, spawnZ, 
                                      motionX, motionY, motionZ);
                }
            }
        }
    }
}
