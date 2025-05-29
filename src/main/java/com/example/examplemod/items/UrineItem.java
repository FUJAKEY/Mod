package com.example.examplemod.items;

import com.example.examplemod.ExampleMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Food;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class UrineItem extends Item {

    public UrineItem() {
        super(new Item.Properties()
                .tab(ItemGroup.TAB_FOOD) // Or another appropriate item group
                .food(new Food.Builder()
                        .nutrition(1) // Low nutrition
                        .saturationMod(0.1f) // Low saturation
                        .effect(() -> new EffectInstance(Effects.POISON, 5 * 20, 0), 1.0F) // 5 seconds, amplifier 0 (Poison I)
                        // Custom "Bladder Rush" effect is NOT added here. It will be applied in finishUsingItem.
                        .alwaysEat() // Can eat even if not hungry
                        .build())
        );
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        if (!worldIn.isClientSide && entityLiving instanceof PlayerEntity) { // Ensure server side for effect application
            PlayerEntity player = (PlayerEntity) entityLiving;
            player.addEffect(new EffectInstance(ExampleMod.BLADDER_RUSH_EFFECT.get(), 5 * 20, 0)); // Bladder Rush, 5 seconds, amplifier 0
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 5 * 20, 1)); // Speed II, 5 seconds, amplifier 1
        }
        return super.finishUsingItem(stack, worldIn, entityLiving); // Poison is applied by super.finishUsingItem via Food.Builder
    }
}
