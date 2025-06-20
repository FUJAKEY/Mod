package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++,
                EmptyBladderPacket.class,
                EmptyBladderPacket::encode,
                EmptyBladderPacket::decode,
                EmptyBladderPacket::handle
        );
        INSTANCE.registerMessage(id++,
                SyncBladderDataPacket.class,
                SyncBladderDataPacket::encode,
                SyncBladderDataPacket::new, // Uses the PacketBuffer constructor
                SyncBladderDataPacket::handle
        );
        INSTANCE.registerMessage(id++,
                StartPeeingPacket.class,
                StartPeeingPacket::encode,
                StartPeeingPacket::decode,
                StartPeeingPacket::handle
        );
        INSTANCE.registerMessage(id++,
                StopPeeingPacket.class,
                StopPeeingPacket::encode,
                StopPeeingPacket::decode,
                StopPeeingPacket::handle
        );
        INSTANCE.registerMessage(id++,
                SpawnPeeBlockPacket.class,
                SpawnPeeBlockPacket::encode,
                SpawnPeeBlockPacket::decode,
                SpawnPeeBlockPacket::handle
        );
    }
}
