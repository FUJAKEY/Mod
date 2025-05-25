package com.example.examplemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.capabilities.IBladder;
import com.example.examplemod.network.PacketHandler;
import com.example.examplemod.network.SyncBladderDataPacket;
import net.minecraftforge.fml.network.PacketDistributor;

public class CommandSetBladder {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> command = Commands.literal("setbladder")
            .requires(source -> source.hasPermission(2)) // Требует уровень прав 2 (оператор)
            .then(Commands.argument("level", FloatArgumentType.floatArg(0, 100))
                .executes(context -> {
                    float level = FloatArgumentType.getFloat(context, "level");
                    ServerPlayerEntity player = context.getSource().getPlayerOrException();
                    player.getCapability(ExampleMod.BLADDER_CAP).ifPresent(bladder -> {
                        bladder.setBladderLevel(level);
                        // Отправляем пакет для синхронизации данных с клиентом
                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncBladderDataPacket(bladder.getBladderLevel()));
                        context.getSource().sendSuccess(new TranslationTextComponent("commands.examplemod.setbladder.success", String.format("%.0f", level)), true);
                    });
                    return 1;
                })
            )
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("level", FloatArgumentType.floatArg(0, 100))
                    .executes(context -> {
                        ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
                        float level = FloatArgumentType.getFloat(context, "level");
                        player.getCapability(ExampleMod.BLADDER_CAP).ifPresent(bladder -> {
                            bladder.setBladderLevel(level);
                            // Отправляем пакет для синхронизации данных с клиентом
                            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncBladderDataPacket(bladder.getBladderLevel()));
                            context.getSource().sendSuccess(new TranslationTextComponent("commands.examplemod.setbladder.success_other", player.getName().getString(), String.format("%.0f", level)), true);
                        });
                        return 1;
                    })
                )
            );
        dispatcher.register(command);
    }
}
