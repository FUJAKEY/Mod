package com.example.examplemod.capabilities;

import net.minecraft.util.math.MathHelper;

public class Bladder implements IBladder {
    private float bladderLevel;

    public Bladder() {
        this.bladderLevel = 100.0f; // Default to full bladder
    }

    @Override
    public float getBladderLevel() {
        return this.bladderLevel;
    }

    @Override
    public void setBladderLevel(float level) {
        this.bladderLevel = MathHelper.clamp(level, 0.0f, 100.0f);
    }

    @Override
    public void addBladderLevel(float amount) {
        this.setBladderLevel(this.bladderLevel + amount);
    }

    @Override
    public void consumeBladderLevel(float amount) {
        this.setBladderLevel(this.bladderLevel - amount);
    }
}
