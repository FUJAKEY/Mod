package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyInputHandler {

    private boolean wasPeeingKeyPressed = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { // Ensure we're not processing twice
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            boolean isPeeingKeyDown = ExampleMod.peeingKey.isDown();

            if (isPeeingKeyDown) {
                player.setShiftKeyDown(true); // Make the player sneak

                // Particle spawning logic
                if (player.level.random.nextFloat() < 0.33f) {
                    Vector3d lookVector = player.getLookAngle();
                    double spawnX = player.getX() + lookVector.x * 0.5;
                    // Adjust Y to be lower, around waist height. Player's eye height is getEyeHeight(), so subtract a bit.
                    double spawnY = player.getY() + player.getEyeHeight() - 0.8; 
                    double spawnZ = player.getZ() + lookVector.z * 0.5;

                    player.level.addParticle(ParticleTypes.DRIPPING_WATER,
                            spawnX, spawnY, spawnZ,
                            (Math.random() - 0.5) * 0.1,
                            (Math.random() - 0.5) * 0.1,
                            (Math.random() - 0.5) * 0.1);
                }

            } else if (wasPeeingKeyPressed) {
                // Key was pressed last tick but not now (released)
                player.setShiftKeyDown(false); // Make the player stop sneaking
            }
            wasPeeingKeyPressed = isPeeingKeyDown;
        }
    }
}
