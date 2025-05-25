package com.example.examplemod.capabilities;

public interface IBladder {
    float getBladderLevel();
    void setBladderLevel(float level);
    void addBladderLevel(float amount);
    void consumeBladderLevel(float amount);
    boolean isPeeing();
    void setPeeing(boolean isPeeing);
}
