package com.example.examplemod.client.hud;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.client.ClientBladderData;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui; // Changed import
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BladderHudOverlay {

    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Post event) {
        MatrixStack matrixStack = event.getMatrixStack();

        if (event.getType() != ElementType.EXPERIENCE) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        float bladderLevel = ClientBladderData.currentBladderLevel;

        // Define bar properties
        int barWidth = 182; // Same as XP bar
        int barHeight = 5;
        int xPos = screenWidth / 2 - barWidth / 2;
        // Y position: XP bar is at screenHeight - 39 (bottom of bar).
        // Our bar's bottom will be screenHeight - 39 - 1 - barHeight = screenHeight - 45
        // Our bar's top will be screenHeight - 39 - 1 - barHeight = screenHeight - 45 - 5 = screenHeight - 50
        // Let's try yPos as the top of our bar.
        // The hunger bar's top is at screenHeight - 39 (hotbar top) - 10 (spacing + hunger bar height) = screenHeight - 49.
        // Let's try yPos = screenHeight - 49 to align with hunger bar top, or slightly above
        int yPos = screenHeight - 54; // Top of the bar, as per subtask

        // Define colors
        int backgroundColor = 0xFF000000; // Black
        int bladderColor = 0xFFFFFF00;    // Yellow
        // Optional: Border color (e.g., dark gray)
        // int borderColor = 0xFF555555;

        // Draw background bar
        AbstractGui.fill(matrixStack, xPos, yPos, xPos + barWidth, yPos + barHeight, backgroundColor);

        // Calculate filled width
        int filledWidth = (int) ((bladderLevel / 100.0f) * barWidth);
        filledWidth = Math.min(filledWidth, barWidth); // Ensure it doesn't exceed barWidth

        // Draw foreground (filled) bar
        AbstractGui.fill(matrixStack, xPos, yPos, xPos + filledWidth, yPos + barHeight, bladderColor);

        // Optional: Draw a border around the bar
        // AbstractGui.hLine(matrixStack, xPos - 1, xPos + barWidth, yPos - 1, borderColor); // Top
        // AbstractGui.hLine(matrixStack, xPos - 1, xPos + barWidth, yPos + barHeight, borderColor); // Bottom
        // AbstractGui.vLine(matrixStack, xPos - 1, yPos - 1, yPos + barHeight, borderColor); // Left
        // AbstractGui.vLine(matrixStack, xPos + barWidth, yPos - 1, yPos + barHeight, borderColor); // Right
    }
}
