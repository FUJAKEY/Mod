package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d; // Для 1.16.5
import net.minecraft.world.World; // Для 1.16.5
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnPeeBlockPacket {

    public SpawnPeeBlockPacket() {
        // Пустой конструктор
    }

    public static SpawnPeeBlockPacket decode(PacketBuffer buf) {
        return new SpawnPeeBlockPacket();
    }

    public static void encode(SpawnPeeBlockPacket pkt, PacketBuffer buf) {
        // Нет данных для кодирования
    }

    public static void handle(SpawnPeeBlockPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player == null || player.level == null) { // level это World в 1.16.5
                return;
            }
            World world = player.level;

            player.getCapability(ExampleMod.BLADDER_CAP).ifPresent(bladder -> {
                if (bladder.isPeeing() && bladder.getBladderLevel() > 0) {
                    BlockState blockState = Blocks.YELLOW_CONCRETE.defaultBlockState();
                    double forwardOffset = 0.4; // Смещение вперед от игрока
                    double upwardOffset = player.getEyeHeight() * 0.35; // Чуть ниже уровня глаз, на уровне груди/живота

                    Vector3d lookVector = player.getViewVector(1.0F); // Направление взгляда
                    // Начальная позиция для блока
                    double x = player.getX() + lookVector.x * forwardOffset;
                    double y = player.getY() + upwardOffset;
                    double z = player.getZ() + lookVector.z * forwardOffset;

                    FallingBlockEntity fallingBlock = new FallingBlockEntity(world, x, y, z, blockState);
                    fallingBlock.getPersistentData().putBoolean("CustomPeeBlock", true); // Наш специальный тег
                    fallingBlock.setNoGravity(false); // Убедимся, что гравитация включена
                    fallingBlock.time = 1; // Установим time в 1, чтобы он не превратился в блок сразу, если заспавнится в блоке
                                           // Также это поможет при проверке в WorldTickEvent

                    // Начальная скорость и направление для "струи"
                    float power = 0.5f; // Сила струи
                    float spread = 0.15f; // Разброс

                    fallingBlock.setDeltaMovement(
                            lookVector.x * power + (world.random.nextFloat() - 0.5f) * spread,
                            lookVector.y * power * 0.4f - 0.15f + (world.random.nextFloat() - 0.5f) * 0.1f, // Направлено немного вниз + случайность
                            lookVector.z * power + (world.random.nextFloat() - 0.5f) * spread
                    );

                    world.addFreshEntity(fallingBlock); // addFreshEntity для 1.16.5
                }
            });
        });
        context.setPacketHandled(true);
    }
}
