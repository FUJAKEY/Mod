package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.capabilities.IBladder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import java.util.function.Supplier;

public class StopPeeingPacket {
    public StopPeeingPacket() {
        // No data needed
    }

    public static void encode(StopPeeingPacket pkt, PacketBuffer buf) {
        // No data to encode
    }

    public static StopPeeingPacket decode(PacketBuffer buf) {
        return new StopPeeingPacket();
    }

    public static void handle(StopPeeingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player != null) {
                player.getCapability(ExampleMod.BLADDER_CAP).ifPresent(bladder -> {
                    bladder.setPeeing(false);
                });
            }
        });
        context.setPacketHandled(true);
    }
}
