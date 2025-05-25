package com.example.examplemod;

import com.example.examplemod.network.EmptyBladderPacket;
import com.example.examplemod.network.PacketHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (ExampleMod.peeingKey == null) {
            return;
        }

        if (event.getKey() == ExampleMod.peeingKey.getKey().getValue() && event.getAction() == GLFW.GLFW_PRESS) {
            PacketHandler.INSTANCE.sendToServer(new EmptyBladderPacket());
            ExampleMod.LOGGER.debug("Peeing key pressed, sending EmptyBladderPacket.");
        }
    }
}
