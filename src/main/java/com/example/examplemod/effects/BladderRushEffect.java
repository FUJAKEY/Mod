package com.example.examplemod.effects;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effects; // Required for referencing vanilla effects like Speed

public class BladderRushEffect extends Effect {

    public BladderRushEffect(EffectType typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);
        // The following line would add a permanent attribute modifier for speed.
        // However, we want the speed to be part of the effect's conditional application (on tick or when applied),
        // similar to how PotionItem applies effects.
        // For now, we will handle applying Speed directly when this effect is applied or via onEffectStarted.
        // this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "...", 0.20000000298023224D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        // The actual speed application will be handled when this effect is given to the player,
        // or by checking for this effect in PlayerTickEvent and applying Speed there if this effect is active.
        // For simplicity, we'll aim to apply the Speed effect when BladderRushEffect is applied.
    }

    @Override
    public void applyEffectTick(LivingEntity entityLiving, int amplifier) {
        // This method is called every tick for effects that are 'beneficial' or need continuous application.
        // We will handle the bladder fill rate increase in the PlayerTickEvent in ExampleMod.java
        // by checking if the player has this effect active.

        // Ensure player has Speed II if this effect is active.
        // This is one way to ensure the speed effect is present.
        // Another way is in onEffectStarted or when the effect is initially applied.
        // For simplicity, let's assume the Speed effect is applied once when BladderRush is applied.
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // For effects that need to do something every tick, this should return true.
        // Since primary logic (bladder rate, speed) will be handled elsewhere or on application,
        // this can be true if we needed per-tick logic here.
        return true; // Let's make it true for now, might be useful.
    }

    // It's often better to apply instant effects like Speed when the effect is first applied
    // or to manage it through the duration of this custom effect.
    // The PotionItem class adds effects like this:
    // player.addEffect(new EffectInstance(ModEffects.BLADDER_RUSH, 5 * 20, 0));
    // player.addEffect(new EffectInstance(Effects.SPEED, 5 * 20, 1)); // Speed II
    // So, the speed effect will be applied alongside this effect in UrineItem.java.
    // This class (BladderRushEffect) will then primarily be a marker effect for the PlayerTickEvent logic.
}
