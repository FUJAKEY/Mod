package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor; // Added import

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
                float currentLevel = bladder.getBladderLevel();
                final float EMPTY_THRESHOLD = 1.0f;

                if (currentLevel < EMPTY_THRESHOLD) {
                    sender.sendMessage(new StringTextComponent("Your bladder is already empty!"), Util.NIL_UUID);
                    ExampleMod.LOGGER.info("Player " + sender.getName().getString() + " tried to empty bladder, but it's already nearly empty (Level: " + currentLevel + ").");
                    // Send current (near-empty) level to client
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new SyncBladderDataPacket(currentLevel));
                } else {
                    bladder.setBladderLevel(0f);
                    ExampleMod.LOGGER.info("Player " + sender.getName().getString() + "'s bladder emptied. New level: " + bladder.getBladderLevel());
                    // Send new (zero) level to client
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new SyncBladderDataPacket(bladder.getBladderLevel()));
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
