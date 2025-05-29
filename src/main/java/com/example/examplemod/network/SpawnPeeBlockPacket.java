package com.example.examplemod.network;

import com.example.examplemod.ExampleMod; // Added import
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
// import net.minecraft.item.Items; // No longer needed for YELLOW_CONCRETE
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
                    // --- Новый код для ItemEntity ---
                    ItemStack itemStack = new ItemStack(ExampleMod.URINE_ITEM.get()); // Changed to URINE_ITEM
                    double forwardOffset = 0.4;
                    double upwardOffset = player.getEyeHeight() * 0.35;

                    Vector3d lookVector = player.getViewVector(1.0F);
                    double x = player.getX() + lookVector.x * forwardOffset;
                    double y = player.getY() + upwardOffset - 0.2; // Немного ниже для предмета, чтобы не появлялся слишком высоко
                    double z = player.getZ() + lookVector.z * forwardOffset;

                    ItemEntity itemEntity = new ItemEntity(world, x, y, z, itemStack);
                    
                    // Настройка, чтобы предмет можно было легко подобрать и он быстро исчез
                    itemEntity.setPickUpDelay(10); // Changed to 10 (0.5 seconds)
                    // Для 1.16.5 нет itemEntity.lifespan напрямую, но age увеличивается, и при 6000 он исчезает.
                    // Чтобы он исчез через 3 секунды (60 тиков):
                    // itemEntity.age = 5940; // Удалено согласно задаче

                    // Начальная скорость и направление
                    float power = 0.3f; // Уменьшил силу для предметов, они легче
                    float spread = 0.2f;

                    itemEntity.setDeltaMovement(
                            lookVector.x * power + (world.random.nextFloat() - 0.5f) * spread,
                            lookVector.y * power * 0.5f - 0.1f + (world.random.nextFloat() - 0.5f) * 0.15f, // Меньше вертикальной силы
                            lookVector.z * power + (world.random.nextFloat() - 0.5f) * spread
                    );
                    
                    world.addFreshEntity(itemEntity);
                    // Сохраняем UUID и текущее время мира (в тиках)
                    // The following lines are removed/commented out as per instructions:
                    // if (ExampleMod.instance != null) { 
                    //     ExampleMod.instance.customPeeItemsWithCreationTick.put(itemEntity.getUUID(), world.getGameTime());
                    // }
                }
            });
        });
        context.setPacketHandled(true);
    }
}
