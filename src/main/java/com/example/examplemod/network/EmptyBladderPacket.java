package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class EmptyBladderPacket {

    public EmptyBladderPacket() {
        // Empty constructor
    }

    // Constructor for decoding
    public EmptyBladderPacket(PacketBuffer buf) {
        // No data to read
    }

    // Method for encoding
    public void encode(PacketBuffer buf) {
        // No data to write
    }

    // Static method for decoding
    public static EmptyBladderPacket decode(PacketBuffer buf) {
        return new EmptyBladderPacket();
    }

    // Method for handling
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity sender = ctx.get().getSender();
        if (sender == null) {
            return; // Should not happen with server-bound messages
        }

        ctx.get().enqueueWork(() -> {
            sender.getCapability(ExampleMod.BLADDER_CAP).ifPresent(bladder -> {
                bladder.setBladderLevel(0f);
                ExampleMod.LOGGER.info("Player " + sender.getName().getString() + "'s bladder emptied. New level: " + bladder.getBladderLevel());
                // Future: Trigger animation/particles here or send packet to client.
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
