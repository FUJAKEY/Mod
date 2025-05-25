package com.example.examplemod.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class BladderStorage implements Capability.IStorage<IBladder> {

    @Nullable
    @Override
    public INBT writeNBT(Capability<IBladder> capability, IBladder instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putFloat("bladderLevel", instance.getBladderLevel());
        return tag;
    }

    @Override
    public void readNBT(Capability<IBladder> capability, IBladder instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            instance.setBladderLevel(((CompoundNBT) nbt).getFloat("bladderLevel"));
        }
    }
}
