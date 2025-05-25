package com.example.examplemod;

import com.example.examplemod.network.EmptyBladderPacket;
import com.example.examplemod.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
// import net.minecraft.particles.ParticleTypes; // Commented out
// import net.minecraft.util.math.vector.Vector3d; // Commented out
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyInputHandler {

    private boolean wasPeeingKeyPressed = false; // Still useful to send packet only once per press

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { // Ensure we're not processing twice
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            boolean isPeeingKeyDown = ExampleMod.peeingKey.isDown();

            if (isPeeingKeyDown && !wasPeeingKeyPressed) {
                PacketHandler.INSTANCE.sendToServer(new EmptyBladderPacket());
                // player.setShiftKeyDown(true); // Removed

                // Particle spawning logic - Commented out
                /*
                if (player.level.random.nextFloat() < 0.33f) {
                    Vector3d lookVector = player.getLookAngle();
                    double spawnX = player.getX() + lookVector.x * 0.5;
                    double spawnY = player.getY() + player.getEyeHeight() - 0.8;
                    double spawnZ = player.getZ() + lookVector.z * 0.5;

                    player.level.addParticle(ParticleTypes.DRIPPING_WATER,
                            spawnX, spawnY, spawnZ,
                            (Math.random() - 0.5) * 0.1,
                            (Math.random() - 0.5) * 0.1,
                            (Math.random() - 0.5) * 0.1);
                }
                */
            }
            /* else if (!isPeeingKeyDown && wasPeeingKeyPressed) { // Key released
                // player.setShiftKeyDown(false); // Removed
            }
            */
            wasPeeingKeyPressed = isPeeingKeyDown;
        }
    }
}
