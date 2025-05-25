package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.client.ClientBladderData; // Added import
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncBladderDataPacket {

    private final float bladderLevel;

    // Constructor for sending
    public SyncBladderDataPacket(float bladderLevel) {
        this.bladderLevel = bladderLevel;
    }

    // Method for encoding
    public void encode(PacketBuffer buffer) {
        buffer.writeFloat(this.bladderLevel);
    }

    // Constructor for decoding (can call static decode or read directly)
    public SyncBladderDataPacket(PacketBuffer buffer) {
        this.bladderLevel = buffer.readFloat(); // Or call decode(buffer).bladderLevel
    }

    // Static method for decoding (alternative to direct reading in constructor)
    // public static SyncBladderDataPacket decode(PacketBuffer buffer) {
    // return new SyncBladderDataPacket(buffer.readFloat());
    // }

    // Method for handling on the client side
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            // ExampleMod.LOGGER.debug("Received SyncBladderDataPacket on client with level: " + this.bladderLevel); // Commented out
            ClientBladderData.currentBladderLevel = this.bladderLevel;
            ExampleMod.LOGGER.debug("ClientBladderData.currentBladderLevel updated to: " + ClientBladderData.currentBladderLevel);
        });
        context.setPacketHandled(true);
    }
}
