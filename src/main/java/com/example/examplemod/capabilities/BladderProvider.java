package com.example.examplemod.capabilities;

import com.example.examplemod.ExampleMod;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BladderProvider implements ICapabilitySerializable<CompoundNBT> {

    private final IBladder bladder = new Bladder();
    private final LazyOptional<IBladder> bladderOptional = LazyOptional.of(() -> bladder);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ExampleMod.BLADDER_CAP.orEmpty(cap, bladderOptional);
    }

    @Override
    public CompoundNBT serializeNBT() {
        if (ExampleMod.BLADDER_CAP == null) {
            return new CompoundNBT();
        }
        return (CompoundNBT) ExampleMod.BLADDER_CAP.getStorage().writeNBT(ExampleMod.BLADDER_CAP, this.bladder, null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (ExampleMod.BLADDER_CAP != null) {
            ExampleMod.BLADDER_CAP.getStorage().readNBT(ExampleMod.BLADDER_CAP, this.bladder, null, nbt);
        }
    }
}
