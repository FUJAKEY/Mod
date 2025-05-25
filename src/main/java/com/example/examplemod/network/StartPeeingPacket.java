package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.capabilities.IBladder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import java.util.function.Supplier;

public class StartPeeingPacket {
    public StartPeeingPacket() {
        // No data needed
    }

    public static void encode(StartPeeingPacket pkt, PacketBuffer buf) {
        // No data to encode
    }

    public static StartPeeingPacket decode(PacketBuffer buf) {
        return new StartPeeingPacket();
    }

    public static void handle(StartPeeingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player != null) {
                player.getCapability(ExampleMod.BLADDER_CAP).ifPresent(bladder -> {
                    if (bladder.getBladderLevel() > 0) { // Only start peeing if not empty
                        bladder.setPeeing(true);
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}
